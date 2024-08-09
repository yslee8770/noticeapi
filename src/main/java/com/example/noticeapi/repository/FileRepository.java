package com.example.noticeapi.repository;

import com.example.noticeapi.entity.File;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

  List<File> findByNoticeId(Long noticeId);
}
