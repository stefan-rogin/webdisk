package com.example.webdisk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import com.example.webdisk.response.FilesSizeResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.io.IOException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class FilesControllerTests {

    @Mock
    private FilesAccess storage;
    
    private Logger logger;

    @InjectMocks
    private FilesController controller;

    @BeforeTestClass
    public void setup() throws IOException {
        when(storage.listFiles()).thenReturn(Arrays.asList("one", "andone"));
        // doNothing().when(logger).info(anyString(), anyString(), anyString());
        try (MockedStatic<Logger> mockLogger = Mockito.mockStatic(Logger.class)) {
            mockLogger.when(() -> logger.info(anyString(), anyString(), anyString())).thenAnswer(null);
        }
    }

    @Test
    void testGetSize() throws Exception {
        ResponseEntity<FilesSizeResponse> response = controller.getFilesSize(null);
        assertThat(response.getStatusCode().is2xxSuccessful());
    }

    // @Test
    // public void testGetFile() throws Exception {
    //     InputStream inputStream = new ByteArrayInputStream("test_content".getBytes());
    //     CompletableFuture<InputStream> futureInputStream = CompletableFuture.completedFuture(inputStream);
    //     when(storage.getFileAsync(anyString())).thenReturn(futureInputStream);

    //     CompletableFuture<ResponseEntity<InputStreamResource>> responseFuture = controller.getFileForFileName("one", null);
    //     ResponseEntity<InputStreamResource> response = responseFuture.get();

        // mockMvc.perform(get("/files/mock")).andExpect(status().isOk())
        //     .andExpect(result -> {
        //         assertThat(result.getResponse().getContentAsString()).isEqualTo("test_content");
        //     }).andReturn();

        // assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        // assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=" + fileName);
        // assertThat(response.getBody()).isNotNull();
    // }
    
    // @Test
    // public void testGetFileForFileName_NotFound() throws Exception {
    //     // Arrange
    //     String fileName = "none";
    //     CompletableFuture<InputStream> futureInputStream = CompletableFuture.failedFuture(new IOException("File not found"));
    //     when(storage.getFileAsync(anyString())).thenReturn(futureInputStream);

    //     // Act
    //     CompletableFuture<ResponseEntity<InputStreamResource>> responseFuture = controller.getFileForFileName(fileName, null);
    //     ResponseEntity<InputStreamResource> response = responseFuture.get();

    //     // Assert
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    // }
}