package com.example.trustwipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
public class TrustwipeApplication {

    private static final Logger log = LoggerFactory.getLogger(TrustwipeApplication.class);

    public static void main(String[] args) {
        checkAdminPrivileges();
        SpringApplication.run(TrustwipeApplication.class, args);
    }

    private static void checkAdminPrivileges() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            try {
                // 'net session' command only works if running as Admin on Windows
                Process process = Runtime.getRuntime().exec("net session");
                process.waitFor();
                if (process.exitValue() == 0) {
                    log.info("------------------------------------------------");
                    log.info("SUCCESS: Running with ADMINISTRATOR privileges.");
                    log.info("------------------------------------------------");
                } else {
                    log.warn("------------------------------------------------");
                    log.warn("WARNING: NOT running as Administrator.");
                    log.warn("Some wiping operations may fail on system files.");
                    log.warn("------------------------------------------------");
                }
            } catch (IOException | InterruptedException e) {
                log.error("Could not determine admin privileges.");
            }
        }
    }

}
