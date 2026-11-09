package in.lmt.documents;
import in.lmt.shared.*;
import jakarta.validation.constraints.*;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.security.MessageDigest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class Uploads {
 public record Input(@NotBlank @Size(max=120) @Pattern(regexp="[^\\p{Cntrl}/\\\\]+") String filename,
  @NotNull @Pattern(regexp="application/pdf|image/png|image/jpeg") String mediaType,
  @Min(1) @Max(1048576) int byteSize,@NotNull @Pattern(regexp="[a-f0-9]{64}") String sha256){}
 public record View(UUID id,String filename,String mediaType,int byteSize,String sha256,String state,Instant expiresAt,String etag,String mode,String scanStatus){}
 public record Page(int limit,String nextCursor){}
 public record Listing(List<View> items,Page page){}
 private final JdbcClient db;private final Audit audit;private final PrivateObjectStore objects;private final ScanOperations scans;
 public Uploads(JdbcClient db,Audit audit,PrivateObjectStore objects,ScanOperations scans){this.db=db;this.audit=audit;this.objects=objects;this.scans=scans;}
 private void scope(UUID a){db.sql("SELECT set_config('lmt.account_id',:a,true)").param("a",a.toString()).query().singleRow();}
 private View map(java.sql.ResultSet r,int n)throws java.sql.SQLException{
  String state=r.getString("state");Instant expiry=r.getTimestamp("expires_at").toInstant();UUID id=r.getObject("id",UUID.class);
  String displayedState=state.equals("awaiting_upload")&&expiry.isBefore(Instant.now())?"expired":state;
  return new View(id,r.getString("filename"),r.getString("media_type"),r.getInt("byte_size"),r.getString("sha256"),displayedState,expiry,"\"upload-"+id+"-"+displayedState+"\"","local_sandbox","unavailable");
 }
 private View find(UUID a,UUID id,boolean lock){return db.sql("SELECT * FROM private_upload WHERE account_id=:a AND id=:id"+(lock?" FOR UPDATE":"")).param("a",a).param("id",id).query(this::map).optional().orElseThrow(()->new ApiProblem(404,"UPLOAD_NOT_FOUND","Upload not found."));}
 @Transactional public View read(UUID a,UUID id){scope(a);var v=find(a,id,false);audit.record(a,null,"UPLOAD_STATUS_READ",id);return v;}
 @Transactional(readOnly=true) public Listing list(UUID a,UUID cursor){scope(a);var rows=db.sql("SELECT * FROM private_upload WHERE account_id=:a AND id>:c ORDER BY id LIMIT 26").param("a",a).param("c",cursor==null?new UUID(0,0):cursor).query(this::map).list();var items=rows.stream().limit(25).toList();return new Listing(items,new Page(25,rows.size()>25?items.getLast().id().toString():null));}
 @Transactional public View create(UUID a,UUID key,Input in){
  scope(a);if(!objects.available())throw new ApiProblem(503,"STORAGE_UNAVAILABLE","Private storage is not configured.");
  db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:a,0))").param("a","upload:"+a).query().singleRow();
  var prior=db.sql("SELECT * FROM private_upload WHERE account_id=:a AND request_key=:k").param("a",a).param("k",key).query(this::map).optional();
  if(prior.isPresent()){var v=prior.get();if(!v.filename().equals(in.filename())||!v.mediaType().equals(in.mediaType())||v.byteSize()!=in.byteSize()||!v.sha256().equals(in.sha256()))throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This key belongs to a different file.");return new View(v.id(),v.filename(),v.mediaType(),v.byteSize(),v.sha256(),"awaiting_upload",v.expiresAt(),"\"upload-"+v.id()+"-awaiting_upload\"",v.mode(),v.scanStatus());}
  if(db.sql("SELECT count(*) FROM private_upload WHERE account_id=:a AND created_at>CURRENT_TIMESTAMP-interval '1 hour'").param("a",a).query(Long.class).single()>=20)throw new ApiProblem(429,"UPLOAD_RATE_LIMIT","The local sandbox accepts 20 upload intents per account per hour. Try later.");
  UUID id=UUID.randomUUID();db.sql("INSERT INTO private_upload(id,account_id,request_key,filename,media_type,byte_size,sha256,expires_at) VALUES(:id,:a,:k,:f,:m,:s,:h,CURRENT_TIMESTAMP+interval '15 minutes')").param("id",id).param("a",a).param("k",key).param("f",in.filename()).param("m",in.mediaType()).param("s",in.byteSize()).param("h",in.sha256()).update();audit.record(a,null,"UPLOAD_INTENT_CREATED",id);return find(a,id,false);
 }
 @Transactional public View receive(UUID a,UUID id,String expected,InputStream stream){
  scope(a);var v=find(a,id,true);
  if(expected==null)throw new ApiProblem(428,"PRECONDITION_REQUIRED","Review the upload before sending the file.");
  if(!expected.equals("\"upload-"+id+"-awaiting_upload\""))throw new ApiProblem(412,"UPLOAD_STALE","Reload this upload before retrying.");
  if(v.state().equals("expired"))throw new ApiProblem(409,"UPLOAD_EXPIRED","This intent expired. Create a new intent for the file.");
  byte[] bytes;try{bytes=stream.readNBytes(v.byteSize()+1);}catch(IOException e){throw new ApiProblem(503,"UPLOAD_INTERRUPTED","The upload was interrupted. Retry the same file.");}
  if(bytes.length!=v.byteSize())throw new ApiProblem(422,"FILE_SIZE_MISMATCH","File size differs from the confirmed intent.");
  try{if(!HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).equals(v.sha256()))throw new ApiProblem(409,"UPLOAD_CONTENT_CONFLICT","The file differs from the confirmed fingerprint.");}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
  boolean signature=switch(v.mediaType()){
   case "application/pdf" -> bytes.length>=5&&new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-");
   case "image/png" -> bytes.length>=8&&Arrays.equals(Arrays.copyOf(bytes,8),new byte[]{(byte)137,80,78,71,13,10,26,10});
   default -> bytes.length>=3&&bytes[0]==(byte)255&&bytes[1]==(byte)216&&bytes[2]==(byte)255;
  };
  if(!signature)throw new ApiProblem(422,"FILE_TYPE_MISMATCH","File signature does not match its declared type.");
  if(v.state().equals("quarantined"))return v;
  objects.put(id,bytes);
  db.sql("UPDATE private_upload SET state='quarantined',received_at=CURRENT_TIMESTAMP WHERE id=:id AND account_id=:a").param("id",id).param("a",a).update();audit.record(a,null,"UPLOAD_QUARANTINED",id);scans.enqueue(a,id);return find(a,id,false);
 }
}
