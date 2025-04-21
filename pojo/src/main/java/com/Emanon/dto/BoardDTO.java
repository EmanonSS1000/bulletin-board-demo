package com.Emanon.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BoardDTO {
    private String title;

    private String author;

    private String content;

    private LocalDate deadline;

    // 這裡是前端上傳的檔案（multipart/form-data）
    private String filePath;
}
