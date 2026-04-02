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
        
        // We removed assetRepository.deleteAll() to maintain data persistence.
        // Instead, we just trigger a real scan to find your actual hardware.
        try {
            driveService.scanAndRegisterDrives();
            log.info("Initial scan complete. Database is now persistent.");
        } catch (Exception e) {
            log.error("Failed to perform initial drive scan: {}", e.getMessage());
        }
    }
}
