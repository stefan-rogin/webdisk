package com.example.webdisk;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.regex.Pattern;

@AutoConfigureMockMvc
@SpringBootTest
class FilesControllerTests {

    @Autowired
    private MockMvc mockMvc; 

    @Mock
    private FilesAccess storage;

    @InjectMocks
    private FilesController controller;

    @Test
    void getFilesSizeTests() throws Exception {
        this.mockMvc.perform(get("/files/size"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(7));
    }
    
    @Test
    void getFileForFileNameTests() throws Exception {
        this.mockMvc.perform(get("/files/one"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-disposition", "attachment; filename=one"))
            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void postFileTests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "oneup", 
                MediaType.TEXT_PLAIN_VALUE, "one".getBytes());

        this.mockMvc.perform(get("/files/size"))
            .andExpect(jsonPath("$.size").value(12));
        this.mockMvc.perform(multipart("/files/upload")
            .file(file))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String response = result.getResponse().getContentAsString();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> responseMap = objectMapper.readValue(response, Map.class);
                String fileName = responseMap.get("filename");
                assertTrue(fileName).matches("^[a-zA-Z0-9-_]{1,64}$");

                Pattern namePattern = Pattern.compile("[a-zA-Z0-9-_]{1,64}");
                assertTrue(namePattern.matcher(response).find());
            });
        this.mockMvc.perform(get("/files/size"))
            .andExpect(jsonPath("$.size").value(13));
    }
    
}