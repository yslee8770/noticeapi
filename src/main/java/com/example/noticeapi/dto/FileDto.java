package com.example.noticeapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDto {

  private Long id;
  private String originalFileName;
  private String storedFileName;
  private String filePath;
}
