package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

import com.example.webdisk.response.FilesSizeResponse;

@RestController
public class FilesController {

    @Autowired
    private FilesCache cache;
    @Autowired
    private FilesAccess storage;



    // Move to own class

    @PostConstruct
    public void initialize() {
        // TODO: mock IO for tests
        try {
            this.storage.listFiles().forEach(f -> this.cache.putFile(f));
        } catch  (Exception ioException) {
            // TODO: Handle
        }
    }

    @GetMapping("/files/size")
    public FilesSizeResponse getStorageSize() {
        return new FilesSizeResponse(this.cache.getSize());
    }

    @PostMapping("/files/generate")
    public void generate() {
        final int RECORDS = 100;
        for (int i = 0; i < RECORDS; i++) {
            this.cache.putFile(FilesSample.generateFileName());
        }

    }
    
}
