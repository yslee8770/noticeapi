package com.example.noticeapi.controller;

import com.example.noticeapi.dto.NoticeCreateDto;
import com.example.noticeapi.dto.NoticeDetailResponseDto;
import com.example.noticeapi.dto.NoticeResponseDto;
import com.example.noticeapi.dto.NoticeSearchDto;
import com.example.noticeapi.dto.NoticeUpdateDto;
import com.example.noticeapi.entity.Notice;
import com.example.noticeapi.repository.NoticeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NoticeControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private NoticeRepository noticeRepository;

  @BeforeEach
  void setUp() {
    noticeRepository.deleteAll();
  }

  @Test
  @DisplayName("공지사항 생성 성공 통합 테스트")
  void testCreateNotice_Success() {
    // Given: A NoticeCreateDto with valid details
    NoticeCreateDto noticeCreateDto = new NoticeCreateDto("Test Title", "Test Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Test Author");
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<NoticeCreateDto> request = new HttpEntity<>(noticeCreateDto, headers);

    // When: Sending POST request to create a notice
    ResponseEntity<NoticeResponseDto> response = restTemplate.postForEntity("http://localhost:" + port + "/notices", request, NoticeResponseDto.class);

    // Then: The notice should be created successfully
    assertEquals(201, response.getStatusCodeValue(), "The status code should be 201 CREATED");
    assertNotNull(response.getBody(), "The response body should not be null");
    assertEquals("Test Title", response.getBody().getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 생성 실패 통합 테스트 - 잘못된 입력")
  void testCreateNotice_Failure() {
    // Given: A NoticeCreateDto with empty title and content
    NoticeCreateDto noticeCreateDto = new NoticeCreateDto("", "", LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Test Author");
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<NoticeCreateDto> request = new HttpEntity<>(noticeCreateDto, headers);

    // When: Sending POST request with invalid input
    ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + "/notices", request, String.class);

    // Then: The request should fail with 400 BAD REQUEST
    assertEquals(400, response.getStatusCodeValue(), "The status code should be 400 BAD REQUEST for invalid input");
  }

  @Test
  @DisplayName("공지사항 전체 조회 성공 통합 테스트")
  void testGetAllNotices_Success() {
    // Given: A notice saved in the repository
    Notice notice = Notice.builder()
        .title("Test Title")
        .content("Test Content")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .author("Test Author")
        .isDeleted(false)
        .build();
    noticeRepository.save(notice);

    // When: Sending GET request to retrieve all notices
    ResponseEntity<NoticeResponseDto[]> response = restTemplate.getForEntity("http://localhost:" + port + "/notices?page=0&size=10", NoticeResponseDto[].class);

    // Then: The notice list should contain the saved notice
    assertEquals(200, response.getStatusCodeValue(), "The status code should be 200 OK");
    assertNotNull(response.getBody(), "The response body should not be null");
    assertEquals(1, response.getBody().length, "The number of notices should be 1");
    assertEquals("Test Title", response.getBody()[0].getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 조회 성공 통합 테스트")
  void testGetNoticeById_Success() {
    // Given: A notice saved in the repository with a specific ID
    Notice notice = Notice.builder()
        .title("Test Title")
        .content("Test Content")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .author("Test Author")
        .isDeleted(false)
        .build();
    Notice savedNotice = noticeRepository.save(notice);

    // When: Sending GET request to retrieve the notice by ID
    ResponseEntity<NoticeDetailResponseDto> response = restTemplate.getForEntity("http://localhost:" + port + "/notices/" + savedNotice.getId(), NoticeDetailResponseDto.class);

    // Then: The retrieved notice should match the saved notice
    assertEquals(200, response.getStatusCodeValue(), "The status code should be 200 OK");
    assertNotNull(response.getBody(), "The response body should not be null");
    assertEquals("Test Title", response.getBody().getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 조회 실패 통합 테스트 - 존재하지 않는 ID")
  void testGetNoticeById_NotFound() {
    // Given: A non-existing notice ID

    // When: Sending GET request with a non-existing ID
    ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/notices/9999", String.class);

    // Then: The request should fail with 404 NOT FOUND
    assertEquals(404, response.getStatusCodeValue(), "The status code should be 404 NOT FOUND for non-existing ID");
  }

  @Test
  @DisplayName("공지사항 검색 성공 통합 테스트")
  void testSearchNotices_Success() {
    // Given: A notice saved in the repository
    Notice notice = Notice.builder()
        .title("Test Title")
        .content("Test Content")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .author("Test Author")
        .isDeleted(false)
        .build();
    noticeRepository.save(notice);
    NoticeSearchDto searchDto = new NoticeSearchDto("Test Title", null, null, null, null);

    // When: Sending GET request to search notices
    ResponseEntity<NoticeResponseDto[]> response = restTemplate.getForEntity("http://localhost:" + port + "/notices/search?title=Test Title", NoticeResponseDto[].class);

    // Then: The notice list should contain the matching notice
    assertEquals(200, response.getStatusCodeValue(), "The status code should be 200 OK");
    assertNotNull(response.getBody(), "The response body should not be null");
    assertEquals(1, response.getBody().length, "The number of notices should be 1");
    assertEquals("Test Title", response.getBody()[0].getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 검색 실패 통합 테스트 - 일치하는 항목 없음")
  void testSearchNotices_NoMatch() {
    // Given: A search criteria with no matching notices
    NoticeSearchDto searchDto = new NoticeSearchDto("Non-existing title", null, null, null, null);

    // When: Sending GET request to search notices
    ResponseEntity<NoticeResponseDto[]> response = restTemplate.getForEntity("http://localhost:" + port + "/notices/search?title=Non-existing title", NoticeResponseDto[].class);

    // Then: The notice list should be empty
    assertEquals(200, response.getStatusCodeValue(), "The status code should be 200 OK");
    assertNotNull(response.getBody(), "The response body should not be null");
    assertEquals(0, response.getBody().length, "The number of notices should be 0");
  }

  @Test
  @DisplayName("공지사항 업데이트 성공 통합 테스트")
  void testUpdateNotice_Success() {
    // Given: A notice saved in the repository
    Notice notice = Notice.builder()
        .title("Test Title")
        .content("Test Content")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .author("Test Author")
        .isDeleted(false)
        .build();
    Notice savedNotice = noticeRepository.save(notice);

    NoticeUpdateDto updateDto = new NoticeUpdateDto("Updated Title", "Updated Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<NoticeUpdateDto> request = new HttpEntity<>(updateDto, headers);

    // When: Sending PUT request to update the notice
    ResponseEntity<NoticeResponseDto> response = restTemplate.exchange("http://localhost:" + port + "/notices/" + savedNotice.getId(), HttpMethod.PUT, request, NoticeResponseDto.class);

    // Then: The notice should be updated successfully
    assertEquals(200, response.getStatusCodeValue(), "The status code should be 200 OK");
    assertNotNull(response.getBody(), "The response body should not be null");
    assertEquals("Updated Title", response.getBody().getTitle(), "The title should match");
  }

  @Test
  @DisplayName("공지사항 업데이트 실패 통합 테스트 - 존재하지 않는 ID")
  void testUpdateNotice_NotFound() {
    // Given: A non-existing notice ID
    NoticeUpdateDto updateDto = new NoticeUpdateDto("Updated Title", "Updated Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<NoticeUpdateDto> request = new HttpEntity<>(updateDto, headers);

    // When: Sending PUT request with a non-existing ID
    ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/notices/9999", HttpMethod.PUT, request, String.class);

    // Then: The request should fail with 404 NOT FOUND
    assertEquals(404, response.getStatusCodeValue(), "The status code should be 404 NOT FOUND for non-existing ID");
  }

  @Test
  @DisplayName("공지사항 삭제 성공 통합 테스트")
  void testDeleteNotice_Success() {
    // Given: A notice saved in the repository
    Notice notice = Notice.builder()
        .title("Test Title")
        .content("Test Content")
        .startDate(LocalDateTime.now())
        .endDate(LocalDateTime.now().plusDays(1))
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .author("Test Author")
        .isDeleted(false)
        .build();
    Notice savedNotice = noticeRepository.save(notice);

    // When: Sending DELETE request to delete the notice
    ResponseEntity<Void> response = restTemplate.exchange("http://localhost:" + port + "/notices/" + savedNotice.getId(), HttpMethod.DELETE, null, Void.class);

    // Then: The notice should be deleted successfully
    assertEquals(204, response.getStatusCodeValue(), "The status code should be 204 NO CONTENT");
  }

  @Test
  @DisplayName("공지사항 삭제 실패 통합 테스트 - 존재하지 않는 ID")
  void testDeleteNotice_NotFound() {
    // Given: A non-existing notice ID

    // When: Sending DELETE request with a non-existing ID
    ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/notices/9999", HttpMethod.DELETE, null, String.class);

    // Then: The request should fail with 404 NOT FOUND
    assertEquals(404, response.getStatusCodeValue(), "The status code should be 404 NOT FOUND for non-existing ID");
  }
}
