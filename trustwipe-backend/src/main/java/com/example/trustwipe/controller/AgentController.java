package com.example.trustwipe.controller;

import com.example.trustwipe.model.Asset;
import com.example.trustwipe.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
public class AgentController {

    @Autowired
    private AssetRepository assetRepository;

    // Stores pending commands for each agent: Map<AgentId, List<Map<String, Object>>>
    private static final Map<String, List<Map<String, Object>>> commandQueue = new ConcurrentHashMap<>();
    private static final Map<String, Long> activeAgents = new ConcurrentHashMap<>();
    private static final Map<String, List<Object>> fileCache = new ConcurrentHashMap<>();

    public void enqueueCommand(String agentId, String userEmail, Map<String, Object> command) {
        if ("LIST_FILES".equals(command.get("command"))) {
            fileCache.remove(agentId + ":" + userEmail);
        }
        commandQueue.computeIfAbsent(agentId, k -> Collections.synchronizedList(new ArrayList<>())).add(command);
    }

    @PostMapping("/report-files")
    public void reportFiles(@RequestParam String agentId, @RequestParam String userEmail, @RequestBody List<Object> files) {
        fileCache.put(agentId + ":" + userEmail, files);
    }

    @GetMapping("/get-files")
    public List<Object> getFiles(@RequestParam String agentId, @RequestParam String userEmail) {
        return fileCache.getOrDefault(agentId + ":" + userEmail, Collections.emptyList());
    }

    @PostMapping("/report-drives")
    public String reportDrives(@RequestParam String agentId, @RequestParam String userEmail, @RequestBody List<Asset> drives) {
        activeAgents.put(agentId, System.currentTimeMillis());
        
        // Register these drives as "Remote" assets in our DB
        for (Asset drive : drives) {
            String driveStatus = "CONNECTED (REMOTE)";
            String driveType = "REMOTE [" + agentId + "]";
            
            // Check if this drive is already in the DB for this user
            Optional<Asset> existing = assetRepository.findAll().stream()
                .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(drive.getName()) && userEmail.equals(a.getUserEmail()))
                .findFirst();

            if (existing.isEmpty()) {
                drive.setStatus(driveStatus);
                drive.setType(driveType);
                drive.setUserEmail(userEmail);
                assetRepository.save(drive);
            } else {
                Asset asset = existing.get();
                asset.setStatus(driveStatus);
                asset.setType(driveType);
                asset.setUserEmail(userEmail);
                assetRepository.save(asset);
            }
        }
        return "Drives Registered";
    }

    @GetMapping("/commands/{agentId}")
    public Map<String, Object> getCommands(@PathVariable String agentId) {
        activeAgents.put(agentId, System.currentTimeMillis());
        List<Map<String, Object>> commands = commandQueue.get(agentId);
        if (commands != null && !commands.isEmpty()) {
            return commands.remove(0);
        }
        return Collections.emptyMap();
    }

    @PostMapping("/command/{assetId}")
    public void sendCommand(@PathVariable String assetId, @RequestBody Map<String, Object> command) {
        // Find the agentId from the asset or just use a fixed ID for the demo
        // For partial wipe of multiple files, we might call this multiple times
        enqueueCommand("MAYANK-LAPTOP-01", "mayanklmao1@gmail.com", command);
    }

    @GetMapping("/active")
    public Set<String> listAgents() {
        return activeAgents.keySet();
    }
}
