package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.webdisk.response.FilesSizeResponse;

@RestController
public class FilesController {

    @GetMapping("/files/size")
    public FilesSizeResponse getStorageSize() {
        return FilesSize.getSize();
    }
}
