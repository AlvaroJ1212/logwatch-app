package com.logwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogwatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogwatchApplication.class, args);
    }
}
