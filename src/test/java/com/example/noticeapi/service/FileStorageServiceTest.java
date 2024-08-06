package com.example.noticeapi.service;

import com.example.noticeapi.dto.FileDto;
import com.example.noticeapi.entity.File;
import com.example.noticeapi.exception.FileStorageException;
import com.example.noticeapi.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class FileStorageServiceTest {

  @Mock
  private FileRepository fileRepository;

  private FileStorageService fileStorageService;

  private final Path fileStorageLocation = Paths.get("D:/TEST").toAbsolutePath().normalize();

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    Files.createDirectories(fileStorageLocation);

    fileStorageService = new FileStorageService(fileStorageLocation.toString(), fileRepository);
  }

  @Test
  @DisplayName("loadFileAsResource 성공 테스트")
  void testLoadFileAsResource_Success() throws Exception {
    String storedFileName = "test.txt";
    Path filePath = fileStorageLocation.resolve(storedFileName).normalize();
    Files.createFile(filePath);

    try {
      Resource resource = fileStorageService.loadFileAsResource(storedFileName);

      assertNotNull(resource, "Resource should not be null");
      assertTrue(resource.exists(), "Resource should exist");
    } finally {
      Files.deleteIfExists(filePath);
    }
  }

  @Test
  @DisplayName("loadFileAsResource 실패 테스트 - 파일 없음")
  void testLoadFileAsResource_FileNotFound() {
    String storedFileName = "nonexistent.txt";

    assertThrows(FileStorageException.class, () -> fileStorageService.loadFileAsResource(storedFileName),
        "Expected FileStorageException to be thrown, but it didn't");
  }

  @Test
  @DisplayName("getFileDtoById 성공 테스트")
  void testGetFileDtoById_Success() {
    Long fileId = 1L;
    File mockFile = File.builder()
        .id(fileId)
        .originalFileName("test.txt")
        .storedFileName("test_stored.txt")
        .filePath("D:/TEST/test_stored.txt")
        .isDeleted(false)
        .build();

    when(fileRepository.findById(anyLong())).thenReturn(Optional.of(mockFile));

    FileDto fileDto = fileStorageService.getFileDtoById(fileId);

    assertNotNull(fileDto, "FileDto should not be null");
    assertEquals(fileId, fileDto.getId(), "File ID should match");
    assertEquals(mockFile.getOriginalFileName(), fileDto.getOriginalFileName(), "Original file name should match");
    assertEquals(mockFile.getStoredFileName(), fileDto.getStoredFileName(), "Stored file name should match");
    assertEquals(mockFile.getFilePath(), fileDto.getFilePath(), "File path should match");
  }

  @Test
  @DisplayName("getFileDtoById 실패 테스트 - 파일 없음")
  void testGetFileDtoById_FileNotFound() {
    Long fileId = 1L;

    when(fileRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(FileStorageException.class, () -> fileStorageService.getFileDtoById(fileId),
        "Expected FileStorageException to be thrown, but it didn't");
  }
}
