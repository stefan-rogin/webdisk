package com.example.webdisk;

import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FilesAccess {

    @Value("${webdisk.path}")
    private String path;

    public List<String> listFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(this.path))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(file -> file.getFileName().toString())
                    .filter(fileName -> fileName.matches("^[a-zA-Z0-9-_]{1,64}$"))
                    .collect(Collectors.toList());
        }
    }

    public byte[] getFile(String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(this.path + "/" + fileName)); 
    }

    public void putFile(String fileName, byte[] content) throws IOException {
        try (FileOutputStream file = new FileOutputStream(this.path + "/" + fileName)) {
            file.write(content);
        }
    }

}
