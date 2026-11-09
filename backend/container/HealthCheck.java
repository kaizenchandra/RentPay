import java.net.*;
import java.nio.file.*;
public class HealthCheck {
 public static void main(String[] args)throws Exception {
  if(args.length>0&&args[0].equals("worker")&&(!Files.exists(Path.of("/tmp/lmt-worker-heartbeat"))||System.currentTimeMillis()-Files.getLastModifiedTime(Path.of("/tmp/lmt-worker-heartbeat")).toMillis()>30000))System.exit(1);
  var c=(HttpURLConnection)URI.create("http://127.0.0.1:8080/actuator/health").toURL().openConnection();c.setConnectTimeout(3000);c.setReadTimeout(3000);System.exit(c.getResponseCode()==200?0:1);
 }
}
