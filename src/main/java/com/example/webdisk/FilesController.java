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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

/**
 * The FilesController class handles HTTP requests related to file operations
 * such as retrieving file size, fetching files, searching files, uploading new
 * files, updating existing files, and deleting files. It uses a cache to store file
 * metadata and a storage service to perform actual file operations.
 * 
 * The controller provides the following endpoints:
 * <ul>
 * <li>GET /files/{fileName} - Retrieves a file by its name.</li>
 * <li>HEAD /files/{fileName} - Check if a file exists, whitout getting its content.</li>
 * <li>POST /files/ - Uploads a new file.</li>
 * <li>PUT /files/{fileName} - Updates an existing file.</li>
 * <li>DELETE /files/{fileName} - Deletes a file by its name.</li>
 * <li>GET /files/search - Searches for files matching a given pattern.</li>
 * <li>GET /files/size - Returns the total number of files stored by the application.</li>
 * <li>GET /files/restricted - Demo endpoint for security implementation.</li>
 * </ul>
 * 
 * The controller uses the following dependencies:
 * <ul>
 * <li>FilesCache - A cache for storing file metadata.</li>
 * <li>FilesAccess - A service for performing file operations.</li>
 * <li>Logger - For logging operations and errors.</li>
 * </ul>
 * 
 * <p>
 * The controller initializes the cache with existing filenames from storage
 * when the application starts.
 * </p>
 */
@RestController
@RequestMapping("/files")
@Tag(name = "WebDisk")
public class FilesController {

    private FilesCache cache;
    private FilesAccess storage;

    private static final Logger logger = LoggerFactory.getLogger(FilesController.class);
    private static final String LOG_WEB_FORMAT = "@Requst:{} {}";

    /**
     * Constructs a new FilesController with the specified cache and storage.
     *
     * @param cache   the cache to be used by this controller
     * @param storage the storage to be used by this controller
     */
    public FilesController(FilesCache cache, FilesAccess storage) {
        this.cache = cache;
        this.storage = storage;
    }

    /**
     * Initializes the FilesController after its construction by reading file names from the 
     * storage and adding them to the cache.
     */
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing cache from path: {}", storage.getPath());
            
            // Reading the entire cache is intensive and should be part of telemetry
            Instant start = Instant.now();
            storage.listFiles().forEach(fileName -> cache.putFile(fileName));
            Instant end = Instant.now();

            logger.info("Cache initialized, took @CacheInit:{} ms",
                    Duration.between(start, end).toMillis());
            logger.info("Cache size @CacheSize:{}", cache.getSize());
        } catch (IOException e) {
            // The app will not start if the storage location is inaccessible
            logger.error("Unable to read from storage location: {}. @Cause:{}",
                    storage.getPath(), e.getMessage());
            throw new RuntimeException("Storage inaccessible, stopping.");
        }
    }

    /**
     * Handles the HTTP GET request to obtain the size of storage in number of files.
     * <p>
     * Returns the number of files encapsulated in a {@link FilesSizeResponse} object.
     * </p>
     *
     * @param request the {@link HttpServletRequest} object that contains the
     *                request the client has made to the servlet
     * @return a {@link ResponseEntity} containing the {@link FilesSizeResponse}
     *         with the number of files
     */
    @Operation(summary = "Storage size", description = "Returns the total number of files stored by the application")
    @ApiResponses(@ApiResponse(responseCode = "200", content = {
            @Content(schema = @Schema(implementation = FilesSizeResponse.class), mediaType = "application/json")
    }))
    @GetMapping("/size")
    public ResponseEntity<FilesSizeResponse> getFilesSize(HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(new FilesSizeResponse(cache.getSize()));
    }

    /**
     * Handles HTTP GET and HEAD requests to retrieve a file by its name.
     *
     * @param fileName the name of the file to retrieve
     * @param request  the HttpServletRequest object containing the request details
     * @return a ResponseEntity containing the file as an InputStreamResource if found,
     *         or a 404 Not Found status if the file does not exist in the cache,
     *         or a 500 Internal Server Error status if an error occurs while reading the file
     */
    @Operation(summary = "Download file", description = "Retrieve a stored file, identified by its name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", 
                description = "File download stream for GET as attachemnt, no content for HEAD",
                content={ @Content(schema = @Schema(implementation = Void.class)) }),
            @ApiResponse(responseCode = "404", description = "Not Found status if the file does not exist in the cache",
                content={ @Content(schema = @Schema(implementation = Void.class)) })
    })
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
            logger.error(LOG_WEB_FORMAT + ": Unable to read file {}. @Cause:{}",
                    request.getMethod(), request.getRequestURI(), fileName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    
    /**
     * Handles the HTTP POST request to upload a new file.
     *
     * @param file the file content to be uploaded
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing the response with the new file name
     * @throws IOException if an I/O error occurs during file upload
     */
    @Operation(summary = "Add new file", description = "Upload a file for the first time")
    @ApiResponses(@ApiResponse(responseCode = "200", content = {
            @Content(schema = @Schema(implementation = FilesPostFileResponse.class), 
            mediaType = "application/json")
    }))
    @PostMapping("/upload")
    public ResponseEntity<FilesPostFileResponse> postFile(
            @RequestParam MultipartFile file,
            HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());

        String newFileName = cache.newFile();
        try {
            storage.putFile(newFileName, file);
        } catch (IOException e) {
            // Revert incomplete create
            cache.deleteFile(newFileName);
            logger.error(LOG_WEB_FORMAT + ": Unable to post new file. @Cause:{}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(new FilesPostFileResponse(newFileName));
    }

    /**
     * Handles the upload or update of a file. If the file already exists, its content will be replaced.
     *
     * @param fileName the name of the file to be uploaded or updated
     * @param content the content of the file to be uploaded
     * @param request the HTTP request object
     * @return a ResponseEntity with the appropriate HTTP status code and message
     *         - 200 OK if the file is successfully uploaded or updated
     *         - 400 Bad Request if the filename is invalid
     *         - 404 Not Found if the file to be replaced does not exist
     *         - 500 Internal Server Error if an error occurs during the file operation
     */
    @Operation(summary = "Upload or update", 
        description = "Upload a file, replacing current content if it already exists")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK if the file is successfully uploaded or updated"),
        @ApiResponse(responseCode = "400", description = "Bad Request if the filename is invalid"),
        @ApiResponse(responseCode = "404", description = "Not Found if the file to be replaced does not exist"),
    })
    @PutMapping("/{fileName}")
    public ResponseEntity<String> putFile(
            @PathVariable String fileName,
            @RequestBody MultipartFile content,
            HttpServletRequest request) {
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
                logger.error(LOG_WEB_FORMAT + ": Unable to clear update file, ", 
                        request.getMethod(), request.getRequestURI(), e.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        }

        // Verify name before accepting operation
        if (!cache.isValid(fileName)) {
            return ResponseEntity.status(400).body("Invalid filename");
        }

        try {
            cache.putFile(fileName);
            storage.putFile(fileName, content);
        } catch (IOException e) {
            // Revert incomplete create
            cache.deleteFile(fileName);
            logger.error(LOG_WEB_FORMAT + ": Unable to put file, ", 
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("");
    }

    /**
     * Deletes a file with the given file name.
     *
     * @param fileName the name of the file to be deleted
     * @param request the HTTP request object
     * @return a ResponseEntity with status 200 (OK) if the file was successfully deleted,
     *         status 404 (Not Found) if the file does not exist,
     *         or status 500 (Internal Server Error) if an error occurred during deletion
     */
    @Operation(summary = "Delete file", description = "Deletes a file with the given file name.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File deleted", content = @Content),
        @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
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
            logger.error(LOG_WEB_FORMAT + ": Unable to delete file, ", 
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok("");
    }


    /**
     * Handles GET requests to search for files matching a given pattern.
     *
     * @param pattern the search pattern to match files against
     * @param request the HttpServletRequest object containing the request details
     * @return a ResponseEntity containing a FilesSearchResponse with the search
     *         results
     */
    @Operation(summary = "Search files", description = "Use a Regexp pattern to search for files")
    @ApiResponses(@ApiResponse(responseCode = "200", content = {
            @Content(schema = @Schema(implementation = FilesSearchResponse.class), mediaType = "application/json") }))
    @GetMapping("/search")
    public ResponseEntity<FilesSearchResponse> getFilesSearch(@RequestParam String pattern,
            HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI() + request.getQueryString());

        Instant start = Instant.now();
        String[] results = cache.findFilesForPattern(pattern);
        Instant end = Instant.now();

        logger.info("Search for {} took @Search:{} ms", pattern, Duration.between(start, end).toMillis());
        return ResponseEntity.ok(new FilesSearchResponse(results));
    }

    /**
     * Handles GET requests to the /restricted endpoint.
     * Demo endpoint for basic implementation of security, with preauthentication. For granting access,
     * the presence of Authorization header is required, with a Bearer token of any value.
     * Its validity with the authentication system is out of scope and was mocked.
     * 
     * @param request the HttpServletRequest object containing the request details
     * @return a ResponseEntity with a message indicating authorization
     */
    @Operation(summary = "Get restricted resource", 
            description = "Demo endpoint for basic implementation of security.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Access granted", 
                content = @Content(examples = @ExampleObject(value = "Authorized"))),
        @ApiResponse(responseCode = "403", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/restricted")
    public ResponseEntity<String> getFilesRestricted(HttpServletRequest request) {
        logger.info(LOG_WEB_FORMAT, request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok("Authorized");
    }

}
