package in.lmt.identity;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.simple.JdbcClient;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
@Component
public class SandboxDelivery {
 private final JdbcClient db;private final IdentityConfig config;private final ChallengeService challenges;
 public SandboxDelivery(JdbcClient db,IdentityConfig config,ChallengeService challenges){this.db=db;this.config=config;this.challenges=challenges;}
 @Scheduled(fixedDelayString="${lmt.identity.delivery-delay-ms:1000}") @Transactional
 public void deliver(){
   if(!config.sandbox||config.localContainers)return;
   var rows=db.sql("SELECT d.challenge_id,c.expires_at FROM auth_delivery d JOIN auth_challenge c ON c.id=d.challenge_id WHERE c.identity_mode='sandbox' AND d.status='pending' AND d.next_attempt_at<=CURRENT_TIMESTAMP ORDER BY c.created_at LIMIT 10 FOR UPDATE OF d SKIP LOCKED").query().listOfRows();
   for(var row:rows){
    UUID id=(UUID)row.get("challenge_id");String status="delivered";
    if(((java.sql.Timestamp)row.get("expires_at")).toInstant().isBefore(java.time.Instant.now()))status="expired";
    else try {
      Path dir=Path.of(config.inbox);Files.createDirectories(dir,PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
      Path target=dir.resolve(id+".txt");
      // Stable filename/code makes delivery safe to repeat after a crash before DB commit.
      if(!Files.exists(target))Files.createFile(target,PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
      Files.writeString(target,challenges.code(id),StandardOpenOption.TRUNCATE_EXISTING);
    }catch(java.io.IOException e){
      db.sql("UPDATE auth_delivery SET attempts=attempts+1,status=CASE WHEN attempts>=4 THEN 'failed' ELSE 'pending' END,next_attempt_at=CURRENT_TIMESTAMP+interval '10 seconds' WHERE challenge_id=:id").param("id",id).update();continue;
    }
    db.sql("UPDATE auth_delivery SET status=:status WHERE challenge_id=:id").param("status",status).param("id",id).update();
   }
 }
 @Scheduled(fixedDelay=60000)
 public void cleanExpiredInbox(){
   if(!config.sandbox||config.localContainers)return;
   Path dir=Path.of(config.inbox);
   if(!Files.isDirectory(dir))return;
   try(var files=Files.list(dir)){
     for(Path file:files.filter(p->p.getFileName().toString().matches("[0-9a-f-]{36}\\.txt")).toList()){
       if(Files.getLastModifiedTime(file).toMillis()<System.currentTimeMillis()-360000)Files.deleteIfExists(file);
     }
   }catch(java.io.IOException ignored){/* Delivery remains bounded; cleanup retries next minute. */}
 }

}
