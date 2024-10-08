package com.example.noticeapi.controller;

import com.example.noticeapi.dto.NoticeCreateDto;
import com.example.noticeapi.dto.NoticeDetailResponseDto;
import com.example.noticeapi.dto.NoticeResponseDto;
import com.example.noticeapi.dto.NoticeSearchDto;
import com.example.noticeapi.dto.NoticeUpdateDto;
import com.example.noticeapi.service.NoticeService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

  private final NoticeService noticeService;

  @PostMapping
  public ResponseEntity<NoticeResponseDto> createNotice(
      @RequestPart("notice") @Validated NoticeCreateDto noticeCreateDto,
      @RequestPart("files") List<MultipartFile> files) {
    NoticeResponseDto responseDto = noticeService.createNotice(noticeCreateDto, files);
    return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<NoticeResponseDto>> getAllNotices(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    List<NoticeResponseDto> notices = noticeService.getAllNotices(page, size);
    return new ResponseEntity<>(notices, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<NoticeDetailResponseDto> getNoticeById(@PathVariable Long id) {
    NoticeDetailResponseDto notice = noticeService.getNoticeDetailById(id);
    return new ResponseEntity<>(notice, HttpStatus.OK);
  }

  @GetMapping("/search")
  public ResponseEntity<List<NoticeResponseDto>> searchNotices(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) String author,
      @RequestParam(required = false) LocalDateTime startDate,
      @RequestParam(required = false) LocalDateTime endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    NoticeSearchDto noticeSearchDto = new NoticeSearchDto(title, content, author, startDate,
        endDate);
    List<NoticeResponseDto> notices = noticeService.searchNotices(noticeSearchDto, page, size);
    return new ResponseEntity<>(notices, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<NoticeResponseDto> updateNotice(
      @PathVariable Long id,
      @RequestPart("notice") @Validated NoticeUpdateDto noticeUpdateDto,
      @RequestPart("files") List<MultipartFile> files) {
    NoticeResponseDto responseDto = noticeService.updateNotice(id, noticeUpdateDto, files);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
    noticeService.deleteNotice(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
