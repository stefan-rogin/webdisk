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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
public class FilesController {

    @Autowired
    private FilesCache cache;

    @Autowired
    private FilesAccess storage;

    private static final Logger logger = LoggerFactory.getLogger(FilesController.class);

    @PostConstruct
    public void initialize() {
        // TODO: mock IO for tests
        // How long does it take?
        try {
            this.storage.listFiles().forEach(fileName-> this.cache.putFile(fileName));
        } catch  (IOException e) {
            logger.error("Unable to read from storage location", e);
        }
    }

    @GetMapping("/size")
    public ResponseEntity<FilesSizeResponse> getFilesSize(HttpServletRequest request) {
        logger.info("{} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(new FilesSizeResponse(this.cache.getSize()));
    }
    
    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFileForFileName(@PathVariable String fileName, HttpServletRequest request) {
        logger.info("{} {}", request.getMethod(), request.getRequestURI());

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
            logger.error("{} {}: Unable to read file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<FilesSearchResponse> getFilesSearch(@RequestParam String pattern, HttpServletRequest request) {
        logger.info("{} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(new FilesSearchResponse(this.cache.findFilesForPattern(pattern)));
    }
    
    @PostMapping("/")
    public ResponseEntity<FilesPostFileResponse> postFile(@RequestBody MultipartFile content, HttpServletRequest request) {
        logger.info("{} {}", request.getMethod(), request.getRequestURI());

        String newFileName = this.cache.newFile();
        try {
            this.storage.putFile(newFileName, content);
        } catch (IOException e) {
            // this.cache.delete(fileName);
            logger.error("{} {}: Unable to post new file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok(new FilesPostFileResponse(newFileName));
    }
    
    @GetMapping("/restricted")
    public ResponseEntity<String> getFilesRestricted(HttpServletRequest request) {
        logger.info("{} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok("Authorized");
    }
    
}
