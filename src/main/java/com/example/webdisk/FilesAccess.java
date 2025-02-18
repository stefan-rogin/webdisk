package com.example.webdisk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class for managing file access operations.
 * 
 * This class provides methods to list, retrieve, store, and delete files in a specified directory.
 * It also supports asynchronous operations for file retrieval and storage.
 * 
 * The base directory path is configured via the "webdisk.path" property.
 * 
 * Methods:
 * - listFiles(): Lists all files in the directory that match a specific regex pattern.
 * - getFile(String fileName): Retrieves an InputStream for the specified file.
 * - getFileAsync(String fileName): Asynchronously retrieves an InputStream for the specified file.
 * - putFile(String fileName, MultipartFile file): Stores the provided file with the specified file name.
 * - putFileAsync(String fileName, MultipartFile file): Asynchronously uploads a file to the server.
 * - deleteFile(String fileName): Deletes the file with the specified name.
 * - getPath(): Retrieves the base directory path.
 * 
 * Private Methods:
 * - getPathForFileName(String fileName): Constructs a Path object by appending the given file name to the base path.
 * 
 * Exceptions:
 * - IOException: Thrown if an I/O error occurs during file operations.
 * - RuntimeException: Thrown if an I/O error occurs during asynchronous file operations.
 * 
 * Annotations:
 * - @Service: Indicates that this class is a Spring service component.
 * - @Value: Injects the value of the "webdisk.path" property.
 * - @Async: Indicates that the method should be executed asynchronously.
 */
@Service
public class FilesAccess {

    private String path;

    /**
     * Sets the path for the webdisk. If the provided path does not end with a 
     * forward slash, it appends one to ensure the path is correctly formatted.
     *
     * @param path the path to be set for the webdisk, typically provided via 
     *             the 'webdisk.path' property.
     */
    @Value("${webdisk.path}")
    public void setPath(String path) {
        this.path = path.endsWith("/") ? path : path + "/";
    } 

    /**
     * Lists all files in the directory specified by the path.
     * 
     * @return a list of file names that are not directories and match the regex pattern "^[a-zA-Z0-9-_]{1,64}$".
     * @throws IOException if an I/O error occurs when accessing the directory.
     */
    public List<String> listFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(file -> file.getFileName().toString())
                    .filter(fileName -> fileName.matches("^[a-zA-Z0-9-_]{1,64}$"))
                    .toList();
        }
    }

    /**
     * Retrieves an InputStream for the specified file.
     *
     * @param fileName the name of the file to retrieve
     * @return an InputStream for the specified file
     * @throws IOException if an I/O error occurs
     */
    public InputStream getFile(String fileName) throws IOException {
        return Files.newInputStream(getPathForFileName(fileName)); 
    }

    /**
     * Asynchronously retrieves an InputStream for the specified file.
     *
     * @param fileName the name of the file to retrieve
     * @return a CompletableFuture containing the InputStream of the file
     * @throws RuntimeException if an I/O error occurs
     */
    @Async("taskExecutor")
    public CompletableFuture<InputStream> getFileAsync(String fileName) {
        try {
            InputStream inputStream = Files.newInputStream(getPathForFileName(fileName));
            return CompletableFuture.completedFuture(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores the provided file with the specified file name.
     *
     * @param fileName the name to be assigned to the stored file
     * @param file the file to be stored
     * @throws IOException if an I/O error occurs during file storage
     */
    public void putFile(String fileName, MultipartFile file) throws IOException {
        Files.copy(file.getInputStream(), getPathForFileName(fileName), 
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Asynchronously uploads a file to the server.
     *
     * @param fileName the name of the file to be uploaded
     * @param file the MultipartFile object containing the file data
     * @return a CompletableFuture representing the asynchronous operation
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> putFileAsync(String fileName, MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), getPathForFileName(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the file with the specified name.
     *
     * @param fileName the name of the file to be deleted
     * @throws IOException if an I/O error occurs or the file does not exist
     */
    public void deleteFile(String fileName) throws IOException {
        Files.delete(getPathForFileName(fileName));
    }

    /**
     * Retrieves the path of the file.
     *
     * @return the path of the file as a String
     */
    public String getPath() {
        return path;
    }

    /**
     * Constructs a Path object by appending the given file name to the base path.
     *
     * @param fileName the name of the file to be appended to the base path
     * @return a Path object representing the full path to the file
     */
    private Path getPathForFileName(String fileName) {
        return Paths.get(path + fileName);
    }

}
