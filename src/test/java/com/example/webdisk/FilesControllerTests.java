package com.example.webdisk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.webdisk.response.FilesPostFileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class FilesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getFilesSizeTests() throws Exception {
        mockMvc.perform(get("/files/size"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size").exists());
    }

    @Test
    void getFileForFileNameShouldReturnNotFoundForFileNotInWebDisk() throws Exception {
        mockMvc.perform(get("/files/n.one"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postGetAndDeleteFileTests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "oneup",
                MediaType.TEXT_PLAIN_VALUE, "oneup".getBytes());

        mockMvc.perform(get("/files/size"))
                .andExpect(jsonPath("$.size").value(7));
        MvcResult result = mockMvc.perform(multipart("/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        String responseContent = result.getResponse().getContentAsString();
        FilesPostFileResponse newFileResponse = mapper.readValue(responseContent, FilesPostFileResponse.class);
        String newFileName = newFileResponse.fileName();
        mockMvc.perform(get("/files/" + newFileName))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition", "attachment; filename=" + newFileName))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string("oneup"));
        // Clean
        mockMvc.perform(delete("/files/" + newFileName))
                .andExpect(status().isOk());
        mockMvc.perform(get("/files/size"))
                .andExpect(jsonPath("$.size").value(7));

    }

    @Test
    void putGetHeadAndDeleteFileTests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "oneone",
                MediaType.TEXT_PLAIN_VALUE, "two".getBytes());
        mockMvc.perform(multipart("/files/oneone")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk());
        mockMvc.perform(get("/files/oneone"))
                .andExpect(content().string("two"));
        mockMvc.perform(head("/files/oneone"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        // Clean
        mockMvc.perform(delete("/files/oneone"))
                .andExpect(status().isOk());
    }

    @Test
    void putFileShouldValidateFileNames() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "one.one",
                MediaType.TEXT_PLAIN_VALUE, "two".getBytes());
        mockMvc.perform(multipart("/files/one.one")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteNonExistent() throws Exception {
        mockMvc.perform(delete("/files/n.one"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchFileTests() throws Exception {
        mockMvc.perform(get("/files/search?pattern=one"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").exists());
        mockMvc.perform(get("/files/search?pattern=n.one"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").exists());
    }

    @Test
    void getRestricted() throws Exception {
        mockMvc.perform(get("/files/restricted"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/files/restricted").header("Authorization", "Bearer any_token"))
                .andExpect(status().isOk());
    }

}