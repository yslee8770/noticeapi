package com.example.noticeapi.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSearchDto {

  private String title;
  private String content;
  private String author;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
