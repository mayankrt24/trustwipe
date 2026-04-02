package com.example.trustwipe.repository;

import com.example.trustwipe.model.Asset;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface AssetRepository extends MongoRepository<Asset, String> {
    Optional<Asset> findByNameIgnoreCase(String name);
}
