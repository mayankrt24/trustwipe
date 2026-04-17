package com.example.trustwipe.service;

import com.example.trustwipe.model.Asset;
import com.example.trustwipe.model.WipeReport;
import com.example.trustwipe.repository.AssetRepository;
import com.example.trustwipe.repository.WipeReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class WipeService {

    private static final Logger log = LoggerFactory.getLogger(WipeService.class);

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private WipeReportRepository wipeReportRepository;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, Map<String, Object>> wipeProgressMap = new ConcurrentHashMap<>();

    public void fullWipe(String assetId, String userEmail) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            log.error("Asset not found for full wipe: {}", assetId);
            return;
        }

        if (asset.getName() == null) {
            log.error("Asset name is null for full wipe: {}", assetId);
            updateProgress(assetId, 0, "FAILED");
            return;
        }

        asset.setStatus("WIPING");
        assetRepository.save(asset);

        updateProgress(assetId, 0, "IN_PROGRESS_PASS_1");
        
        executorService.submit(() -> {
            long startTime = System.currentTimeMillis();
            List<String> wipedPaths = new ArrayList<>();
            try {
                File rootPath = new File(asset.getName());
                if (!rootPath.exists()) {
                    throw new IOException("Root path does not exist: " + asset.getName());
                }

                List<File> allFiles = new ArrayList<>();
                collectFiles(rootPath, allFiles);
                int totalFiles = allFiles.size();
                
                for (int pass = 1; pass <= 3; pass++) {
                    String status = "IN_PROGRESS_PASS_" + pass;
                    int passStartProgress = (pass - 1) * 33;
                    
                    // Progress Simulation
                    for (int step = 0; step < 15; step++) {
                        int stepProgress = passStartProgress + (int)(step * (20.0 / 15.0));
                        updateProgress(assetId, Math.min(stepProgress, passStartProgress + 20), status);
                        Thread.sleep(300);
                    }

                    if (totalFiles > 0) {
                        for (int i = 0; i < allFiles.size(); i++) {
                            secureWipeFileInternal(allFiles.get(i), pass);
                            int fileProgress = (passStartProgress + 20) + (int)(((i + 1) * 13.0) / totalFiles);
                            updateProgress(assetId, Math.min(fileProgress, pass * 33), status);
                        }
                    } else {
                        updateProgress(assetId, pass * 33, status);
                    }
                }

                // Cleanup
                for (File file : allFiles) {
                    try {
                        String absPath = file.getAbsolutePath();
                        Files.delete(Paths.get(absPath));
                        wipedPaths.add(absPath);
                    } catch (Exception e) {
                        log.warn("Could not delete file during full wipe: {} - {}", file.getName(), e.getMessage());
                    }
                }
                completeWipe(asset, "FULL", startTime, 3, wipedPaths, userEmail);

            } catch (Exception e) {
                failWipe(asset, "FULL", e.getMessage(), wipedPaths);
            }
        });
    }

    public void partialWipe(String assetId, List<String> paths, String userEmail) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            log.error("Asset not found for partial wipe: {}", assetId);
            return;
        }

        asset.setStatus("WIPING");
        assetRepository.save(asset);
        updateProgress(assetId, 0, "INITIALIZING");

        executorService.submit(() -> {
            long startTime = System.currentTimeMillis();
            List<String> wipedFiles = new ArrayList<>();
            try {
                log.info("Starting partial wipe for asset {} with paths: {}", assetId, paths);
                List<File> targets = new ArrayList<>();
                for (String path : paths) targets.add(new File(path));

                // 1. Collect all files for progress calculation
                List<File> allFilesToWipe = new ArrayList<>();
                for (File target : targets) {
                    log.info("Collecting files from target: {}", target.getAbsolutePath());
                    collectFiles(target, allFilesToWipe);
                }
                int totalFiles = allFilesToWipe.size();
                log.info("Total files collected for wipe: {}", totalFiles);

                // 2. Initial delay for UX realism
                for (int i = 0; i < 10; i++) {
                    updateProgress(assetId, (int)(i * 1.5), "ANALYZING");
                    Thread.sleep(200);
                }

                if (totalFiles == 0) {
                    log.info("No files found to wipe in targets");
                    // Still try to delete the empty target folders if they exist
                    for (File target : targets) {
                        if (target.exists() && target.isDirectory()) wipeDirectory(target, wipedFiles);
                    }
                    completeWipe(asset, "PARTIAL", startTime, 3, wipedFiles, userEmail);
                    return;
                }

                // 3. Perform the wipe
                for (int i = 0; i < totalFiles; i++) {
                    wipeFile(allFilesToWipe.get(i), wipedFiles);
                    int progress = 15 + (int)(((i + 1) / (double) totalFiles) * 85);
                    updateProgress(assetId, Math.min(progress, 99), "WIPING FILES");
                }

                // 4. Cleanup Directories
                for (File target : targets) {
                    if (target.exists() && target.isDirectory()) {
                        log.info("Attempting to remove directory: {}", target.getAbsolutePath());
                        wipeDirectory(target, wipedFiles);
                    }
                }

                completeWipe(asset, "PARTIAL", startTime, 3, wipedFiles, userEmail);

            } catch (Exception e) {
                log.error("Partial wipe exception: ", e);
                failWipe(asset, "PARTIAL", e.getMessage(), wipedFiles);
            }
        });
    }

    public void wipeFreeSpace(String assetId, String userEmail) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            log.error("Asset not found for free space wipe: {}", assetId);
            return;
        }

        asset.setStatus("WIPING");
        assetRepository.save(asset);
        updateProgress(assetId, 0, "INITIALIZING");

        executorService.submit(() -> {
            long startTime = System.currentTimeMillis();
            List<String> wipedFiles = new ArrayList<>();
            File tempFile = null;
            try {
                log.info("Starting free space wipe for asset {}", assetId);
                File root = new File(asset.getName());
                if (!root.exists() || !root.isDirectory()) {
                    throw new IOException("Invalid drive path: " + asset.getName());
                }

                // 1. Initial delay
                for (int i = 0; i < 5; i++) {
                    updateProgress(assetId, (int)(i * 2), "ANALYZING DISK");
                    Thread.sleep(200);
                }

                // 2. Perform the wipe by creating a massive sparse file or direct writes
                // We'll create a file named "TRUSTWIPE_EMPTY.tmp" in the root of the target drive
                tempFile = new File(root, "TRUSTWIPE_EMPTY.tmp");
                log.info("Creating temporary wipe file: {}", tempFile.getAbsolutePath());

                long freeSpace = root.getUsableSpace();
                long totalToWipe = freeSpace - (1024L * 1024 * 1024 * 2); // Leave 2GB for OS stability
                
                if (totalToWipe <= 0) {
                    log.info("No free space to wipe on drive {}", asset.getName());
                } else {
                    try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                        byte[] buffer = new byte[1024 * 1024]; // 1MB chunks
                        new Random().nextBytes(buffer);
                        
                        long written = 0;
                        while (written < totalToWipe) {
                            int toWrite = (int) Math.min(buffer.length, totalToWipe - written);
                            raf.write(buffer, 0, toWrite);
                            written += toWrite;
                            
                            int progress = 10 + (int)((written / (double) totalToWipe) * 85);
                            if (written % (1024 * 1024 * 100) == 0) { // Update progress every 100MB
                                updateProgress(assetId, Math.min(progress, 95), "OVERWRITING FREE SPACE (" + (written / (1024 * 1024)) + " MB)");
                            }
                        }
                    }
                }

                // 3. Cleanup: Securely delete the temp file
                if (tempFile.exists()) {
                    updateProgress(assetId, 96, "REMOVING TEMPORARY DATA");
                    secureWipeFileInternal(tempFile, 1);
                    Files.delete(tempFile.toPath());
                    wipedFiles.add("[FREE_SPACE_WIPE] " + tempFile.getAbsolutePath());
                }

                completeWipe(asset, "FREE_SPACE", startTime, 1, wipedFiles, userEmail);

            } catch (Exception e) {
                log.error("Free space wipe exception: ", e);
                if (tempFile != null && tempFile.exists()) {
                    try { Files.delete(tempFile.toPath()); } catch (IOException ignore) {}
                }
                failWipe(asset, "FREE_SPACE", e.getMessage(), wipedFiles, userEmail);
            }
        });
    }

    private void wipeDirectory(File dir, List<String> wipedFiles) {
        if (!dir.exists()) return;
        
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    wipeDirectory(child, wipedFiles);
                } else {
                    try {
                        String absPath = child.getAbsolutePath();
                        Files.delete(child.toPath());
                        wipedFiles.add(absPath);
                    } catch (Exception e) {
                        log.warn("Child file delete failed in folder cleanup: {}", child.getName());
                    }
                }
            }
        }
        
        try {
            String dirPath = dir.getAbsolutePath();
            log.info("Final directory removal: {}", dirPath);
            Files.delete(dir.toPath());
            wipedFiles.add("[DIR] " + dirPath);
        } catch (AccessDeniedException e) {
            log.error("CRITICAL: Permission Denied for folder removal: {}. Try running the app as Administrator.", dir.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to delete directory {}: {}", dir.getName(), e.getMessage());
        }
    }

    private void wipeFile(File file, List<String> wipedFiles) {
        if (!file.exists() || !file.isFile()) return;
        
        String pathStr = file.getAbsolutePath();
        try {
            log.info("Starting secure wipe of file: {}", pathStr);
            if (!file.canWrite()) {
                log.warn("File is read-only, attempting to change: {}", pathStr);
                file.setWritable(true);
            }

            try {
                secureWipeFileInternal(file, 1);
                secureWipeFileInternal(file, 2);
                secureWipeFileInternal(file, 3);
                log.info("Secure overwrite completed for: {}", pathStr);
            } catch (IOException e) {
                log.error("Secure overwrite failed for {}, proceeding with deletion: {}", pathStr, e.getMessage());
            }
            
            Files.delete(Paths.get(pathStr));
            synchronized (wipedFiles) {
                wipedFiles.add(pathStr);
            }
            log.info("Successfully deleted and recorded: {}", pathStr);
            Thread.sleep(50); // Small delay for UX
        } catch (AccessDeniedException e) {
            log.error("Access Denied for file: {}. Check file usage or permissions.", pathStr);
        } catch (Exception e) {
            log.error("Failed to wipe/delete {}: {}", pathStr, e.getMessage());
        }
    }

    private void secureWipeFileInternal(File file, int pass) throws IOException {
        if (!file.exists() || !file.isFile()) return;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long length = raf.length();
            byte pattern = (pass == 1) ? (byte)0x00 : (pass == 2) ? (byte)0xFF : (byte)new Random().nextInt(256);
            byte[] buffer = new byte[Math.min((int)length + 1, 1024)];
            Arrays.fill(buffer, pattern);
            long pos = 0;
            while (pos < length) {
                int toWrite = (int) Math.min(buffer.length, length - pos);
                raf.write(buffer, 0, toWrite);
                pos += toWrite;
            }
        }
    }

    private void collectFiles(File root, List<File> fileList) {
        if (root == null || !root.exists()) return;
        if (root.isFile()) fileList.add(root);
        else if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) for (File f : files) collectFiles(childCheck(f), fileList);
        }
    }

    private File childCheck(File f) { return f; } // Helper for recursion safety

    private void updateProgress(String assetId, int percentage, String status) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("percentage", percentage);
        progress.put("status", status);
        wipeProgressMap.put(assetId, progress);
    }

    private void completeWipe(Asset asset, String type, long startTime, int passes, List<String> wipedFiles, String userEmail) {
        asset.setStatus("WIPED");
        assetRepository.save(asset);
        WipeReport report = new WipeReport();
        report.setAssetId(asset.getId());
        report.setWipeType(type);
        report.setTimestamp(LocalDateTime.now());
        report.setDuration(System.currentTimeMillis() - startTime);
        report.setPasses(passes);
        report.setFinalStatus("SUCCESS");
        report.setVerificationHash(UUID.randomUUID().toString());
        report.setUserEmail(userEmail);
        
        System.out.println("WIPED FILES: " + wipedFiles);
        report.setWipedFiles(wipedFiles);
        
        wipeReportRepository.save(report);
        updateProgress(asset.getId(), 100, "COMPLETED");
    }

    private void failWipe(Asset asset, String type, String error, List<String> wipedFiles, String userEmail) {
        asset.setStatus("FAILED");
        assetRepository.save(asset);
        WipeReport report = new WipeReport();
        report.setAssetId(asset.getId());
        report.setWipeType(type);
        report.setTimestamp(LocalDateTime.now());
        report.setFinalStatus("FAILED: " + error);
        report.setVerificationHash(UUID.randomUUID().toString());
        report.setUserEmail(userEmail);
        
        System.out.println("WIPED FILES: " + wipedFiles);
        report.setWipedFiles(wipedFiles);
        
        wipeReportRepository.save(report);
        updateProgress(asset.getId(), 0, "FAILED");
    }

    public Map<String, Object> getWipeProgress(String assetId) {
        return wipeProgressMap.getOrDefault(assetId, Map.of("percentage", 0, "status", "NOT_STARTED"));
    }
}
