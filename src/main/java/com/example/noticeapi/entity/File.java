package com.example.noticeapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String originalFileName;
  private String storedFileName;
  private String filePath;
  private boolean isDeleted;

  @ManyToOne
  @JoinColumn(name = "notice_id")
  private Notice notice;

  public void delete() {
    this.isDeleted = true;
  }

  public void associateWithNotice(Notice notice) {
    this.notice = notice;
  }
}
