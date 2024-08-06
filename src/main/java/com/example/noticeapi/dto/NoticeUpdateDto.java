package com.example.noticeapi.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeUpdateDto {

  private String title;
  private String content;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
