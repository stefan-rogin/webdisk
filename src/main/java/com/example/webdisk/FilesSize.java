package com.example.webdisk;

import org.springframework.beans.factory.annotation.Value;

import com.example.webdisk.response.FilesSizeResponse;

public class FilesSize {
    
    @Value("${webdisk.path}")
    private static String webdiskPath;

    public static FilesSizeResponse getSize() {
        String randomeFilename = FilesSample.generateFilename();
        return new FilesSizeResponse(25);
    }
}
