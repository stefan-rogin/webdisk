package com.example.webdisk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@AutoConfigureMockMvc
@SpringBootTest
class FilesControllerTests {

    private MockMvc mockMvc; 

    @Mock
    private FilesAccess storage;

    @InjectMocks
    private FilesController controller;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testGetFile() throws Exception {
        String fileName = "testone";
        InputStream inputStream = new ByteArrayInputStream("test_content".getBytes());
        CompletableFuture<InputStream> futureInputStream = CompletableFuture.completedFuture(inputStream);
        when(storage.getFileAsync(anyString())).thenReturn(futureInputStream);

        CompletableFuture<ResponseEntity<InputStreamResource>> responseFuture = controller.getFileForFileName(fileName, null);
        ResponseEntity<InputStreamResource> response = responseFuture.get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=" + fileName);
        assertThat(response.getBody()).isNotNull();
    }
    
    @Test
    public void testGetFileForFileName_NotFound() throws Exception {
        String fileName = "none";
        CompletableFuture<InputStream> futureInputStream = CompletableFuture.failedFuture(new IOException("File not found"));
        when(storage.getFileAsync(anyString())).thenReturn(futureInputStream);

        CompletableFuture<ResponseEntity<InputStreamResource>> responseFuture = controller.getFileForFileName(fileName, null);
        ResponseEntity<InputStreamResource> response = responseFuture.get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    }
    
}
