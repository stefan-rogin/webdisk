package com.example.webdisk;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Component;

@Component
public class FilesNameSupplier implements Supplier<String> {
    private final RandomGenerator generator;
    private static final int LIMIT_ASCII_LOW = 45;
    private static final int LIMIT_ASCII_HIGH = 122;
    private static final int MIN_FILENAME_LENGTH = 1;
    private static final int MAX_FILENAME_LENGTH = 64;

    public FilesNameSupplier() {
        this.generator = RandomGenerator.of("L128X256MixRandom");
    }

    public String get() {
        // 0-9: 48-57
        // a-z: 97-122
        // A-Z: 65-90
        // -_: 45,95
        int randomFileNameLength = generator.nextInt(MIN_FILENAME_LENGTH, MAX_FILENAME_LENGTH + 1);
        return generator.ints(LIMIT_ASCII_LOW, LIMIT_ASCII_HIGH + 1)
                .filter(n -> n == 45 || (n >= 48 && n <= 57) || (n >= 65 && n <= 90) || n == 95
                        || (n >= 97 && n <= 122))
                .limit(randomFileNameLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
