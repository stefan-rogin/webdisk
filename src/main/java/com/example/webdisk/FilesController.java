package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.IOException;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.List;

import com.example.webdisk.response.FilesSizeResponse;

@RestController
public class FilesController {

    private FilesCache cache;

    @Value("${webdisk.path}")
    private String path;

    private List<String> listFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(this.path))) {
            return stream 
                .filter (file -> !Files.isDirectory(file))
                .map(file -> file.getFileName().toString())
                .filter(fileName -> fileName.matches("^[a-zA-Z0-9-_]{1,64}$"))
                .collect(Collectors.toList());
        }
    }

    @PostConstruct
    public void initialize() {
        // TODO: with IoC, look into @Service
        this.cache = new FilesCache();
        try {
            this.listFiles().forEach(f -> this.cache.putFile(f));
        } catch  (IOException eIoException) {
            // TODO: Handle
        }
    }

    @GetMapping("/files/size")
    public FilesSizeResponse getStorageSize() {
        return new FilesSizeResponse(this.cache.getSize());
    }
}
