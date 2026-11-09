package in.lmt.documents;

import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class ScanOperations {
 public record Input(@NotBlank @Size(min=10,max=300) @Pattern(regexp="[^\\p{Cntrl}]*") String reason){}
 public record Job(UUID id,UUID uploadId,String status,int revision,String etag,int attempts,int cycleAttempts,int maxAttempts,String lastCode,Instant updatedAt,Instant nextAttemptAt,Instant leaseUntil,String scannerMode){}
 public record Attempt(int attempt,String outcome,Instant startedAt,Instant finishedAt){}
 public record Detail(Job operation,List<Attempt> recentAttempts,int totalAttempts){}
 public record Claim(UUID id,UUID account,UUID upload,UUID token){}
 private final JdbcClient db;private final Audit audit;private final PrivateObjectStore objects;private final ObjectMapper json;private final int leaseSeconds;
 public ScanOperations(JdbcClient db,Audit audit,PrivateObjectStore objects,ObjectMapper json,@Value("${lmt.documents.scan-lease-seconds:30}")int seconds){this.db=db;this.audit=audit;this.objects=objects;this.json=json;if(seconds<1||seconds>300)throw new IllegalArgumentException("Invalid scan lease duration");leaseSeconds=seconds;}
 private void scope(UUID a){db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",a.toString()).query().singleRow();}
 private void authorize(UUID a,UUID upload){scope(a);if(db.sql("SELECT id FROM private_upload WHERE id=:id AND account_id=:a AND state='quarantined'").param("id",upload).param("a",a).query().listOfRows().isEmpty())throw new ApiProblem(404,"SCAN_NOT_FOUND","Scan operation is unavailable.");}
 private Instant time(java.sql.ResultSet r,String field)throws java.sql.SQLException{var value=r.getTimestamp(field);return value==null?null:value.toInstant();}
 private Job job(UUID upload){return db.sql("SELECT * FROM document_scan_job WHERE upload_id=:id").param("id",upload).query((r,n)->new Job(r.getObject("id",UUID.class),upload,r.getString("status"),r.getInt("revision"),"\"scan-"+r.getObject("id")+"-"+r.getInt("revision")+"\"",r.getInt("attempts"),r.getInt("cycle_attempts"),5,r.getString("last_code"),time(r,"updated_at"),time(r,"next_attempt_at"),time(r,"lease_until"),"unavailable")).optional().orElseThrow(()->new ApiProblem(404,"SCAN_NOT_FOUND","Scan operation is unavailable."));}
 @Transactional public void enqueue(UUID a,UUID upload){authorize(a,upload);var id=db.sql("INSERT INTO document_scan_job(upload_id,account_id,causation_id) SELECT id,account_id,request_key FROM private_upload WHERE id=:id AND account_id=:a ON CONFLICT(upload_id) DO NOTHING RETURNING id").param("id",upload).param("a",a).query(UUID.class).optional();id.ifPresent(v->audit.record(a,null,"DOCUMENT_SCAN_QUEUED",v));}
 @Transactional public Detail read(UUID a,UUID upload){authorize(a,upload);db.sql("SELECT id FROM document_scan_job WHERE upload_id=:id AND account_id=:a FOR SHARE").param("id",upload).param("a",a).query().listOfRows();var operation=job(upload);var attempts=db.sql("SELECT * FROM document_scan_attempt WHERE job_id=:id ORDER BY attempt DESC LIMIT 5").param("id",operation.id()).query((r,n)->new Attempt(r.getInt("attempt"),r.getString("outcome"),time(r,"started_at"),time(r,"finished_at"))).list();return new Detail(operation,attempts,operation.attempts());}
 private String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 @Transactional public Job command(UUID a,UUID upload,UUID key,String expected,Input input,boolean cancel){
  authorize(a,upload);String reason=input.reason().strip();if(reason.length()<10)throw new ApiProblem(422,"VALIDATION_FAILED","Give a reason of at least ten characters.","reason");
  db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:k,0))").param("k","scan-command:"+a+":"+key).query().singleRow();
  db.sql("SELECT id FROM document_scan_job WHERE upload_id=:id AND account_id=:a FOR UPDATE").param("id",upload).param("a",a).query().listOfRows().stream().findFirst().orElseThrow(()->new ApiProblem(404,"SCAN_NOT_FOUND","Scan operation is unavailable."));
  String fingerprint=hash(upload+":"+cancel+":"+expected+":"+reason);var prior=db.sql("SELECT request_hash,result::text FROM document_scan_command WHERE account_id=:a AND request_key=:k").param("a",a).param("k",key).query().listOfRows();
  if(!prior.isEmpty()){if(!fingerprint.equals(prior.getFirst().get("request_hash")))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different scan confirmation.");return json.readValue((String)prior.getFirst().get("result"),Job.class);}
  var current=job(upload);if(expected==null||expected.isBlank())throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the scan operation before confirming.");if(!expected.equals(current.etag()))throw new ApiProblem(412,"SCAN_STALE","The scan operation changed. Refresh before confirming.");
  if(cancel&&current.status().equals("cancelled"))throw new ApiProblem(409,"SCAN_ALREADY_CANCELLED","This operation is already cancelled.");
  if(!cancel&&!Set.of("blocked","failed","cancelled").contains(current.status()))throw new ApiProblem(409,"SCAN_NOT_RETRYABLE","Only blocked, failed or cancelled operations can be rechecked.");
  db.sql("UPDATE document_scan_attempt SET outcome='cancelled',finished_at=CURRENT_TIMESTAMP WHERE job_id=:id AND outcome='running'").param("id",current.id()).update();
  db.sql("UPDATE document_scan_job SET status=:s,revision=revision+1,cycle_attempts=CASE WHEN :cancel THEN cycle_attempts ELSE 0 END,lease_token=NULL,lease_until=NULL,last_code=NULL,next_attempt_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=:id")
   .param("s",cancel?"cancelled":"queued").param("cancel",cancel).param("id",current.id()).update();var result=job(upload);
  db.sql("INSERT INTO document_scan_command(account_id,request_key,job_id,request_hash,reason,result) VALUES(:a,:k,:id,:h,:reason,CAST(:r AS jsonb))").param("a",a).param("k",key).param("id",current.id()).param("h",fingerprint).param("reason",reason).param("r",json.writeValueAsString(result)).update();audit.record(a,null,cancel?"DOCUMENT_SCAN_CANCELLED":"DOCUMENT_SCAN_REQUEUED",current.id());return result;
 }
 @Transactional public List<Claim> claim(){
  var rows=db.sql("SELECT * FROM document_scan_job WHERE (status='queued' AND next_attempt_at<=CURRENT_TIMESTAMP) OR (status='running' AND lease_until<=CURRENT_TIMESTAMP) ORDER BY next_attempt_at,id LIMIT 5 FOR UPDATE SKIP LOCKED").query().listOfRows();var claims=new ArrayList<Claim>();
  for(var row:rows){UUID id=(UUID)row.get("id"),a=(UUID)row.get("account_id"),upload=(UUID)row.get("upload_id");
   db.sql("UPDATE document_scan_attempt SET outcome='lease_expired',finished_at=CURRENT_TIMESTAMP WHERE job_id=:id AND outcome='running'").param("id",id).update();
   if(((Number)row.get("cycle_attempts")).intValue()>=5){db.sql("UPDATE document_scan_job SET status='failed',last_code='RETRY_EXHAUSTED',lease_token=NULL,lease_until=NULL,revision=revision+1,updated_at=CURRENT_TIMESTAMP WHERE id=:id").param("id",id).update();audit.record(a,null,"DOCUMENT_SCAN_EXHAUSTED",id);continue;}
   UUID token=UUID.randomUUID();int attempt=((Number)row.get("attempts")).intValue()+1;
   db.sql("UPDATE document_scan_job SET status='running',attempts=attempts+1,cycle_attempts=cycle_attempts+1,lease_token=:t,lease_until=CURRENT_TIMESTAMP+(:seconds * interval '1 second'),revision=revision+1,updated_at=CURRENT_TIMESTAMP WHERE id=:id").param("t",token).param("seconds",leaseSeconds).param("id",id).update();
   db.sql("INSERT INTO document_scan_attempt(job_id,attempt,lease_token) VALUES(:id,:n,:t)").param("id",id).param("n",attempt).param("t",token).update();claims.add(new Claim(id,a,upload,token));
  }return claims;
 }
 private boolean fence(Claim c){return !db.sql("SELECT id FROM document_scan_job WHERE id=:id AND account_id=:a AND upload_id=:u AND status='running' AND lease_token=:t AND lease_until>clock_timestamp() FOR UPDATE").param("id",c.id()).param("a",c.account()).param("u",c.upload()).param("t",c.token()).query().listOfRows().isEmpty();}
 @Transactional public void complete(Claim c){
  if(!fence(c))return;scope(c.account());var row=db.sql("SELECT byte_size,sha256 FROM private_upload WHERE id=:u AND account_id=:a AND state='quarantined'").param("u",c.upload()).param("a",c.account()).query().listOfRows().stream().findFirst();
  String code="SUBJECT_AUTHORITY_UNAVAILABLE";
  if(row.isPresent())code=switch(objects.inspect(c.upload(),((Number)row.get().get("byte_size")).intValue(),(String)row.get().get("sha256"))){case intact->"SCANNER_UNAVAILABLE";case missing->"STORAGE_MISSING";case mismatch->"STORAGE_CONTENT_MISMATCH";case unavailable->"STORAGE_UNAVAILABLE";};
  if(!fence(c))return;
  db.sql("UPDATE document_scan_job SET status='blocked',last_code=:code,lease_token=NULL,lease_until=NULL,revision=revision+1,updated_at=CURRENT_TIMESTAMP WHERE id=:id").param("code",code).param("id",c.id()).update();
  db.sql("UPDATE document_scan_attempt SET outcome=:code,finished_at=CURRENT_TIMESTAMP WHERE lease_token=:t").param("code",code).param("t",c.token()).update();audit.record(c.account(),null,"DOCUMENT_SCAN_BLOCKED",c.id());
 }
 @Transactional public void failed(Claim c){
  if(!fence(c))return;
  int cycle=db.sql("SELECT cycle_attempts FROM document_scan_job WHERE id=:id").param("id",c.id()).query(Integer.class).single();
  int delay=Math.min(60,1<<cycle)+java.util.concurrent.ThreadLocalRandom.current().nextInt(1,4);
  db.sql("UPDATE document_scan_job SET status='queued',last_code='TRANSIENT_FAILURE',lease_token=NULL,lease_until=NULL,next_attempt_at=CURRENT_TIMESTAMP+(:seconds * interval '1 second'),revision=revision+1,updated_at=CURRENT_TIMESTAMP WHERE id=:id").param("seconds",delay).param("id",c.id()).update();
  db.sql("UPDATE document_scan_attempt SET outcome='transient_failure',finished_at=CURRENT_TIMESTAMP WHERE lease_token=:t").param("t",c.token()).update();audit.record(c.account(),null,"DOCUMENT_SCAN_RETRY_SCHEDULED",c.id());
 }
}
