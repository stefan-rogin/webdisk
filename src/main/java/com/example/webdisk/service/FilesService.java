package com.example.webdisk.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
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
 * 
 * <ul>
 * <li>listFiles(): Lists all files in the directory that match a specific regex pattern.</li>
 * <li>getFile(String fileName): Retrieves an InputStream for the specified file.</li>
 * <li>getFileAsync(String fileName): Asynchronously retrieves an InputStream for the specified file.</li>
 * <li>putFile(String fileName, MultipartFile file): Stores the provided file with the specified file name.</li>
 * <li>putFileAsync(String fileName, MultipartFile file): Asynchronously uploads a file to the server.</li>
 * <li>deleteFile(String fileName): Deletes the file with the specified name.</li>
 * <li>getPath(): Retrieves the base directory path.</li>
 * </ul>
 * 
 * Private Methods:
 * <ul>
 * <li>getPathForFileName(String fileName): Constructs a Path object by appending the given file name to the base path.</li>
 * </ul>
 * 
 * Exceptions:
 * <ul>
 * <li>IOException: Thrown if an I/O error occurs during file operations.</li>
 * <li>RuntimeException: Thrown if an I/O error occurs during asynchronous file operations.</li>
 * </ul>
 * 
 * Annotations:
 * <ul>
 * <li>@Service: Indicates that this class is a Spring service component.</li>
 * <li>@Value: Injects the value of the "webdisk.path" property.</li>
 * <li>@Async: Indicates that the method should be executed asynchronously.</li>
 * </ul>
 */
@Service
public class FilesService {

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
