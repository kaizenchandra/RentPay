package in.lmt.documents;
import in.lmt.identity.IdentityConfig;
import in.lmt.shared.ApiProblem;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.*;
import org.springframework.stereotype.Component;
@Component
public class LocalQuarantineStore implements PrivateObjectStore {
 private final Path root;
 public LocalQuarantineStore(IdentityConfig config) {
  root=config.sandbox?Path.of(config.inbox).toAbsolutePath().resolveSibling("quarantine"):null;
 }
 public boolean available(){return root!=null;}
 public Integrity inspect(UUID id,int size,String sha256){
  if(!available())return Integrity.unavailable;
  try {
   if(Files.isSymbolicLink(root))return Integrity.unavailable;
   Path target=root.resolve(id.toString());
   var attributes=Files.readAttributes(target,java.nio.file.attribute.BasicFileAttributes.class,LinkOption.NOFOLLOW_LINKS);
   if(!attributes.isRegularFile()||attributes.size()!=size)return Integrity.mismatch;
   try(var input=Files.newInputStream(target,LinkOption.NOFOLLOW_LINKS)){
    byte[] bytes=input.readNBytes(size+1);
    return bytes.length==size&&fingerprint(bytes).equals(sha256)?Integrity.intact:Integrity.mismatch;
   }
  }catch(NoSuchFileException e){return Integrity.missing;}
  catch(java.io.IOException | UnsupportedOperationException e){return Integrity.unavailable;}
 }
 private static String fingerprint(byte[] bytes){
  try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}
  catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
 public void put(UUID id,byte[] content){
  if(!available())throw new ApiProblem(503,"STORAGE_UNAVAILABLE","Private storage is not configured.");
  Path temporary=null;
  try{
   Files.createDirectories(root,PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
   if(Files.isSymbolicLink(root))throw new java.io.IOException("Unsafe storage root");
   Files.setPosixFilePermissions(root,PosixFilePermissions.fromString("rwx------"));
   Path target=root.resolve(id.toString());
   if(Files.exists(target,LinkOption.NOFOLLOW_LINKS)){
    if(inspect(id,content.length,fingerprint(content))!=Integrity.intact)throw new java.io.IOException("Object mismatch");
    return;
   }
   temporary=Files.createTempFile(root,"incoming-",".tmp",PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
   try(var channel=FileChannel.open(temporary,StandardOpenOption.WRITE)){var bytes=ByteBuffer.wrap(content);while(bytes.hasRemaining())channel.write(bytes);channel.force(true);}
   try { Files.createLink(target,temporary); }
   catch(FileAlreadyExistsException e){
    if(inspect(id,content.length,fingerprint(content))!=Integrity.intact)throw new java.io.IOException("Object conflict");
   }
  }catch(java.io.IOException | UnsupportedOperationException e){throw new ApiProblem(503,"STORAGE_UNAVAILABLE","Private storage is unavailable. Retry the same upload.");}
  finally{if(temporary!=null)try{Files.deleteIfExists(temporary);}catch(java.io.IOException ignored){/* Private orphan; never exposed. */}}
 }
}
