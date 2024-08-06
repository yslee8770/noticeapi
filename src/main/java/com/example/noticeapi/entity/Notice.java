package com.example.noticeapi.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;
  private String content;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createdAt;
  private int viewCount;
  private String author;
  private boolean isDeleted;

  @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<File> attachments = new ArrayList<>();

  public void update(String title, String content, LocalDateTime startDate, LocalDateTime endDate,
      List<File> attachments) {
    this.title = title;
    this.content = content;
    this.startDate = startDate;
    this.endDate = endDate;
    this.attachments.clear();
    this.attachments.addAll(attachments);
    for (File attachment : this.attachments) {
      attachment.associateWithNotice(this);
    }
  }

  public void delete() {
    this.isDeleted = true;
    if (this.attachments != null) {
      this.attachments.forEach(File::delete);
    }
  }
}
