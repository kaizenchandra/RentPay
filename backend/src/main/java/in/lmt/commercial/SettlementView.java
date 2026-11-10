package in.lmt.commercial;
import java.time.Instant;
import java.util.UUID;
public record SettlementView(UUID id,String status,int version,String etag,Instant receivedAt,Instant appliedAt,Instant reversedAt,int attempts,String lastCode){}
