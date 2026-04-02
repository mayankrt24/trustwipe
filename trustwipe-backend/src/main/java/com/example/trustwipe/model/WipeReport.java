package com.example.trustwipe.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "wipeReports")
public class WipeReport {
    @Id
    private String id;
    private String assetId;
    private String wipeType; // FULL / PARTIAL
    private LocalDateTime timestamp;
    private long duration; // in milliseconds
    private int passes;
    private String finalStatus;
    private String verificationHash;
    private List<String> wipedFiles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getWipeType() {
        return wipeType;
    }

    public void setWipeType(String wipeType) {
        this.wipeType = wipeType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPasses() {
        return passes;
    }

    public void setPasses(int passes) {
        this.passes = passes;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getVerificationHash() {
        return verificationHash;
    }

    public void setVerificationHash(String verificationHash) {
        this.verificationHash = verificationHash;
    }

    public List<String> getWipedFiles() {
        return wipedFiles;
    }

    public void setWipedFiles(List<String> wipedFiles) {
        this.wipedFiles = wipedFiles;
    }
}
