package com.example.webdisk.response;

/**
 * A response record for a file post operation.
 *
 * @param fileName the name of the file that was posted
 */
public record FilesPostFileResponse(String fileName) { }