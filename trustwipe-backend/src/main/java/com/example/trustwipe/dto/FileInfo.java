package com.example.trustwipe.dto;

/**
 * DTO for representing a file or directory on the system.
 */
public class FileInfo {
    private String name;
    private String path;
    private boolean isDirectory;
    private long size;

    public FileInfo() {}

    public FileInfo(String name, String path, boolean isDirectory, long size) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isIsDirectory() { return isDirectory; }
    public void setIsDirectory(boolean isDirectory) { this.isDirectory = isDirectory; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
