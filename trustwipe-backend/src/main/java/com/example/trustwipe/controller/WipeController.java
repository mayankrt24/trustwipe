package com.example.trustwipe.controller;

import com.example.trustwipe.service.WipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wipe")
public class WipeController {

    @Autowired
    private WipeService wipeService;

    /**
     * Triggers a full NIST 3-pass wipe on an asset.
     */
    @PostMapping("/{assetId}")
    public void wipeAsset(@PathVariable String assetId, @RequestParam(required = false) String userEmail) {
        wipeService.fullWipe(assetId, userEmail);
    }

    /**
     * Triggers a secure wipe of ONLY the free space on a drive.
     */
    @PostMapping("/free-space/{assetId}")
    public void wipeFreeSpace(@PathVariable String assetId, @RequestParam(required = false) String userEmail) {
        wipeService.wipeFreeSpace(assetId, userEmail);
    }

    /**
     * Triggers a partial NIST 3-pass wipe on specific paths.
     */
    @PostMapping("/partial")
    public void partialWipe(@RequestBody PartialWipeRequest request) {
        wipeService.partialWipe(request.getAssetId(), request.getPaths(), request.getUserEmail());
    }

    /**
     * Returns the current wipe progress for an asset.
     */
    @GetMapping("/progress/{assetId}")
    public Map<String, Object> getWipeProgress(@PathVariable String assetId) {
        return wipeService.getWipeProgress(assetId);
    }
}
