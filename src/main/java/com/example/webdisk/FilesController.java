package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;


import jakarta.annotation.PostConstruct;

import com.example.webdisk.response.FilesSizeResponse;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class FilesController {

    @Autowired
    private FilesCache cache;

    @Autowired
    private FilesAccess storage;

    @PostConstruct
    public void initialize() {
        // TODO: mock IO for tests
        // How long does it take?
        try {
            this.storage.listFiles().forEach(f -> this.cache.putFile(f));
        } catch  (Exception ioException) {
            // TODO: Handle
        }
    }

    @GetMapping("/files/size")
    public FilesSizeResponse getFilesSize() {
        return new FilesSizeResponse(this.cache.getSize());
    }

    @PostMapping("/files/inflate")
    public void postFilesInflate() {
        final int RECORDS = 100;
        for (int i = 0; i < RECORDS; i++) {
            this.cache.putFile(FilesCache.generateFileName());
        }
    }
    
    @GetMapping("/files/{fileName}")
    public String getFileForFileName(@PathVariable String fileName) {
        if (!this.cache.containsFile(fileName)) {
            return "404";
        }
        try {
            return new String(storage.getFile(fileName));
        } catch (Exception ex) {
            // TODO: Handle it
        }
        return new String();
    }

    @GetMapping("/files/search")
    public String[] getFilesSearch(@RequestParam String pattern) {
        return this.cache.findFilesForPattern(pattern);
    }
    
    @PostMapping("/files")
    public String postFile(@RequestBody String content) {
        boolean newFileNameFound = false;
        String newFileName = new String();
        while (!newFileNameFound) {
            String newFileNameTry = FilesCache.generateFileName();
            if (!this.cache.containsFile(newFileNameTry)) {
                newFileName = newFileNameTry;
                newFileNameFound = true;
            }
        }
        try {
            this.storage.putFile(newFileName, content.getBytes());
        } catch (Exception exception) {
            // TODO: Handle it
        }
        return newFileName;
    }
    
}
