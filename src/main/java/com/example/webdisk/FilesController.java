package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.webdisk.response.FilesPostFileResponse;
import com.example.webdisk.response.FilesSearchResponse;
import com.example.webdisk.response.FilesSizeResponse;

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
            this.storage.listFiles().forEach(fileName-> this.cache.putFile(fileName));
        } catch  (IOException e) {
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
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<FilesSearchResponse> getFilesSearch(@RequestParam String pattern) {
        return ResponseEntity.ok(new FilesSearchResponse(this.cache.findFilesForPattern(pattern)));
    }
    
    @PostMapping("/")
    public ResponseEntity<FilesPostFileResponse> postFile(@RequestBody MultipartFile content) {
        String newFileName = this.cache.newFile();
        try {
            this.storage.putFile(newFileName, content);
        } catch (IOException e) {
            // this.cache.delete(fileName);
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok(new FilesPostFileResponse(newFileName));
    }
    
}
