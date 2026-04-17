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

    // Stores pending commands for each agent: Map<AgentId, Command>
    private static final Map<String, Map<String, Object>> commandQueue = new ConcurrentHashMap<>();
    private static final Map<String, Long> activeAgents = new ConcurrentHashMap<>();

    @PostMapping("/report-drives")
    public String reportDrives(@RequestParam String agentId, @RequestBody List<Asset> drives) {
        activeAgents.put(agentId, System.currentTimeMillis());
        
        // Register these drives as "Remote" assets in our DB
        for (Asset drive : drives) {
            drive.setStatus("CONNECTED (REMOTE)");
            drive.setType("REMOTE [" + agentId + "]");
            
            // Check if this drive is already in the DB
            Optional<Asset> existing = assetRepository.findAll().stream()
                .filter(a -> a.getName() != null && a.getName().equals(drive.getName()))
                .findFirst();

            if (existing.isEmpty()) {
                assetRepository.save(drive);
            }
        }
        return "Drives Registered";
    }

    @GetMapping("/commands/{agentId}")
    public Map<String, Object> getCommands(@PathVariable String agentId) {
        activeAgents.put(agentId, System.currentTimeMillis());
        return commandQueue.remove(agentId);
    }

    @PostMapping("/command/{assetId}")
    public void sendCommand(@PathVariable String assetId, @RequestBody Map<String, Object> command) {
        // Find the agentId from the asset or just use a fixed ID for the demo
        commandQueue.put("MAYANK-PC", command);
    }

    @GetMapping("/active")
    public Set<String> listAgents() {
        return activeAgents.keySet();
    }
}
