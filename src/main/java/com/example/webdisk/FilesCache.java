package com.example.webdisk;

import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class FilesCache {

    private final Set<String> files = new HashSet<String>();

    public boolean containsFile(String fileName) {
        return this.files.contains(fileName);
    }

    public void putFile(String fileName) {
        this.files.add(fileName);
    }

    public String newFile() {
        
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

    private String generateFileName() {
        // 0-9: 48-57
        // a-z: 97-122
        // A-Z: 65-90
        // -_: 45,95
        final int LIMIT_ASCII_LOW = 45, LIMIT_ASCII_HIGH = 123, MIN_FILENAME_LENGTH = 1, MAX_FILENAME_LENGTH = 64;

        RandomGenerator generator = RandomGenerator.of("L128X256MixRandom");
        int randomFileNameLength = generator.nextInt(MIN_FILENAME_LENGTH, MAX_FILENAME_LENGTH + 1);
        String randomFileName = generator.ints(LIMIT_ASCII_LOW, LIMIT_ASCII_HIGH)
                .filter(n -> n == 45 || (n >= 48 && n <= 57) || (n >= 65 && n <= 90) || n == 95
                        || (n >= 97 && n <= 122))
                .limit(randomFileNameLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return randomFileName;
    }
}
