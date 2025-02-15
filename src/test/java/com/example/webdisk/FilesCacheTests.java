package com.example.webdisk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FilesCacheTests {

    // TODO: IoC
    private FilesCache cache = new FilesCache();

    @Test
    void shouldStartEmpty() {
        assertThat(cache.getSize()).isZero();
    }

    @Test
    void shouldReturnItsSize() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        assertThat(cache.getSize()).isEqualTo(1);
        cache.putFile("anotherone");
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
        cache.putFile("anotherone");
        cache.putFile("xyz");
        assertThat(cache.findFilesForPattern("one")).containsExactlyInAnyOrder(new String[]{"anotherone", "one"});    
        assertThat(cache.findFilesForPattern("^[a-z]{3}$")).containsExactlyInAnyOrder((new String[]{"one", "xyz"}));    
    }

    @Test
    void shouldNotFindFilesForAGivenPatternWithNoMatches() {
        assertThat(cache.getSize()).isZero();
        cache.putFile("one");
        cache.putFile("anotherone");
        cache.putFile("xyz");
        assertThat(cache.findFilesForPattern("[0-9]+")).isEmpty();    
    }

    @Test
    void shouldGenerateNewFileNames() {
        assertThat(cache.getSize()).isZero();
        assertThat(cache.newFile()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
        assertThat(cache.getSize()).isEqualTo(1);
        assertThat(cache.newFile()).containsPattern("^[a-zA-Z0-9-_]{1,64}$");
        assertThat(cache.getSize()).isEqualTo(2);    }

}
