package com.Emanon.repository;

import com.Emanon.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 根據發布者查詢
    List<Board> findByAuthor(String author);

    // 模糊查詢標題（LIKE %keyword%）
    List<Board> findByTitleContaining(String keyword);

    // 查找還沒過期的公告（deadline > now）
    Page<Board> findByDeadlineAfter(LocalDate date, Pageable pageable);

    // 根據標題和發布者查（複合條件）
    List<Board> findByTitleContainingAndAuthor(String keyword, String author);

    // 根據截止日期排序
    List<Board> findAllByOrderByDeadlineDesc();
}
