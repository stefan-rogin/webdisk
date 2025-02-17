package com.example.webdisk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilesAccess {

    private String path;

    @Value("${webdisk.path}")
    public void setPath(String path) {
        // Sanitize configured path
        this.path = path.endsWith("/") ? path : path + "/";
    } 

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
        return Files.newInputStream(getPathForFileName(fileName)); 
    }

    public void putFile(String fileName, MultipartFile content) throws IOException {
        Files.copy(content.getInputStream(), getPathForFileName(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteFile(String fileName) throws IOException {
        Files.delete(getPathForFileName(fileName));
    }

    public String getPath() {
        return path;
    }

    private Path getPathForFileName(String fileName) {
        return Paths.get(path + fileName);
    }

}
