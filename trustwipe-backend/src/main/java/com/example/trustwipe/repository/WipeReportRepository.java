package com.example.trustwipe.repository;

import com.example.trustwipe.model.WipeReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface WipeReportRepository extends MongoRepository<WipeReport, String> {
    List<WipeReport> findByUserEmail(String userEmail);
}
