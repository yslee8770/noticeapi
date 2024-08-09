package com.example.noticeapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDto {

  private Long id;
  private String originalFileName;
  private String storedFileName;
  private String filePath;
}
