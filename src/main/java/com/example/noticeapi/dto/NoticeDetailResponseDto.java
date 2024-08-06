package com.example.noticeapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeDetailResponseDto {

  private Long id;
  private String title;
  private String content;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createdAt;
  private String author;
  private List<FileDto> attachments;
}
