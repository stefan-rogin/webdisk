package com.example.webdisk;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;


@Service
public class FilesCache {

    private final Set<String> files = new HashSet<>();

    public boolean containsFile(String fileName) {
        return files.contains(fileName);
    }

    public boolean putFile(String fileName) {
        return files.add(fileName);
    }

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

    public boolean isValid(String fileName) {
        return fileName.matches("^[a-zA-Z0-9-_]{1,64}$");
    }

    public int getSize() {
        return files.size();
    }
}
