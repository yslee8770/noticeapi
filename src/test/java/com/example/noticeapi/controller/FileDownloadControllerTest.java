package com.example.noticeapi.controller;

import com.example.noticeapi.dto.FileDto;
import com.example.noticeapi.exception.FileNotFoundException;
import com.example.noticeapi.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileDownloadController.class)
public class FileDownloadControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FileStorageService fileStorageService;

  @InjectMocks
  private FileDownloadController fileDownloadController;

  @BeforeEach
  public void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  public void downloadFile_Success() throws Exception {
    FileDto fileDto = FileDto.builder()
        .id(1L)
        .originalFileName("test.txt")
        .storedFileName("test.txt")
        .filePath("path/to/test.txt")
        .build();

    Resource resource = new ByteArrayResource("This is a test file content".getBytes());

    when(fileStorageService.getFileDtoById(1L)).thenReturn(fileDto);
    when(fileStorageService.loadFileAsResource("test.txt")).thenReturn(CompletableFuture.completedFuture(resource));

    mockMvc.perform(get("/files/download/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
        .andExpect(content().bytes("This is a test file content".getBytes()));
  }

  @Test
  public void downloadFile_FileNotFound() throws Exception {
    when(fileStorageService.getFileDtoById(1L)).thenThrow(new FileNotFoundException("File not found"));

    mockMvc.perform(get("/files/download/1"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.error").value("FileNotFoundException"))
        .andExpect(jsonPath("$.message").value("File not found"));
  }
}
