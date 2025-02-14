package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;

import com.example.webdisk.response.FilesSizeResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/files")
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

    @GetMapping("/size")
    public ResponseEntity<FilesSizeResponse> getFilesSize() {
        return ResponseEntity.ok(new FilesSizeResponse(this.cache.getSize()));
    }
    
    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFileForFileName(@PathVariable String fileName) {
        if (!this.cache.containsFile(fileName)) {
            return ResponseEntity.status(404).build();
        }
        try {
            InputStream fileStream = storage.getFile(fileName);
            InputStreamResource resource = new InputStreamResource(fileStream);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public String[] getFilesSearch(@RequestParam String pattern) {
        return this.cache.findFilesForPattern(pattern);
    }
    
    @PostMapping("/")
    public String postFile(@RequestBody String content) {
        String newFileName = this.cache.newFile();
        try {
            this.storage.putFile(newFileName, content.getBytes());
        } catch (IOException exception) {
            // this.cache.remove(newFileName);
            // TODO: Handle it
        }
        return newFileName;
    }
    
}
