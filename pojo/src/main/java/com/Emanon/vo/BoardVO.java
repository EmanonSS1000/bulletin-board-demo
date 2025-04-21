package com.Emanon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardVO {
    private Long id;

    private String title;

    private String author;

    private String content;

    private LocalDate publishDate;

    private LocalDate deadline;

    // 回傳給前端可下載的 URL 或檔名
    private String fileUrl;
}
