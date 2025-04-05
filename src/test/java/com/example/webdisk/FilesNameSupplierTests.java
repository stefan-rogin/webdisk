package com.example.webdisk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.webdisk.util.FilesNameSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FilesNameSupplierTests {

    @Autowired
    private FilesNameSupplier supplier;

    @Test
    void contextLoads() {
        assertThat(supplier.get()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
    }

}
