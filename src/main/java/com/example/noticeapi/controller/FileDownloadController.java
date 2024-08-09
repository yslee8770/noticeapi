package com.example.noticeapi.controller;

import com.example.noticeapi.dto.FileDto;
import com.example.noticeapi.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files/download")
public class FileDownloadController {

  private final FileStorageService fileStorageService;

  @GetMapping("/{fileId}")
  public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
    FileDto fileDto = fileStorageService.getFileDtoById(fileId);
    Resource resource = fileStorageService.loadFileAsResource(fileDto.getStoredFileName()).join();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + fileDto.getOriginalFileName() + "\"")
        .body(resource);
  }
}
