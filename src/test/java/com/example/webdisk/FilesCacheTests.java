package com.example.webdisk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class FilesCacheTests {

    private FilesCache cache;

    @BeforeEach
    public void setupEachTest() {
        cache = new FilesCache();
    }

    @Test
    void shouldStartEmpty() {
        assertThat(cache.getSize()).isZero();
    }

    @Test
    void shouldReturnItsSize() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        assertThat(cache.getSize()).isEqualTo(1);
        cache.putFile("andone");
        assertThat(cache.getSize()).isEqualTo(2);
    }

    @Test
    void shouldReturnIfAFileIsPresentInCache() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        assertThat(cache.containsFile("one")).isTrue();
    }

    @Test
    void shouldGenerateNewValidFileNames() {
        assertThat(cache.getSize()).isZero();
        assertThat(cache.newFile()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
        assertThat(cache.getSize()).isEqualTo(1);
        assertThat(cache.newFile()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
        assertThat(cache.getSize()).isEqualTo(2);    
    }

}
