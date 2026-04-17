package com.example.trustwipe.service;

import com.example.trustwipe.model.Asset;
import com.example.trustwipe.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DriveService {

    private static final Logger log = LoggerFactory.getLogger(DriveService.class);

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Scans for system drives and automatically registers new ones in the database.
     * @return List of all registered assets.
     */
    public List<Asset> scanAndRegisterDrives(String userEmail) {
        File[] roots = File.listRoots();
        log.info("System roots detected: {} for user: {}", roots.length, userEmail);
        
        // 1. Get all currently registered assets for this user
        List<Asset> existingAssets = assetRepository.findByUserEmail(userEmail);
        List<String> currentRootPaths = new ArrayList<>();
        List<Asset> activeAssets = new ArrayList<>();

        // 2. Process physical drives found on the system
        for (File root : roots) {
            String path = root.getAbsolutePath();
            log.info("Checking system root: {}", path);
            currentRootPaths.add(path.toUpperCase());
            
            Optional<Asset> existingAsset = assetRepository.findByNameIgnoreCaseAndUserEmail(path, userEmail);

            if (existingAsset.isEmpty()) {
                log.info("Registering NEW drive: {} for user: {}", path, userEmail);
                String type = detectDriveType(root);
                long size = root.getTotalSpace();
                
                if (size > 0) {
                    Asset newAsset = new Asset(path, type, size, "CONNECTED", userEmail);
                    Asset saved = assetRepository.save(newAsset);
                    log.info("Saved new asset to MongoDB with ID: {}", saved.getId());
                    activeAssets.add(saved);
                } else {
                    log.warn("Drive {} has 0 size, skipping registration", path);
                }
            } else {
                Asset asset = existingAsset.get();
                log.info("Drive {} already exists in DB for user {} with status: {}", path, userEmail, asset.getStatus());
                // Update status to CONNECTED if it was disconnected
                if (!"WIPING".equals(asset.getStatus()) && !"WIPED".equals(asset.getStatus())) {
                    asset.setStatus("CONNECTED");
                    assetRepository.save(asset);
                }
                activeAssets.add(asset);
            }
        }

        // 3. Update status of registered assets that are NOT in the current scan
        for (Asset asset : existingAssets) {
            // SKIP: If it's a remote asset, don't mark it as disconnected here
            // The AgentController handles the status of remote assets
            if (asset.getType() != null && asset.getType().contains("REMOTE")) {
                continue;
            }

            if (asset.getName() != null && !currentRootPaths.contains(asset.getName().toUpperCase())) {
                if (!"DISCONNECTED".equals(asset.getStatus()) && 
                    !"WIPED".equals(asset.getStatus()) && 
                    !"FAILED".equals(asset.getStatus())) {
                    log.info("Drive disconnected: {}", asset.getName());
                    asset.setStatus("DISCONNECTED");
                    assetRepository.save(asset);
                }
            }
        }

        return activeAssets;
    }

    private String detectDriveType(File root) {
        // High-level heuristic for drive type
        // In a real scenario, this would use OS-specific commands (lsblk, wmic, etc.)
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Very basic heuristic: A: and B: are floppy, C: is usually HDD/SSD
            if (root.getAbsolutePath().startsWith("A:") || root.getAbsolutePath().startsWith("B:")) {
                return "FLOPPY";
            }
            // Can't easily distinguish HDD/SSD/USB via File API alone in Java
            // Defaulting to "UNKNOWN" for simulation
            return "UNKNOWN";
        }
        return "UNKNOWN";
    }

    public List<Asset> getAllRegisteredAssets(String userEmail) {
        return assetRepository.findByUserEmail(userEmail).stream()
                .filter(a -> a.getName() != null)
                .toList();
    }
}
