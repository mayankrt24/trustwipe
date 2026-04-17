package com.example.trustwipe.controller;

import com.example.trustwipe.model.WipeReport;
import com.example.trustwipe.repository.WipeReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private WipeReportRepository wipeReportRepository;

    @GetMapping
    public List<WipeReport> getAllReports(@RequestParam String userEmail) {
        return wipeReportRepository.findByUserEmail(userEmail);
    }
}
