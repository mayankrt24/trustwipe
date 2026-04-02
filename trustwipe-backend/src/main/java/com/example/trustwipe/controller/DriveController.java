package com.example.trustwipe.controller;

import com.example.trustwipe.dto.FileInfo;
import com.example.trustwipe.model.Asset;
import com.example.trustwipe.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/drives")
public class DriveController {

    @Autowired
    private DriveService driveService;

    @GetMapping("/scan")
    public List<Asset> scanDrives() {
        return driveService.scanAndRegisterDrives();
    }

    @GetMapping("/list-path")
    public List<FileInfo> listPath(@RequestParam String path) {
        System.out.println("DEBUG: Listing path requested: [" + path + "]");
        File folder = new File(path);
        List<FileInfo> result = new ArrayList<>();
        
        if (!folder.exists()) {
            System.out.println("DEBUG: Path does NOT exist: " + path);
            return result;
        }
        
        if (!folder.isDirectory()) {
            System.out.println("DEBUG: Path is NOT a directory: " + path);
            return result;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("DEBUG: listFiles() returned NULL for: " + path + " (likely permissions)");
            return result;
        }

        System.out.println("DEBUG: Found " + files.length + " items in " + path);
        for (File file : files) {
            result.add(new FileInfo(
                file.getName(),
                file.getAbsolutePath(),
                file.isDirectory(),
                file.isDirectory() ? 0 : file.length()
            ));
        }
        return result;
    }

    @GetMapping
    public List<Asset> getDrives() {
        return driveService.getAllRegisteredAssets();
    }
}
