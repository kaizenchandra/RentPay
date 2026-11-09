package in.lmt.documents;

import in.lmt.shared.ApiProblem;
import in.lmt.shared.Audit;
import java.io.InputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.security.MessageDigest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class StorageRecovery {
 public record View(UUID uploadId,String outcome,int revision,Instant checkedAt,String etag,String scanStatus){}
 private record Upload(int size,String sha256,String state){}
 private final JdbcClient db;
 private final PrivateObjectStore objects;
 private final Audit audit;
 private final ObjectMapper json;
 public StorageRecovery(JdbcClient db,PrivateObjectStore objects,Audit audit,ObjectMapper json){this.db=db;this.objects=objects;this.audit=audit;this.json=json;}
 private void scope(UUID actor){db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",actor.toString()).query().singleRow();}
 private Upload authorize(UUID actor,UUID id){
  var upload=db.sql("SELECT byte_size,sha256,state FROM private_upload WHERE account_id=:a AND id=:id FOR UPDATE").param("a",actor).param("id",id)
   .query((r,n)->new Upload(r.getInt("byte_size"),r.getString("sha256"),r.getString("state"))).optional()
   .orElseThrow(()->new ApiProblem(404,"UPLOAD_NOT_FOUND","Upload not found."));
  if(!upload.state().equals("quarantined"))throw new ApiProblem(409,"UPLOAD_NOT_QUARANTINED","Complete the upload before checking stored bytes.");
  return upload;
 }
 private String tag(UUID id,int version){return "\"storage-"+id+"-"+version+"\"";}
 private View current(UUID actor,UUID id){
  return db.sql("SELECT outcome,revision,checked_at FROM upload_storage_review WHERE account_id=:a AND upload_id=:id").param("a",actor).param("id",id)
   .query((r,n)->new View(id,r.getString("outcome"),r.getInt("revision"),r.getTimestamp("checked_at").toInstant(),tag(id,r.getInt("revision")),"unavailable"))
   .optional().orElse(new View(id,"unchecked",0,null,tag(id,0),"unavailable"));
 }
 @Transactional public View read(UUID actor,UUID id){scope(actor);authorize(actor,id);audit.record(actor,null,"UPLOAD_STORAGE_REVIEWED",id);return current(actor,id);}
 private void keyLock(UUID actor,UUID key){db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key","storage-command:"+actor+":"+key).query().singleRow();}
 private String hash(String text){return hash(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
 private String hash(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private Optional<View> prior(UUID actor,UUID key,String fingerprint){
  var row=db.sql("SELECT request_hash,result::text FROM upload_storage_command WHERE account_id=:a AND request_key=:k").param("a",actor).param("k",key).query().listOfRows().stream().findFirst();
  if(row.isEmpty())return Optional.empty();
  if(!row.get().get("request_hash").equals(fingerprint))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different storage confirmation.");
  return Optional.of(json.readValue((String)row.get().get("result"),View.class));
 }
 private void precondition(String expected,View view){
  if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review current storage status before confirming.");
  if(!expected.equals(view.etag()))throw new ApiProblem(412,"STORAGE_REVIEW_STALE","Storage status changed. Refresh and review it again.");
 }
 private View record(UUID actor,UUID id,UUID key,String operation,String fingerprint,String outcome){
  db.sql("INSERT INTO upload_storage_review(upload_id,account_id,outcome,revision,checked_at) VALUES(:id,:a,:o,1,CURRENT_TIMESTAMP) ON CONFLICT(upload_id) DO UPDATE SET outcome=EXCLUDED.outcome,revision=upload_storage_review.revision+1,checked_at=EXCLUDED.checked_at")
   .param("id",id).param("a",actor).param("o",outcome).update();
  var result=current(actor,id);
  db.sql("INSERT INTO upload_storage_command(account_id,request_key,upload_id,operation,request_hash,result) VALUES(:a,:k,:id,:op,:h,CAST(:r AS jsonb))")
   .param("a",actor).param("k",key).param("id",id).param("op",operation).param("h",fingerprint).param("r",json.writeValueAsString(result)).update();
  audit.record(actor,null,operation.equals("check")?"UPLOAD_STORAGE_CHECKED":"UPLOAD_STORAGE_RESTORED",id);
  return result;
 }
 @Transactional public View check(UUID actor,UUID id,UUID key,String expected){
  scope(actor);keyLock(actor,key);var upload=authorize(actor,id);
  if(expected==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review current storage status before confirming.");
  String fingerprint=hash("check:"+id+":"+expected);var replay=prior(actor,key,fingerprint);if(replay.isPresent())return replay.get();
  precondition(expected,current(actor,id));
  return record(actor,id,key,"check",fingerprint,objects.inspect(id,upload.size(),upload.sha256()).name());
 }
 @Transactional public View restore(UUID actor,UUID id,UUID key,String expected,InputStream input){
  scope(actor);keyLock(actor,key);var upload=authorize(actor,id);
  if(expected==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review missing-file status before confirming.");
  byte[] bytes;try{bytes=input.readNBytes(upload.size()+1);}catch(IOException e){throw new ApiProblem(503,"UPLOAD_INTERRUPTED","Transfer interrupted. Retry this same restoration.");}
  if(bytes.length!=upload.size()||!hash(bytes).equals(upload.sha256()))throw new ApiProblem(409,"UPLOAD_CONTENT_CONFLICT","Only the exact original file can restore this evidence.");
  String fingerprint=hash("restore:"+id+":"+expected+":"+upload.sha256());var replay=prior(actor,key,fingerprint);if(replay.isPresent())return replay.get();
  var review=current(actor,id);precondition(expected,review);
  if(!review.outcome().equals("missing"))throw new ApiProblem(409,"STORAGE_RESTORE_NOT_ALLOWED","Restoration requires a reviewed missing file. Changed objects require operator investigation.");
  var actual=objects.inspect(id,upload.size(),upload.sha256());
  if(actual==PrivateObjectStore.Integrity.mismatch)throw new ApiProblem(409,"STORAGE_CONTENT_CONFLICT","Stored bytes changed. Nothing was overwritten; operator investigation is required.");
  if(actual==PrivateObjectStore.Integrity.unavailable)throw new ApiProblem(503,"STORAGE_UNAVAILABLE","Storage cannot be checked. Retry the same restoration later.");
  // The durable, exact-digest upload intent and missing-file review predate this write.
  // A matching object after a rolled-back audit is reconciled, never overwritten.
  objects.put(id,bytes);
  if(objects.inspect(id,upload.size(),upload.sha256())!=PrivateObjectStore.Integrity.intact)throw new ApiProblem(503,"STORAGE_UNAVAILABLE","Stored bytes could not be confirmed. Retry the same restoration.");
  return record(actor,id,key,"restore",fingerprint,"intact");
 }
}
