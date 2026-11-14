package club.ttg.bug.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BugReportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BugReportServiceApplication.class, args);
    }
}
