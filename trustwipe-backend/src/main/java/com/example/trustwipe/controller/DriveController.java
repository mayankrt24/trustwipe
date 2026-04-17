package com.example.trustwipe.controller;

import com.example.trustwipe.dto.FileInfo;
import com.example.trustwipe.model.Asset;
import com.example.trustwipe.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drives")
public class DriveController {

    @Autowired
    private DriveService driveService;

    @Autowired
    private AgentController agentController;

    @GetMapping("/scan")
    public List<Asset> scanDrives(@RequestParam String userEmail) {
        return driveService.scanAndRegisterDrives(userEmail);
    }

    @GetMapping("/list-path")
    public List<Object> listPath(@RequestParam String path, @RequestParam String userEmail) {
        System.out.println("DEBUG: Listing path requested: [" + path + "] for user: " + userEmail);
        
        File folder = new File(path);

        // 1. SMART CHECK: If the path actually exists on THIS machine, use it directly!
        if (folder.exists() && folder.isDirectory()) {
            System.out.println("DEBUG: Path found LOCALLY. Listing directly.");
            File[] files = folder.listFiles();
            List<FileInfo> result = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    result.add(new FileInfo(
                        file.getName(),
                        file.getAbsolutePath(),
                        file.isDirectory(),
                        file.isDirectory() ? 0 : file.length()
                    ));
                }
            }
            return (List)result;
        }

        // 2. REMOTE FALLBACK: Use Agent.
        // For the demo, we'll try to find the agentId from the assets list
        String agentId = "MAYANK-LAPTOP-01"; 
        
        // Find if any asset for this user and path is a REMOTE asset
        List<Asset> userAssets = driveService.getAllRegisteredAssets(userEmail);
        for (Asset asset : userAssets) {
            if (path.startsWith(asset.getName()) && asset.getType().contains("REMOTE")) {
                // Extract agentId from "REMOTE [AGENT_ID]"
                String type = asset.getType();
                if (type.contains("[") && type.contains("]")) {
                    agentId = type.substring(type.indexOf("[") + 1, type.indexOf("]"));
                }
                break;
            }
        }

        System.out.println("DEBUG: Triggering Agent: " + agentId);
        Map<String, Object> cmd = new HashMap<>();
        cmd.put("command", "LIST_FILES");
        cmd.put("path", path);
        agentController.enqueueCommand(agentId, userEmail, cmd);
        
        try {
            // Wait up to 10 seconds (agent now polls every 1 sec)
            for (int i = 0; i < 100; i++) { 
                Thread.sleep(100);
                List<Object> files = agentController.getFiles(agentId, userEmail);
                if (!files.isEmpty()) return files;
            }
        } catch (InterruptedException e) {}
        
        return new ArrayList<>();
    }

    @GetMapping
    public List<Asset> getDrives(@RequestParam String userEmail) {
        return driveService.getAllRegisteredAssets(userEmail);
    }
}
