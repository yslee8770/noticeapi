package com.example.noticeapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.noticeapi.dto.NoticeCreateDto;
import com.example.noticeapi.dto.NoticeDetailResponseDto;
import com.example.noticeapi.dto.NoticeResponseDto;
import com.example.noticeapi.dto.NoticeSearchDto;
import com.example.noticeapi.dto.NoticeUpdateDto;
import com.example.noticeapi.entity.File;
import com.example.noticeapi.entity.Notice;
import com.example.noticeapi.exception.GlobalExceptionHandler;
import com.example.noticeapi.exception.NoticeNotFoundException;
import com.example.noticeapi.service.FileStorageService;
import com.example.noticeapi.service.NoticeService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NoticeControllerTest {

  @Mock
  private NoticeService noticeService;

  @Mock
  private FileStorageService fileStorageService;

  @InjectMocks
  private NoticeController noticeController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(noticeController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    CompletableFuture<Void> mockFutureVoid = CompletableFuture.completedFuture(null);
    CompletableFuture<List<File>> mockFutureFiles = CompletableFuture.completedFuture(
        Collections.emptyList());

    when(fileStorageService.deleteFilesByNotice(any(Notice.class))).thenReturn(mockFutureVoid);
    when(fileStorageService.processFiles(anyList(), any(Notice.class))).thenReturn(mockFutureFiles);
  }

  @Test
  @DisplayName("공지사항 생성 성공 테스트")
  void createNotice_Success() throws Exception {
    NoticeResponseDto noticeResponseDto = NoticeResponseDto.builder()
        .id(1L)
        .title("Title")
        .content("Content")
        .author("Author")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .attachments(Collections.emptyList())
        .build();

    MockMultipartFile noticeFile = new MockMultipartFile("notice", "", "application/json",
        "{\"title\":\"Title\",\"content\":\"Content\",\"startDate\":\"2022-01-01T00:00:00\",\"endDate\":\"2022-01-02T00:00:00\",\"author\":\"Author\"}".getBytes());
    MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain",
        "some text".getBytes());

    when(noticeService.createNotice(any(NoticeCreateDto.class), anyList())).thenReturn(
        noticeResponseDto);

    mockMvc.perform(multipart("/notices")
            .file(noticeFile)
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Title"));

    verify(noticeService, times(1)).createNotice(any(NoticeCreateDto.class), anyList());
  }

  @Test
  @DisplayName("공지사항 업데이트 성공 테스트")
  void updateNotice_Success() throws Exception {
    NoticeResponseDto noticeResponseDto = NoticeResponseDto.builder()
        .id(1L)
        .title("New Title")
        .content("New Content")
        .author("Author")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .attachments(Collections.emptyList())
        .build();

    MockMultipartFile noticeFile = new MockMultipartFile("notice", "", "application/json",
        "{\"title\":\"New Title\",\"content\":\"New Content\",\"startDate\":\"2023-01-01T00:00:00\",\"endDate\":\"2023-01-02T00:00:00\",\"author\":\"Author\"}".getBytes());
    MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain",
        "some text".getBytes());

    when(noticeService.updateNotice(anyLong(), any(NoticeUpdateDto.class), any())).thenReturn(
        noticeResponseDto);

    mockMvc.perform(multipart("/notices/{id}", 1L)
            .file(noticeFile)
            .file(file)
            .with(request -> {
              request.setMethod("PUT");
              return request;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("New Title"));

    verify(noticeService, times(1)).updateNotice(anyLong(), any(NoticeUpdateDto.class), any());
  }

  @Test
  @DisplayName("공지사항 업데이트 실패 테스트 - 존재하지 않는 ID")
  void updateNotice_Failure_NotFound() throws Exception {
    MockMultipartFile noticeFile = new MockMultipartFile("notice", "", "application/json", "{\"title\":\"New Title\",\"content\":\"New Content\",\"startDate\":\"2022-01-01T00:00:00\",\"endDate\":\"2022-01-02T00:00:00\",\"author\":\"Author\"}".getBytes());
    MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "some text".getBytes());

    when(noticeService.updateNotice(anyLong(), any(NoticeUpdateDto.class), any())).thenThrow(new NoticeNotFoundException("Notice not found with id 1"));

    try {
      mockMvc.perform(multipart("/notices/{id}", 1L)
              .file(noticeFile)
              .file(file)
              .with(request -> {
                request.setMethod("PUT");
                return request;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error").value("NoticeNotFound"))
          .andExpect(jsonPath("$.message").value("Notice not found with id 1"));
    } catch (Exception e) {
      e.printStackTrace();  // 예외 스택 트레이스 출력
      throw e;  // 예외 재발생
    }

    verify(noticeService, times(1)).updateNotice(anyLong(), any(NoticeUpdateDto.class), any());
  }

  @Test
  @DisplayName("전체 공지사항 조회 성공 테스트")
  void getAllNotices_Success() throws Exception {
    NoticeResponseDto noticeResponseDto = NoticeResponseDto.builder()
        .id(1L)
        .title("Title")
        .content("Content")
        .author("Author")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .attachments(Collections.emptyList())
        .build();
    List<NoticeResponseDto> notices = Collections.singletonList(noticeResponseDto);

    when(noticeService.getAllNotices(anyInt(), anyInt())).thenReturn(notices);

    mockMvc.perform(get("/notices")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Title"));

    verify(noticeService, times(1)).getAllNotices(anyInt(), anyInt());
  }

  @Test
  @DisplayName("공지사항 상세 조회 성공 테스트")
  void getNoticeById_Success() throws Exception {
    NoticeDetailResponseDto noticeDetailResponseDto = NoticeDetailResponseDto.builder()
        .id(1L)
        .title("Title")
        .content("Content")
        .author("Author")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .attachments(Collections.emptyList())
        .build();

    when(noticeService.getNoticeDetailById(anyLong())).thenReturn(noticeDetailResponseDto);

    mockMvc.perform(get("/notices/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Title"));

    verify(noticeService, times(1)).getNoticeDetailById(anyLong());
  }

  @Test
  @DisplayName("공지사항 상세 조회 실패 테스트 - 존재하지 않는 ID")
  void getNoticeById_Failure_NotFound() throws Exception {
    when(noticeService.getNoticeDetailById(anyLong())).thenThrow(
        new NoticeNotFoundException("Notice not found with id 1"));

    mockMvc.perform(get("/notices/{id}", 1L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NoticeNotFound"))
        .andExpect(jsonPath("$.message").value("Notice not found with id 1"));

    verify(noticeService, times(1)).getNoticeDetailById(anyLong());
  }

  @Test
  @DisplayName("공지사항 검색 성공 테스트")
  void searchNotices_Success() throws Exception {
    NoticeResponseDto noticeResponseDto = NoticeResponseDto.builder()
        .id(1L)
        .title("Title")
        .content("Content")
        .author("Author")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .attachments(Collections.emptyList())
        .build();
    List<NoticeResponseDto> notices = Collections.singletonList(noticeResponseDto);

    when(noticeService.searchNotices(any(NoticeSearchDto.class), anyInt(), anyInt())).thenReturn(
        notices);

    mockMvc.perform(get("/notices/search")
            .param("title", "Title")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Title"));

    verify(noticeService, times(1)).searchNotices(any(NoticeSearchDto.class), anyInt(), anyInt());
  }

  @Test
  @DisplayName("공지사항 검색 실패 테스트 - 일치하는 항목 없음")
  void searchNotices_Failure_NoMatch() throws Exception {
    when(noticeService.searchNotices(any(NoticeSearchDto.class), anyInt(), anyInt())).thenReturn(
        Collections.emptyList());

    mockMvc.perform(get("/notices/search")
            .param("title", "Non-existing title")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(noticeService, times(1)).searchNotices(any(NoticeSearchDto.class), anyInt(), anyInt());
  }

  @Test
  @DisplayName("공지사항 삭제 성공 테스트")
  void deleteNotice_Success() throws Exception {
    doNothing().when(noticeService).deleteNotice(anyLong());

    mockMvc.perform(delete("/notices/{id}", 1L))
        .andExpect(status().isNoContent());

    verify(noticeService, times(1)).deleteNotice(anyLong());
  }

  @Test
  @DisplayName("공지사항 삭제 실패 테스트 - 존재하지 않는 ID")
  void deleteNotice_Failure_NotFound() throws Exception {
    doThrow(new NoticeNotFoundException("Notice not found with id 1")).when(noticeService)
        .deleteNotice(anyLong());

    mockMvc.perform(delete("/notices/{id}", 1L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NoticeNotFound"))
        .andExpect(jsonPath("$.message").value("Notice not found with id 1"));

    verify(noticeService, times(1)).deleteNotice(anyLong());
  }
}
