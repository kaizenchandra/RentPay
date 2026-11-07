package in.lmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class LmtApplication {
    public static void main(String[] args) { SpringApplication.run(LmtApplication.class, args); }
}
