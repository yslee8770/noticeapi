package com.example.noticeapi.service;

import com.example.noticeapi.dto.NoticeCreateDto;
import com.example.noticeapi.dto.NoticeDetailResponseDto;
import com.example.noticeapi.dto.NoticeResponseDto;
import com.example.noticeapi.dto.NoticeSearchDto;
import com.example.noticeapi.dto.NoticeUpdateDto;
import com.example.noticeapi.entity.File;
import com.example.noticeapi.entity.Notice;
import com.example.noticeapi.exception.NoticeNotFoundException;
import com.example.noticeapi.mapper.NoticeMapper;
import com.example.noticeapi.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class NoticeServiceTest {

  @Mock
  private NoticeRepository noticeRepository;

  @Mock
  private FileStorageService fileStorageService;

  @InjectMocks
  private NoticeService noticeService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("공지사항 생성 성공 테스트")
  void testCreateNotice_Success() {
    // Given: A NoticeCreateDto with valid details
    NoticeCreateDto noticeCreateDto = new NoticeCreateDto("Test Title", "Test Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Test Author");
    Notice notice = NoticeMapper.toEntity(noticeCreateDto);

    List<File> mockFiles = Collections.singletonList(File.builder()
        .originalFileName("file.txt")
        .storedFileName("random-name.txt")
        .filePath("path/to/file")
        .notice(notice)
        .build());
    when(fileStorageService.processFiles(anyList(), any(Notice.class))).thenReturn(CompletableFuture.completedFuture(mockFiles));

    // When: Saving the notice
    when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

    // Then: The notice should be created successfully
    NoticeResponseDto responseDto = noticeService.createNotice(noticeCreateDto, Collections.emptyList());
    assertNotNull(responseDto, "Response DTO should not be null");
    assertEquals("Test Title", responseDto.getTitle(), "The title should match");
    verify(noticeRepository, times(1)).save(any(Notice.class));
  }

  @Test
  @DisplayName("공지사항 생성 실패 테스트 - 제목과 내용이 빈 경우")
  void testCreateNotice_Failure() {
    // Given: A NoticeCreateDto with empty title and content
    NoticeCreateDto noticeCreateDto = new NoticeCreateDto("", "", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Test Author");

    // When & Then: Creating notice should throw an exception
    assertThrows(IllegalArgumentException.class, () -> noticeService.createNotice(noticeCreateDto, Collections.emptyList()), "An empty title or content should throw IllegalArgumentException");
  }

  @Test
  @DisplayName("공지사항 전체 조회 성공 테스트")
  void testGetAllNotices_Success() {
    // Given: A notice with a specific title
    Notice notice = Notice.builder()
        .title("Test Title")
        .build();

    // When: Fetching all notices
    when(noticeRepository.findByIsDeletedFalse(any())).thenReturn(new PageImpl<>(Collections.singletonList(notice)));

    // Then: The notice list should contain the given notice
    List<NoticeResponseDto> notices = noticeService.getAllNotices(0, 10);
    assertNotNull(notices, "Notices list should not be null");
    assertEquals(1, notices.size(), "The size of notices list should be 1");
    assertEquals("Test Title", notices.get(0).getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 조회 성공 테스트")
  void testGetNoticeById_Success() {
    // Given: A notice with a specific ID
    Notice notice = Notice.builder()
        .id(1L)
        .title("Test Title")
        .build();

    // When: Fetching the notice by ID
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

    // Then: The fetched notice should match the given ID
    NoticeDetailResponseDto responseDto = noticeService.getNoticeDetailById(1L);
    assertNotNull(responseDto, "Response DTO should not be null");
    assertEquals("Test Title", responseDto.getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 조회 실패 테스트 - 존재하지 않는 ID")
  void testGetNoticeById_NotFound() {
    // Given: A non-existing notice ID
    when(noticeRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then: Fetching the notice by ID should throw an exception
    assertThrows(NoticeNotFoundException.class, () -> noticeService.getNoticeDetailById(1L), "A non-existing ID should throw NoticeNotFoundException");
  }

  @Test
  @DisplayName("공지사항 검색 성공 테스트")
  void testSearchNotices_Success() {
    // Given: Search criteria
    Notice notice = Notice.builder()
        .title("Test Title")
        .build();
    NoticeSearchDto searchDto = new NoticeSearchDto("Test", null, null, null, null);

    // When: Searching notices
    when(noticeRepository.findByTitleContainingAndContentContainingAndAuthorContainingAndCreatedAtBetween(
        anyString(), anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), any()))
        .thenReturn(new PageImpl<>(Collections.singletonList(notice)));

    // Then: The search result should contain the matching notice
    List<NoticeResponseDto> notices = noticeService.searchNotices(searchDto, 0, 10);
    assertNotNull(notices, "Notices list should not be null");
    assertEquals(1, notices.size(), "The size of notices list should be 1");
    assertEquals("Test Title", notices.get(0).getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 검색 실패 테스트 - 일치하는 항목 없음")
  void testSearchNotices_NoMatch() {
    // Given: Search criteria
    NoticeSearchDto searchDto = new NoticeSearchDto("Non-existing title", null, null, null, null);

    // When: Searching notices
    when(noticeRepository.findByTitleContainingAndContentContainingAndAuthorContainingAndCreatedAtBetween(
        anyString(), anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), any()))
        .thenReturn(Page.empty());

    // Then: The search result should be empty
    List<NoticeResponseDto> notices = noticeService.searchNotices(searchDto, 0, 10);
    assertNotNull(notices, "Notices list should not be null");
    assertEquals(0, notices.size(), "The size of notices list should be 0");
  }

  @Test
  @DisplayName("공지사항 업데이트 성공 테스트")
  void testUpdateNotice_Success() {
    // Given: A notice to update
    Notice notice = Notice.builder()
        .id(1L)
        .title("Old Title")
        .build();
    NoticeUpdateDto updateDto = new NoticeUpdateDto("New Title", "New Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
    when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

    List<File> mockFiles = Collections.singletonList(File.builder()
        .originalFileName("file.txt")
        .storedFileName("random-name.txt")
        .filePath("path/to/file")
        .notice(notice)
        .build());
    when(fileStorageService.processFiles(anyList(), any(Notice.class))).thenReturn(CompletableFuture.completedFuture(mockFiles));

    // When: Updating the notice
    NoticeResponseDto responseDto = noticeService.updateNotice(1L, updateDto, Collections.emptyList());

    // Then: The notice should be updated successfully
    assertNotNull(responseDto, "Response DTO should not be null");
    assertEquals("New Title", responseDto.getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 업데이트 실패 테스트 - 존재하지 않는 ID")
  void testUpdateNotice_NotFound() {
    // Given: A non-existing notice ID
    NoticeUpdateDto updateDto = new NoticeUpdateDto("New Title", "New Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    when(noticeRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then: Updating the notice should throw an exception
    assertThrows(NoticeNotFoundException.class, () -> noticeService.updateNotice(1L, updateDto, Collections.emptyList()), "A non-existing ID should throw NoticeNotFoundException");
  }

  @Test
  @DisplayName("공지사항 삭제 성공 테스트")
  void testDeleteNotice_Success() {
    // Given: A notice to delete
    Notice notice = Notice.builder()
        .id(1L)
        .build();
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

    // When: Deleting the notice
    noticeService.deleteNotice(1L);

    // Then: The notice should be marked as deleted
    assertTrue(notice.isDeleted(), "The notice should be marked as deleted");
    verify(noticeRepository, times(1)).save(notice);
  }

  @Test
  @DisplayName("공지사항 삭제 실패 테스트 - 존재하지 않는 ID")
  void testDeleteNotice_NotFound() {
    // Given: A non-existing notice ID
    when(noticeRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then: Deleting the notice should throw an exception
    assertThrows(NoticeNotFoundException.class, () -> noticeService.deleteNotice(1L), "A non-existing ID should throw NoticeNotFoundException");
  }
}
