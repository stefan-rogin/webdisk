package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.webdisk.response.FilesPostFileResponse;
import com.example.webdisk.response.FilesSearchResponse;
import com.example.webdisk.response.FilesSizeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
@Tag(name = "WebDisk")
public class FilesController {

    private FilesCache cache;
    private FilesAccess storage;

    private static final Logger logger = LoggerFactory.getLogger(FilesController.class);
    private static final String LOG_WEB = "{} {}";

    public FilesController(FilesCache cache, FilesAccess storage) {
        this.cache = cache;
        this.storage = storage;
    }

    /**
     * Populate the cache with the existing filenames from storage when the application starts.
     * Performs IO operations. 
     */
    @PostConstruct
    public void initialize() {
        // TODO: mock IO for tests
        try {
            this.storage.listFiles().forEach(fileName-> this.cache.putFile(fileName));
        } catch  (IOException e) {
            // The cache will be empty if the storage location is unaccessible
            logger.error("Unable to read from storage location", e.getMessage());
        }
    }

    /**
     * Get the total number of files stored, as present in cache.
     * It does not perform IO operations.
     * @param request 
     * @return { size: <int> }
     */
    @Operation(summary = "Storage size", description = "Returns the total number of files stored by the application")
    @GetMapping("/size")
    public ResponseEntity<FilesSizeResponse> getFilesSize(HttpServletRequest request) {
        logger.info(LOG_WEB, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(new FilesSizeResponse(this.cache.getSize()));
    }
    
    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFileForFileName(@PathVariable String fileName, HttpServletRequest request) {
        logger.info(LOG_WEB, request.getMethod(), request.getRequestURI());

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
            logger.error(LOG_WEB + ": Unable to read file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<FilesSearchResponse> getFilesSearch(@RequestParam String pattern, HttpServletRequest request) {
        logger.info(LOG_WEB, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(new FilesSearchResponse(this.cache.findFilesForPattern(pattern)));
    }
    
    @PostMapping("/")
    public ResponseEntity<FilesPostFileResponse> postFile(@RequestBody MultipartFile content, HttpServletRequest request) {
        logger.info(LOG_WEB, request.getMethod(), request.getRequestURI());

        String newFileName = this.cache.newFile();
        try {
            this.storage.putFile(newFileName, content);
        } catch (IOException e) {
            // this.cache.delete(fileName);
            logger.error(LOG_WEB + ": Unable to post new file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok(new FilesPostFileResponse(newFileName));
    }
    
    @GetMapping("/restricted")
    public ResponseEntity<String> getFilesRestricted(HttpServletRequest request) {
        logger.info(LOG_WEB, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok("Authorized");
    }
    
}
