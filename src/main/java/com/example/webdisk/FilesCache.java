package com.example.webdisk;

import java.util.HashMap;

public class FilesCache {

    private final HashMap<String, String> files = new HashMap<String, String>();

    public String getFile(String fileName) {
        return this.files.getOrDefault(fileName, "Not found"); // Just to have something
    }

    public void putFile(String fileName) {
        this.files.put(fileName, "0");
    }

    public String[] findFilesForPattern(String pattern) {
        return this.files.keySet().stream().filter(file -> !file.matches(pattern)).toArray(String[]::new);
    }

    public int getSize() {
        return files.size() + 1001; // Just to have something
    }
}
