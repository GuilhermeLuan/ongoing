package dev.guilhermeluan.ongoing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OngoingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OngoingApplication.class, args);
    }

}
