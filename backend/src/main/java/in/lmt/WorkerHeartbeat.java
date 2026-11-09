package in.lmt;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.process",havingValue="worker")
public class WorkerHeartbeat {
 @org.springframework.scheduling.annotation.Scheduled(fixedDelay=1000)
 public void beat() throws java.io.IOException {java.nio.file.Files.writeString(java.nio.file.Path.of("/tmp/lmt-worker-heartbeat"),"alive");}
}
