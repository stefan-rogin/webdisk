package com.example.webdisk.response;

/**
 * A record that represents the response for a file search operation.
 *
 * @param results an array of strings containing the search results
 */
public record FilesSearchResponse(String[] results) { }
