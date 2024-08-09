package com.example.noticeapi.service;

import com.example.noticeapi.dto.FileDto;
import com.example.noticeapi.entity.File;
import com.example.noticeapi.entity.Notice;
import com.example.noticeapi.exception.FileStorageException;
import com.example.noticeapi.exception.InvalidFileNameException;
import com.example.noticeapi.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

  private Path fileStorageLocation;

  private final FileRepository fileRepository;

  public FileStorageService(@Value("${file.storage.location}") String fileStorageLocationStr,
      FileRepository fileRepository) {
    this.fileStorageLocation = Paths.get(fileStorageLocationStr).toAbsolutePath().normalize();
    this.fileRepository = fileRepository;
    init();
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new FileStorageException("Could not create the directory", ex);
    }
  }

  public CompletableFuture<String> storeFile(MultipartFile file) {
    return CompletableFuture.supplyAsync(() -> {
      String originalFileName = file.getOriginalFilename();
      String fileExtension = "";

      if (originalFileName != null && originalFileName.contains(".")) {
        fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
      }

      String storedFileName = UUID.randomUUID() + fileExtension;

      try {
        validateFileName(originalFileName);
        Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetLocation);
        return storedFileName;
      } catch (IOException ex) {
        throw new FileStorageException(
            "Could not store file " + originalFileName + ". Please try again!", ex);
      }
    });
  }

  private void validateFileName(String fileName) {
    if (fileName == null || fileName.contains("..")) {
      throw new InvalidFileNameException(
          "Filename contains invalid path sequence or is null: " + fileName);
    }
  }

  @Transactional
  public CompletableFuture<List<File>> processFiles(List<MultipartFile> files, Notice notice) {
    List<CompletableFuture<File>> futures = files.stream()
        .map(file -> storeFile(file).thenApply(storedFileName -> {
          File attachment = File.builder()
              .originalFileName(file.getOriginalFilename())
              .storedFileName(storedFileName)
              .filePath(fileStorageLocation.resolve(storedFileName).toString())
              .isDeleted(false)
              .notice(notice)
              .build();
          fileRepository.save(attachment);
          return attachment;
        }))
        .collect(Collectors.toList());

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
  }

  @Transactional
  @CacheEvict(value = "files", key = "#notice.id")
  public CompletableFuture<Void> deleteFilesByNotice(Notice notice) {
    return CompletableFuture.runAsync(() -> {
      List<File> attachments = fileRepository.findByNoticeId(notice.getId());
      for (File attachment : attachments) {
        attachment.delete();
        fileRepository.save(attachment);
        try {
          deletePhysicalFile(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
          throw new FileStorageException("Could not delete file " + attachment.getStoredFileName(),
              e);
        }
      }
    });
  }

  protected void deletePhysicalFile(Path path) throws IOException {
    Files.deleteIfExists(path);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "files", key = "#storedFileName")
  public CompletableFuture<Resource> loadFileAsResource(String storedFileName) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
          return resource;
        } else {
          throw new FileStorageException("File not found " + storedFileName);
        }
      } catch (Exception ex) {
        throw new FileStorageException("File not found " + storedFileName, ex);
      }
    });
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "fileDto", key = "#fileId")
  public FileDto getFileDtoById(Long fileId) {
    File file = fileRepository.findById(fileId)
        .orElseThrow(() -> new FileStorageException("File not found with id " + fileId));
    return FileDto.builder()
        .id(file.getId())
        .originalFileName(file.getOriginalFileName())
        .storedFileName(file.getStoredFileName())
        .filePath(file.getFilePath())
        .build();
  }
}
