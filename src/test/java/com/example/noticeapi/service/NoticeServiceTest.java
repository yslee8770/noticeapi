package com.example.noticeapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.noticeapi.dto.NoticeCreateDto;
import com.example.noticeapi.dto.NoticeDetailResponseDto;
import com.example.noticeapi.dto.NoticeResponseDto;
import com.example.noticeapi.dto.NoticeSearchDto;
import com.example.noticeapi.dto.NoticeUpdateDto;
import com.example.noticeapi.entity.Notice;
import com.example.noticeapi.exception.NoticeNotFoundException;
import com.example.noticeapi.mapper.NoticeMapper;
import com.example.noticeapi.repository.NoticeRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;

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
  void createNotice_Success() throws Exception {
    NoticeCreateDto noticeCreateDto = new NoticeCreateDto("Title", "Content", LocalDateTime.now(),
        LocalDateTime.now().plusDays(1), "Author");
    Notice notice = NoticeMapper.toEntity(noticeCreateDto);
    Notice savedNotice = Notice.builder()
        .id(1L)
        .title(notice.getTitle())
        .content(notice.getContent())
        .startDate(notice.getStartDate())
        .endDate(notice.getEndDate())
        .author(notice.getAuthor())
        .createdAt(notice.getCreatedAt())
        .build();

    when(fileStorageService.processFiles(anyList(), any(Notice.class)))
        .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));
    when(noticeRepository.save(any(Notice.class))).thenReturn(savedNotice);

    NoticeResponseDto responseDto = noticeService.createNotice(noticeCreateDto,
        Collections.emptyList());

    assertNotNull(responseDto);
    assertEquals("Title", responseDto.getTitle());
    verify(noticeRepository, times(1)).save(any(Notice.class));
  }

  @Test
  @DisplayName("공지사항 상세 조회 성공 테스트")
  void getNoticeDetailById_Success() {
    Notice notice = Notice.builder().id(1L).title("Title").content("Content").isDeleted(false)
        .build();
    when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(notice));

    NoticeDetailResponseDto responseDto = noticeService.getNoticeDetailById(1L);

    assertNotNull(responseDto);
    assertEquals("Title", responseDto.getTitle());
    verify(noticeRepository, times(1)).findById(anyLong());
  }

  @Test
  @DisplayName("공지사항 상세 조회 실패 테스트 - 존재하지 않는 ID")
  void getNoticeDetailById_Failure_NotFound() {
    when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(NoticeNotFoundException.class, () -> {
      noticeService.getNoticeDetailById(1L);
    });

    verify(noticeRepository, times(1)).findById(anyLong());
  }

  @Test
  @DisplayName("전체 공지사항 조회 성공 테스트")
  void getAllNotices_Success() {
    Notice notice = Notice.builder().id(1L).title("Title").content("Content").isDeleted(false)
        .build();
    when(noticeRepository.findByIsDeletedFalse(any())).thenReturn(new PageImpl<>(List.of(notice)));

    List<NoticeResponseDto> responseDtos = noticeService.getAllNotices(0, 10);

    assertNotNull(responseDtos);
    assertEquals(1, responseDtos.size());
    assertEquals("Title", responseDtos.get(0).getTitle());
    verify(noticeRepository, times(1)).findByIsDeletedFalse(any());
  }

  @Test
  @DisplayName("공지사항 검색 성공 테스트")
  void searchNotices_Success() {
    Notice notice = Notice.builder().id(1L).title("Title").content("Content").author("Author")
        .createdAt(LocalDateTime.now()).isDeleted(false).build();
    NoticeSearchDto searchDto = new NoticeSearchDto("Title", "Content", "Author",
        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    when(
        noticeRepository.findByTitleContainingAndContentContainingAndAuthorContainingAndCreatedAtBetween(
            anyString(), anyString(), anyString(), any(LocalDateTime.class),
            any(LocalDateTime.class), any()))
        .thenReturn(new PageImpl<>(List.of(notice)));

    List<NoticeResponseDto> responseDtos = noticeService.searchNotices(searchDto, 0, 10);

    assertNotNull(responseDtos);
    assertEquals(1, responseDtos.size());
    assertEquals("Title", responseDtos.get(0).getTitle());
    verify(noticeRepository,
        times(1)).findByTitleContainingAndContentContainingAndAuthorContainingAndCreatedAtBetween(
        anyString(), anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class),
        any());
  }

  @Test
  @DisplayName("공지사항 업데이트 성공 테스트")
  void updateNotice_Success() throws Exception {
    Notice notice = Notice.builder().id(1L).title("Title").content("Content").isDeleted(false)
        .build();
    NoticeUpdateDto noticeUpdateDto = new NoticeUpdateDto("Updated Title", "Updated Content",
        LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(notice));
    when(fileStorageService.processFiles(anyList(), any(Notice.class)))
        .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));
    when(fileStorageService.deleteFilesByNotice(any(Notice.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(noticeRepository.save(any(Notice.class))).thenReturn(notice);

    NoticeResponseDto responseDto = noticeService.updateNotice(1L, noticeUpdateDto,
        Collections.emptyList());

    assertNotNull(responseDto);
    assertEquals("Updated Title", responseDto.getTitle());
    verify(noticeRepository, times(1)).save(any(Notice.class));
  }

  @Test
  @DisplayName("공지사항 업데이트 실패 테스트 - 존재하지 않는 ID")
  void updateNotice_Failure_NotFound() {
    NoticeUpdateDto noticeUpdateDto = new NoticeUpdateDto("Updated Title", "Updated Content",
        LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(NoticeNotFoundException.class, () -> {
      noticeService.updateNotice(1L, noticeUpdateDto, Collections.emptyList());
    });

    verify(noticeRepository, times(1)).findById(anyLong());
  }

  @Test
  @DisplayName("공지사항 삭제 성공 테스트")
  void deleteNotice_Success() throws Exception {
    Notice notice = Notice.builder().id(1L).title("Title").content("Content").isDeleted(false)
        .build();
    when(noticeRepository.findById(anyLong())).thenReturn(Optional.of(notice));
    when(fileStorageService.deleteFilesByNotice(any(Notice.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    noticeService.deleteNotice(1L);

    assertTrue(notice.isDeleted());
    verify(noticeRepository, times(1)).save(any(Notice.class));
  }

  @Test
  @DisplayName("공지사항 삭제 실패 테스트 - 존재하지 않는 ID")
  void deleteNotice_Failure_NotFound() {
    when(noticeRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(NoticeNotFoundException.class, () -> {
      noticeService.deleteNotice(1L);
    });

    verify(noticeRepository, times(1)).findById(anyLong());
  }
}
