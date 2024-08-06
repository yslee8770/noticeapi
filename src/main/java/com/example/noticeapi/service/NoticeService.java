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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NoticeService {

  @Autowired
  private NoticeRepository noticeRepository;

  @Autowired
  private FileStorageService fileStorageService;

  @Transactional
  public NoticeResponseDto createNotice(NoticeCreateDto noticeCreateDto,
      List<MultipartFile> files) {
    if (noticeCreateDto.getTitle() == null || noticeCreateDto.getTitle().trim().isEmpty() ||
        noticeCreateDto.getContent() == null || noticeCreateDto.getContent().trim().isEmpty()) {
      throw new IllegalArgumentException("Title and content cannot be empty");
    }
    Notice notice = NoticeMapper.toEntity(noticeCreateDto);
    List<File> attachments = fileStorageService.processFiles(files, notice).join();
    notice.getAttachments().addAll(attachments);

    notice = noticeRepository.save(notice);
    return NoticeMapper.toDto(notice);
  }

  @Transactional(readOnly = true)
  public List<NoticeResponseDto> getAllNotices(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Notice> notices = noticeRepository.findByIsDeletedFalse(pageable);
    return notices.stream()
        .map(NoticeMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public NoticeDetailResponseDto getNoticeDetailById(Long id) {
    Notice notice = noticeRepository.findById(id)
        .filter(n -> !n.isDeleted())
        .orElseThrow(() -> new NoticeNotFoundException("Notice not found with id " + id));
    return NoticeMapper.toDetailDto(notice);
  }

  @Transactional(readOnly = true)
  public List<NoticeResponseDto> searchNotices(NoticeSearchDto noticeSearchDto, int page,
      int size) {
    Pageable pageable = PageRequest.of(page, size);

    String title = Optional.ofNullable(noticeSearchDto.getTitle()).orElse("");
    String content = Optional.ofNullable(noticeSearchDto.getContent()).orElse("");
    String author = Optional.ofNullable(noticeSearchDto.getAuthor()).orElse("");
    LocalDateTime startDate = Optional.ofNullable(noticeSearchDto.getStartDate())
        .orElse(LocalDateTime.MIN);
    LocalDateTime endDate = Optional.ofNullable(noticeSearchDto.getEndDate())
        .orElse(LocalDateTime.MAX);

    Page<Notice> notices = noticeRepository.findByTitleContainingAndContentContainingAndAuthorContainingAndCreatedAtBetween(
        title, content, author, startDate, endDate, pageable);
    return notices.stream()
        .map(NoticeMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public NoticeResponseDto updateNotice(Long id, NoticeUpdateDto noticeUpdateDto,
      List<MultipartFile> files) {
    Notice notice = noticeRepository.findById(id)
        .orElseThrow(() -> new NoticeNotFoundException("Notice not found with id " + id));
    NoticeMapper.updateEntity(noticeUpdateDto, notice);

    fileStorageService.deleteFilesByNotice(notice);

    List<File> attachments = fileStorageService.processFiles(files, notice).join();
    notice.update(notice.getTitle(), notice.getContent(), notice.getStartDate(),
        notice.getEndDate(), attachments);

    notice = noticeRepository.save(notice);
    return NoticeMapper.toDto(notice);
  }

  @Transactional
  public void deleteNotice(Long id) {
    Notice notice = noticeRepository.findById(id)
        .orElseThrow(() -> new NoticeNotFoundException("Notice not found with id " + id));
    notice.delete();
    noticeRepository.save(notice);
  }
}
