package com.example.webdisk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FilesSampleTests {

    @Test
    void contextLoads() {
        assertThat(FilesSample.generateFilename()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
    }

}
