package com.example.noticeapi.mapper;

import com.example.noticeapi.dto.FileDto;
import com.example.noticeapi.dto.NoticeCreateDto;
import com.example.noticeapi.dto.NoticeDetailResponseDto;
import com.example.noticeapi.dto.NoticeResponseDto;
import com.example.noticeapi.dto.NoticeUpdateDto;
import com.example.noticeapi.entity.Notice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class NoticeMapper {

  public static Notice toEntity(NoticeCreateDto dto) {
    return Notice.builder()
        .title(dto.getTitle())
        .content(dto.getContent())
        .startDate(dto.getStartDate())
        .endDate(dto.getEndDate())
        .createdAt(LocalDateTime.now())
        .viewCount(0)
        .author(dto.getAuthor())
        .isDeleted(false)
        .attachments(new ArrayList<>())
        .build();
  }

  public static void updateEntity(NoticeUpdateDto dto, Notice notice) {
    notice.update(dto.getTitle(), dto.getContent(), dto.getStartDate(), dto.getEndDate(),
        notice.getAttachments());
  }

  public static NoticeResponseDto toDto(Notice notice) {
    return NoticeResponseDto.builder()
        .id(notice.getId())
        .title(notice.getTitle())
        .content(notice.getContent())
        .startDate(notice.getStartDate())
        .endDate(notice.getEndDate())
        .createdAt(notice.getCreatedAt())
        .viewCount(notice.getViewCount())
        .author(notice.getAuthor())
        .attachments(notice.getAttachments().stream()
            .map(file -> FileDto.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .storedFileName(file.getStoredFileName())
                .filePath(file.getFilePath())
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  public static NoticeDetailResponseDto toDetailDto(Notice notice) {
    return NoticeDetailResponseDto.builder()
        .id(notice.getId())
        .title(notice.getTitle())
        .content(notice.getContent())
        .startDate(notice.getStartDate())
        .endDate(notice.getEndDate())
        .createdAt(notice.getCreatedAt())
        .author(notice.getAuthor())
        .attachments(notice.getAttachments().stream()
            .map(file -> FileDto.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .storedFileName(file.getStoredFileName())
                .filePath(file.getFilePath())
                .build())
            .collect(Collectors.toList()))
        .build();
  }
}
