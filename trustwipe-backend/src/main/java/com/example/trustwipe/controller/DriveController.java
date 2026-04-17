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
    public List<Object> listPath(@RequestParam String path, @RequestParam(required = false) String userEmail) {
        System.out.println("DEBUG: Listing path requested: [" + path + "]");
        
        // Remote Check: If it's a Windows drive letter or specifically marked remote
        if (path.contains(":") || path.startsWith("\\\\")) {
            String agentId = "MAYANK-LAPTOP-01"; // Demo fixed ID
            
            // 1. Tell the agent to scan
            Map<String, Object> cmd = new HashMap<>();
            cmd.put("command", "LIST_FILES");
            cmd.put("path", path);
            agentController.enqueueCommand(agentId, cmd);
            
            // 2. Wait a bit for the agent to report back (synchronous simulation for demo)
            try {
                for (int i = 0; i < 20; i++) { // Wait up to 2 seconds
                    Thread.sleep(100);
                    List<Object> files = agentController.getFiles(agentId);
                    if (!files.isEmpty()) return files;
                }
            } catch (InterruptedException e) {}
            
            return new ArrayList<>(); // Return empty if agent is slow
        }

        // Local Fallback (for Render server's own disk)
        File folder = new File(path);
        List<FileInfo> result = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
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
        }
        return (List)result;
    }

    @GetMapping
    public List<Asset> getDrives(@RequestParam String userEmail) {
        return driveService.getAllRegisteredAssets(userEmail);
    }
}
