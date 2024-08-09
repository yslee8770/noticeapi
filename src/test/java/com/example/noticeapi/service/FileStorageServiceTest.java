package com.example.noticeapi.service;

import com.example.noticeapi.dto.FileDto;
import com.example.noticeapi.entity.File;
import com.example.noticeapi.entity.Notice;
import com.example.noticeapi.exception.FileStorageException;
import com.example.noticeapi.exception.InvalidFileNameException;
import com.example.noticeapi.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
  @DisplayName("파일 저장 성공 테스트")
  void storeFile_Success() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
    String storedFileName = fileStorageService.storeFile(file).join();

    assertNotNull(storedFileName);
    assertTrue(storedFileName.endsWith(".txt"));
    assertTrue(Files.exists(fileStorageLocation.resolve(storedFileName)));
  }

  @Test
  @DisplayName("파일 저장 실패 테스트 - 잘못된 파일명")
  void storeFile_Failure_InvalidFileName() {
    MockMultipartFile file = new MockMultipartFile("file", "../test.txt", "text/plain", "Hello, World!".getBytes());

    CompletionException thrown = assertThrows(CompletionException.class, () -> {
      fileStorageService.storeFile(file).join();
    });

    assertTrue(thrown.getCause() instanceof InvalidFileNameException);
  }

  @Test
  @DisplayName("파일 로드 성공 테스트")
  void loadFileAsResource_Success() throws Exception {
    String storedFileName = "test.txt";
    Path filePath = fileStorageLocation.resolve(storedFileName).normalize();
    Files.createFile(filePath);

    try {
      Resource resource = fileStorageService.loadFileAsResource(storedFileName).join();
      assertNotNull(resource);
      assertTrue(resource.exists());
    } finally {
      Files.deleteIfExists(filePath);
    }
  }

  @Test
  @DisplayName("파일 로드 실패 테스트 - 파일 없음")
  void loadFileAsResource_FileNotFound() {
    String storedFileName = "nonexistent.txt";

    CompletionException thrown = assertThrows(CompletionException.class, () -> {
      fileStorageService.loadFileAsResource(storedFileName).join();
    });

    assertTrue(thrown.getCause() instanceof FileStorageException);
  }

  @Test
  @DisplayName("파일 DTO 조회 성공 테스트")
  void getFileDtoById_Success() {
    Long fileId = 1L;
    File mockFile = File.builder()
        .id(fileId)
        .originalFileName("test.txt")
        .storedFileName("test_stored.txt")
        .filePath(fileStorageLocation.resolve("test_stored.txt").toString())
        .isDeleted(false)
        .build();

    when(fileRepository.findById(anyLong())).thenReturn(Optional.of(mockFile));

    FileDto fileDto = fileStorageService.getFileDtoById(fileId);

    assertNotNull(fileDto);
    assertEquals(fileId, fileDto.getId());
    assertEquals("test.txt", fileDto.getOriginalFileName());
    assertEquals("test_stored.txt", fileDto.getStoredFileName());
    assertEquals(fileStorageLocation.resolve("test_stored.txt").toString(), fileDto.getFilePath());
  }

  @Test
  @DisplayName("파일 DTO 조회 실패 테스트 - 파일 없음")
  void getFileDtoById_FileNotFound() {
    Long fileId = 1L;

    when(fileRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(FileStorageException.class, () -> {
      fileStorageService.getFileDtoById(fileId);
    });
  }

  @Test
  @DisplayName("공지사항의 파일 처리 성공 테스트")
  void processFiles_Success() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
    Notice notice = Notice.builder().id(1L).build();

    when(fileRepository.save(any(File.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<File> processedFiles = fileStorageService.processFiles(Collections.singletonList(file), notice).join();

    assertNotNull(processedFiles);
    assertEquals(1, processedFiles.size());
    assertEquals("test.txt", processedFiles.get(0).getOriginalFileName());
  }

  @Test
  @DisplayName("공지사항의 파일 삭제 성공 테스트")
  void deleteFilesByNotice_Success() throws Exception {
    Notice notice = Notice.builder().id(1L).build();

    File mockFile = File.builder()
        .id(1L)
        .originalFileName("test.txt")
        .storedFileName("test_stored.txt")
        .filePath(fileStorageLocation.resolve("test_stored.txt").toString())
        .isDeleted(false)
        .notice(notice)
        .build();

    when(fileRepository.findByNoticeId(anyLong())).thenReturn(Collections.singletonList(mockFile));

    fileStorageService.deleteFilesByNotice(notice).join();

    verify(fileRepository, times(1)).save(any(File.class));
    assertTrue(mockFile.isDeleted());
  }
}
