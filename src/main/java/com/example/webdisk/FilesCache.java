package com.example.webdisk;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class FilesCache {

    private final Set<String> files = new HashSet<>();

    public boolean containsFile(String fileName) {
        return this.files.contains(fileName);
    }

    public void putFile(String fileName) {
        this.files.add(fileName);
    }

    public String newFile() {
        String newFileName = Stream.generate(new FilesNameSupplier())
                .filter(fileName -> !this.files.contains(fileName))
                .limit(1)
                .collect(Collectors.joining());
        this.putFile(newFileName);
        return newFileName;
    }

    public String[] findFilesForPattern(String pattern) {
        Pattern regexpPattern = Pattern.compile(pattern); // TODO: Mention case sensitive in docs
        return this.files.stream()
                .filter(file -> {
                    Matcher matcher = regexpPattern.matcher(file);
                    return matcher.find();
                })
                .toArray(String[]::new);
    }

    public int getSize() {
        return this.files.size();
    }
}
