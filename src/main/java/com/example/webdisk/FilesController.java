package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.webdisk.response.FilesPostFileResponse;
import com.example.webdisk.response.FilesSizeResponse;

import java.io.IOException;
import java.io.InputStream;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
public class FilesController {

    private FilesCache cache;
    private FilesAccess storage;

    public FilesController(FilesCache cache, FilesAccess storage) {
        this.cache = cache;
        this.storage = storage;
    }

    @PostConstruct
    public void initialize() {
        try {
            storage.listFiles().forEach(fileName -> cache.putFile(fileName));

        } catch (IOException e) {}
    }

    @GetMapping("/size")
    public ResponseEntity<FilesSizeResponse> getFilesSize(HttpServletRequest request) {
        return ResponseEntity.ok(new FilesSizeResponse(cache.getSize()));
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFileForFileName(
            @PathVariable String fileName,
            HttpServletRequest request) {

        if (!cache.containsFile(fileName)) {
            return ResponseEntity.notFound().build();
        }

        if ("HEAD".equals(request.getMethod())) {
            return ResponseEntity.ok().build();
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
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<FilesPostFileResponse> postFile(
            @RequestBody MultipartFile content,
            HttpServletRequest request) {

        String newFileName = cache.newFile();
        try {
            storage.putFile(newFileName, content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(new FilesPostFileResponse(newFileName));
    }

    @PutMapping("/{fileName}")
    public ResponseEntity<String> putFile(
            @PathVariable String fileName,
            @RequestBody MultipartFile content,
            HttpServletRequest request) {

        // Verify name before accepting operation
        if (!cache.isValid(fileName)) {
            return ResponseEntity.status(400).body("Invalid filename");
        }

        try {
            cache.putFile(fileName);
            storage.putFile(fileName, content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("");
    }

    @GetMapping("/restricted")
    public ResponseEntity<String> getFilesRestricted(HttpServletRequest request) {
        return ResponseEntity.ok("Authorized");
    }

}
