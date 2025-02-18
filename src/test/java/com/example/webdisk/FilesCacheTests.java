package com.example.webdisk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    void shouldFindFilesForAGivenPattern() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        cache.putFile("andone");
        cache.putFile("two");
        assertThat(cache.findFilesForPattern("one"))
            .containsExactlyInAnyOrder(new String[]{"andone", "one"});    
        assertThat(cache.findFilesForPattern("^[a-z]{3}$"))
            .containsExactlyInAnyOrder((new String[]{"one", "two"}));    
    }

    @Test
    void shouldNotFindFilesForAGivenPatternWithNoMatches() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        cache.putFile("andone");
        cache.putFile("two");
        assertThat(cache.findFilesForPattern("[0-9]+")).isEmpty();    
    }

    @Test
    void shouldGenerateNewValidFileNames() {
        assertThat(cache.getSize()).isZero();
        assertThat(cache.newFile()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
        assertThat(cache.getSize()).isEqualTo(1);
        assertThat(cache.newFile()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
        assertThat(cache.getSize()).isEqualTo(2);    
    }

    @Test
    void shouldDeleteGivenFileNames() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        assertThat(cache.getSize()).isEqualTo(1);
        cache.deleteFile("one");
        assertThat(cache.getSize()).isZero();
    }

    @Test
    void shouldDoNothingWhenAskedToDeleteInexistentFileNames() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        assertThat(cache.getSize()).isEqualTo(1);
        cache.deleteFile("two");
        assertThat(cache.getSize()).isEqualTo(1);
    }

    @Test
    void shouldValidateFileNames() {
        String longName = Stream.generate(() -> "e").limit(100).collect(Collectors.joining());
        assertThat(cache.isValid("")).isFalse();
        assertThat(cache.isValid(longName)).isFalse();
        assertThat(cache.isValid("reGu1ar_")).isTrue();
        assertThat(cache.isValid("illegal$chars.txt")).isFalse();
    }

}
