package com.example.trustwipe.repository;

import com.example.trustwipe.model.Asset;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface AssetRepository extends MongoRepository<Asset, String> {
    Optional<Asset> findByNameIgnoreCase(String name);
    List<Asset> findByUserEmail(String userEmail);
    Optional<Asset> findByNameIgnoreCaseAndUserEmail(String name, String userEmail);
}
