package com.example.webdisk;

import org.springframework.core.task.TaskExecutor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class FilesAccessTest {

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private Path pathMock;

    @InjectMocks
    private FilesAccess filesAccess;

    @BeforeEach
    public void setup() {
        filesAccess.setPath("/sample");
    }

    @Test
    public void shouldListStorageAndFilterValidFiles() throws IOException {
        Path one = mock(Path.class);      // Valid
        Path andone = mock(Path.class);   // Valid
        Path not1 = mock(Path.class);     // Not valid
        Path twodots = mock(Path.class);  // Not valid
        Stream<Path> pathStream = Stream.of(one, andone, not1, twodots);

        when(Files.list(any(Path.class))).thenReturn(pathStream);
        when(one.getFileName()).thenReturn(Path.of("one"));
        when(andone.getFileName()).thenReturn(Path.of("andone"));
        when(not1.getFileName()).thenReturn(Path.of("not1.tmp"));
        when(Files.isDirectory(one)).thenReturn(false);
        when(Files.isDirectory(andone)).thenReturn(false);
        when(Files.isDirectory(not1)).thenReturn(false);
        when(Files.isDirectory(twodots)).thenReturn(true);

        assertThat(filesAccess.listFiles()).isEqualTo(Arrays.asList("one", "andone"));
    }

    @Test
    public void shouldGetFileContentsInAStream() throws IOException {
        InputStream inputStreamMock = new ByteArrayInputStream("testContent".getBytes());
        MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class);

        filesStaticMock.when(() -> Files.newInputStream(any(Path.class))).thenReturn(inputStreamMock);

        assertThat(filesAccess.getFile("any")).isEqualTo(inputStreamMock);
    }

    @Test
    public void shouldGetFileContentsInAStreamAsync() throws Exception {
        InputStream inputStreamMock = new ByteArrayInputStream("testContent".getBytes());
        MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class);

        filesStaticMock.when(() -> Files.newInputStream(any(Path.class))).thenReturn(inputStreamMock);

        assertThat(filesAccess.getFileAsync("any").get()).isEqualTo(inputStreamMock);
    }

    @Test
    public void shouldStoreFile() throws IOException {
        MultipartFile multipartFileMock = mock(MultipartFile.class);
        InputStream inputStreamMock = new ByteArrayInputStream("testContent".getBytes());
        MockedStatic<Files> filesStaticMock = Mockito.mockStatic(Files.class);
        AtomicBoolean fileCopied = new AtomicBoolean(false);

        when(multipartFileMock.getInputStream()).thenReturn(inputStreamMock);
        filesStaticMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                .thenAnswer(i -> {
                    fileCopied.set(true);
                    return 1L;
                });

        filesAccess.putFile("testFile", multipartFileMock);

        assertThat(fileCopied).isTrue();
    }
}
