package com.example.webdisk;

import org.springframework.web.multipart.MultipartFile;

import com.example.webdisk.service.FilesAccess;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class FilesAccessTests {

    @Mock
    private Path pathMock;

    @InjectMocks
    private FilesAccess filesAccess;

    @BeforeEach
    public void setup() {
        filesAccess.setPath("/mock");
    }

    @Test
    void shouldListStorageAndFilterValidFiles() throws IOException {

        try (MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class)) {

            Path one = mock(Path.class); // Valid
            Path andone = mock(Path.class); // Valid
            Path not1 = mock(Path.class); // Not valid
            Path twodots = mock(Path.class); // Not valid
            Stream<Path> pathStream = Stream.of(one, andone, not1, twodots);

            filesStaticMock.when(() -> Files.list(any(Path.class))).thenReturn(pathStream);
            when(one.getFileName()).thenReturn(Path.of("one"));
            when(andone.getFileName()).thenReturn(Path.of("andone"));
            when(not1.getFileName()).thenReturn(Path.of("not1.tmp"));
            when(Files.isDirectory(one)).thenReturn(false);
            when(Files.isDirectory(andone)).thenReturn(false);
            when(Files.isDirectory(not1)).thenReturn(false);
            when(Files.isDirectory(twodots)).thenReturn(true);

            assertThat(filesAccess.listFiles()).isEqualTo(Arrays.asList("one", "andone"));
        }
    }

    @Test
    void shouldGetFileContentsInAStream() throws IOException {
        try (MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class)) {
            InputStream inputStreamMock = new ByteArrayInputStream("oneContent".getBytes());

            filesStaticMock.when(() -> Files.newInputStream(any(Path.class))).thenReturn(inputStreamMock);

            assertThat(filesAccess.getFile("any")).isEqualTo(inputStreamMock);
        }
    }

    @Test
    void shouldStoreFile() throws IOException {
        try (MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class)) {
            MultipartFile multipartFileMock = mock(MultipartFile.class);
            InputStream inputStreamMock = new ByteArrayInputStream("oneContent".getBytes());
            AtomicBoolean fileCopied = new AtomicBoolean(false);

            when(multipartFileMock.getInputStream()).thenReturn(inputStreamMock);
            filesStaticMock
                    .when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenAnswer(i -> {
                        fileCopied.set(true);
                        return null;
                    });
            filesAccess.putFile("one", multipartFileMock);

            assertThat(fileCopied).isTrue();
        }
    }

    @Test
    void shouldDeleteFile() throws IOException {
        try (MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class)) {
            AtomicBoolean fileDeleted = new AtomicBoolean(false);
            filesStaticMock.when(() -> Files.delete(any(Path.class)))
                    .thenAnswer(i -> {
                        fileDeleted.set(true);
                        return null;
                    });
            filesAccess.deleteFile("any");

            assertThat(fileDeleted).isTrue();
        }
    }
    
    @Test
    void shouldGetInstancePath() {
        assertThat(filesAccess.getPath()).isEqualTo("/mock/");
    }
}
