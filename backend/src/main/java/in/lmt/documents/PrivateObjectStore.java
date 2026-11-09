package in.lmt.documents;
import java.util.UUID;
/** Private immutable quarantine objects. No public URL or unvalidated retrieval capability. */
public interface PrivateObjectStore {
 enum Integrity { intact, missing, mismatch, unavailable }
 boolean available();
 Integrity inspect(UUID intent, int expectedSize, String expectedSha256);
 void put(UUID intent, byte[] content);
}
