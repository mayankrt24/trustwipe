package com.example.trustwipe.controller;

import com.example.trustwipe.model.Asset;
import com.example.trustwipe.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetRepository assetRepository;

    @PostMapping
    public Asset createAsset(@RequestBody Asset asset) {
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name cannot be null or empty");
        }
        return assetRepository.save(asset);
    }

    @GetMapping
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @GetMapping("/{id}")
    public Asset getAssetById(@PathVariable String id) {
        return assetRepository.findById(id).orElse(null);
    }
}
