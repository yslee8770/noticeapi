package com.example.noticeapi.repository;

import com.example.noticeapi.entity.Notice;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

  Page<Notice> findByIsDeletedFalse(Pageable pageable);

  Page<Notice> findByTitleContainingAndContentContainingAndAuthorContainingAndCreatedAtBetween(
      String title, String content, String author, LocalDateTime startDate, LocalDateTime endDate,
      Pageable pageable);
}
