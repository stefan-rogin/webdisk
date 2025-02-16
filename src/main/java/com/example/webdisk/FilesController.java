package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

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
    private static final String LOG_WEB_FORMAT = "{} {}";

    public FilesController(FilesCache cache, FilesAccess storage) {
        this.cache = cache;
        this.storage = storage;
    }

    /**
     * Populate the cache with the existing filenames from storage when the
     * application starts.
     * Performs IO operations.
     */
    @PostConstruct
    public void initialize() {
        // TODO: mock IO for tests
        initCache();
    }

    protected void initCache() {
        try {
            logger.info("Initializing cache from path: {}", storage.getPath());

            Instant start = Instant.now();
            storage.listFiles().forEach(fileName -> cache.putFile(fileName));
            Instant end = Instant.now();

            logger.info("Cache initialized, took @CacheInit:{} ms", Duration.between(start, end).toMillis());
            logger.info("Cache size @CacheSize:{}", cache.getSize());
        } catch (IOException e) {
            // The app will start with an empty cache if the storage location is
            // unaccessible
            logger.error("Unable to read from storage location: {}", storage.getPath(), e.getMessage());
        }
    }

    /**
     * Handles the HTTP GET request to obtain the size of cached files.
     * <p>
     * This method logs the incoming request method and URI, and returns the size of the 
     * cached files encapsulated in a {@link FilesSizeResponse} object.
     * </p>
     *
     * @param request the {@link HttpServletRequest} object that contains the
     *          request the client has made to the servlet
     * @return a {@link ResponseEntity} containing the {@link FilesSizeResponse}
     *          with the size of the cached files
     */
    @Operation(summary = "Storage size", description = "Returns the total number of files stored by the application")
    @GetMapping("/size")
    public ResponseEntity<FilesSizeResponse> getFilesSize(HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(new FilesSizeResponse(cache.getSize()));
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFileForFileName(
            @PathVariable String fileName,
            HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());

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
            logger.error(LOG_WEB_FORMAT + ": Unable to read file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<FilesSearchResponse> getFilesSearch(@RequestParam String pattern,
            HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());
        
        Instant start = Instant.now();
        String[] results = cache.findFilesForPattern(pattern);
        Instant end = Instant.now();
        
        logger.info("Search for {} took @Search:{} ms", pattern, Duration.between(start, end).toMillis());        
        return ResponseEntity.ok(new FilesSearchResponse(results));
    }

    @PostMapping("/")
    public ResponseEntity<FilesPostFileResponse> postFile(
            @RequestBody MultipartFile content,
            HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());

        String newFileName = cache.newFile();
        try {
            storage.putFile(newFileName, content);
        } catch (IOException e) {
            // Revert incomplete create
            cache.deleteFile(newFileName);
            logger.error(LOG_WEB_FORMAT + ": Unable to post new file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(new FilesPostFileResponse(newFileName));
    }

    @PutMapping("/{fileName}")
    public ResponseEntity<String> putFile(
            @PathVariable String fileName,
            @RequestBody MultipartFile content,
            HttpServletRequest request
    ) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());
        
        // Clean if necessary
        if (cache.containsFile(fileName)) {
            try {
                cache.deleteFile(fileName);
                storage.deleteFile(fileName);
            } catch (NoSuchElementException e) {
                return ResponseEntity.notFound().build();
            } catch (IOException e) {
                // Revert incomplete delete
                cache.putFile(fileName);
                return ResponseEntity.internalServerError().build();
            }
        }

        try {
            cache.putFile(fileName);
            storage.putFile(fileName, content);
        } catch (IOException e) {
            // Revert incomplete create
            cache.deleteFile(fileName);
            logger.error(LOG_WEB_FORMAT + ": Unable to post new file", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("");
    }

    @GetMapping("/restricted")
    public ResponseEntity<String> getFilesRestricted(HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok("Authorized");
    }

    @DeleteMapping("/{fileName}") 
    public ResponseEntity<String> deleteFile(@PathVariable String fileName, HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI() + request.getQueryString());
        
        if (!cache.containsFile(fileName)) {
            return ResponseEntity.notFound().build();
        }

        cache.deleteFile(fileName);
        try {
            storage.deleteFile(fileName);
        } catch (IOException e) {
            // Revert incomplete delete
            cache.putFile(fileName);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok("");
    }

}
