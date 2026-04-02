package com.example.trustwipe.repository;

import com.example.trustwipe.model.WipeReport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WipeReportRepository extends MongoRepository<WipeReport, String> {
}
