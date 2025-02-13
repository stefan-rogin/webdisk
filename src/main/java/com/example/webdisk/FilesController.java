package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.webdisk.response.FilesSizeResponse;

import jakarta.annotation.PostConstruct;

@RestController
public class FilesController {

    private FilesCache cache;

    @PostConstruct
    public void initialize() {
        // TODO: with IoC, look into @Service
        this.cache = new FilesCache();
    }

    @GetMapping("/files/size")
    public FilesSizeResponse getStorageSize() {
        return new FilesSizeResponse(this.cache.getSize());
    }
}
