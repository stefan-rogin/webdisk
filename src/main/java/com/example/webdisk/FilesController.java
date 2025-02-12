package com.example.webdisk;

import org.springframework.web.bind.annotation.RestController;

import com.example.webdisk.response.FilesSizeResponse;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class FilesController {

    @GetMapping("/files/size")
    public FilesSizeResponse getStaorageSize() {
        return new FilesSizeResponse(25);
    }
}
