package com.example.webdisk;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilesAccess {

    @Value("${webdisk.path}")
    private String path;

    public List<String> listFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(file -> file.getFileName().toString())
                    .filter(fileName -> fileName.matches("^[a-zA-Z0-9-_]{1,64}$"))
                    .toList();
        }
    }

    public InputStream getFile(String fileName) throws IOException {
        return Files.newInputStream(Paths.get(path + "/" + fileName)); 
    }

    public void putFile(String fileName, MultipartFile content) throws IOException {
        Files.copy(content.getInputStream(), Paths.get(path + "/" + fileName));
    }

    public String getPath() {
        return path;
    }

}
