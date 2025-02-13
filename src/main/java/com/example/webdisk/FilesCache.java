package com.example.webdisk;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class FilesCache {

    private final Set<String> files = new HashSet<String>();

    public boolean hasFile(String fileName) {
        return this.files.contains(fileName);
    }

    public void putFile(String fileName) {
        this.files.add(fileName);
    }

    public String[] findFilesForPattern(String pattern) {
        return this.files.stream()
            .filter(file -> file.matches(pattern)).toArray(String[]::new);
    }

    public int getSize() {
        return this.files.size();
    }
}
