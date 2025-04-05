package com.example.webdisk.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.webdisk.util.FilesNameSupplier;

/**
 * Service class for managing a cache of file names.
 * 
 * <p>This class provides methods to add, check, generate, find, and delete file names in the cache.
 * It ensures that each file name in the cache is unique.</p>
 * 
 * <p>The cache is implemented using a {@link HashSet} to store the file names.</p>
 * 
 * Methods provided:
 * <ul>
 *   <li>{@link #containsFile(String)} - Checks if a file name is present in the cache.</li>
 *   <li>{@link #putFile(String)} - Adds a file name to the cache.</li>
 *   <li>{@link #newFile()} - Generates a new unique file name and adds it to the cache.</li>
 *   <li>{@link #findFilesForPattern(String)} - Finds file names that match a given pattern.</li>
 *   <li>{@link #deleteFile(String)} - Deletes a file name from the cache.</li>
 *   <li>{@link #getSize()} - Returns the number of file names in the cache.</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * FilesCache filesCache = new FilesCache();
 * filesCache.putFile("example_txt");
 * boolean exists = filesCache.containsFile("example_txt");
 * String newFileName = filesCache.newFile();
 * String[] matchingFiles = filesCache.findFilesForPattern(".*\\_txt");
 * filesCache.deleteFile("example_txt");
 * int size = filesCache.getSize();
 * }
 * </pre>
 */
@Service
public class FilesCache {

    /**
     * A set that holds the names of the files in the cache.
     * This set ensures that each file name is unique within the cache.
     */
    private final Set<String> files = new HashSet<>();

    /**
     * An instance of FilesAccess used to interact with the file storage system.
     */
    private FilesAccess storage;

    /**
     * Constructs a new FilesCache instance with the specified storage.
     *
     * @param storage the FilesAccess instance used for file storage operations
     */
    public FilesCache(FilesAccess storage) {
        this.storage = storage;
    }

    /**
     * Initializes the cache by reading and storing all files from the storage.
     * This operation is intensive and its duration is measured for telemetry purposes.
     *
     * @return the time taken to initialize the cache, in milliseconds
     * @throws IOException if an I/O error occurs while reading the files
     */
    public long initCache() throws IOException {
        // Reading the entire cache is intensive and should be part of telemetry
        Instant start = Instant.now();
        storage.listFiles().forEach(this::putFile);
        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    /**
     * Checks if the cache contains a file with the specified name.
     *
     * @param fileName the name of the file to check for
     * @return true if the file is present in the cache, false otherwise
     */
    public boolean containsFile(String fileName) {
        return files.contains(fileName);
    }


    /**
     * Adds the specified file name to the cache.
     *
     * @param fileName the name of the file to be added to the cache
     * @return {@code true} if the file was successfully added to the cache,
     *         {@code false} otherwise
     */
    public boolean putFile(String fileName) {
        return files.add(fileName);
    }

    /**
     * Generates a new unique file name that does not already exist in the cache,
     * adds it to the cache, and returns the new file name.
     *
     * @return the newly generated unique file name
     */
    public String newFile() {
        final int MAX_ATTEMPTS = 100;

        Optional<String> newFileName = Stream.generate(new FilesNameSupplier())
                .limit(MAX_ATTEMPTS)
                .filter(fileName -> !files.contains(fileName))
                .limit(1)
                .findFirst();
        putFile(newFileName.orElseThrow(() -> new RuntimeException("Unable to generate a unique cache key")));
        return newFileName.get();
    }

    /**
     * Finds and returns an array of file names that match the given pattern.
     * 
     * @param pattern the regular expression pattern to match file names against
     * @return an array of file names that match the given pattern
     */
    public String[] findFilesForPattern(String pattern) {
        Pattern regexpPattern = Pattern.compile(pattern);
        return files.stream()
                .filter(file -> {
                    Matcher matcher = regexpPattern.matcher(file);
                    return matcher.find();
                })
                .toArray(String[]::new);
    }

    /**
     * Deletes a file from the cache.
     * 
     * @param fileName the name of the file to be deleted
     */
    public void deleteFile(String fileName) {
        if (files.contains(fileName)) {
            files.remove(fileName);
        }
    }


    /**
     * Checks if the given file name is valid.
     * A valid file name contains only alphanumeric characters, hyphens, and underscores,
     * and its length is between 1 and 64 characters inclusive.
     *
     * @param fileName the name of the file to be validated
     * @return true if the file name is valid, false otherwise
     */
    public boolean isValid(String fileName) {
        return fileName.matches("^[a-zA-Z0-9-_]{1,64}$");
    }

    /**
     * Returns the number of files in the cache.
     *
     * @return the size of the files cache
     */
    public int getSize() {
        return files.size();
    }
}
