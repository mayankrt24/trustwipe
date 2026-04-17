package com.example.trustwipe.config;

import com.example.trustwipe.service.DriveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data loader that runs on startup.
 * Modified to be non-destructive: It only performs a real drive scan.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private DriveService driveService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting initial system drive scan...");
        
        // Retry logic for MongoDB connection
        int retries = 5;
        while (retries > 0) {
            try {
                driveService.scanAndRegisterDrives("system@trustwipe.com");
                log.info("Initial scan complete. Database is now persistent.");
                break;
            } catch (Exception e) {
                retries--;
                log.error("Failed to perform initial drive scan: {}. Retrying in 5s... (Retries left: {})", e.getMessage(), retries);
                if (retries > 0) {
                    Thread.sleep(5000);
                } else {
                    log.error("MAX RETRIES REACHED. Initial drive scan failed permanently.");
                }
            }
        }
    }
}
