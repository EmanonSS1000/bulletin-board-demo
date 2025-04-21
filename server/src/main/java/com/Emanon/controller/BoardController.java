package com.Emanon.controller;

import com.Emanon.config.FileStorageConfiguration;
import com.Emanon.dto.BoardDTO;
import com.Emanon.entity.User;
import com.Emanon.result.Result;
import com.Emanon.service.BoardService;
import com.Emanon.vo.BoardVO;
import io.swagger.annotations.Api;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/boards")
@Slf4j
@Api(tags = "布告欄相關接口")
public class BoardController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;  // 引入文件儲存設定

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * 新增一筆公告
     */
    @PostMapping("/create")
    public Result<String> createBoard(@RequestBody BoardDTO dto, HttpSession session) {
        try{
            boardService.createBoard(dto);
            messagingTemplate.convertAndSend("/topic/board", "new");
            return Result.success("新增成功");
        } catch (Exception e) {
            return Result.error("新增失敗：" + e.getMessage());
        }

    }

    /**
     * 查詢所有未過期公告（deadline > 現在）
     * @return
     */
    @GetMapping("/active")
    public Result<Page<BoardVO>> getActiveBoards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<BoardVO> boards = boardService.getActiveBoards(page, size);
        return Result.success(boards);
    }

    /**
     * 根據作者查詢公告
     * @param author
     * @return
     */
    @GetMapping("/author/{author}")
    public Result<List<BoardVO>> getBoardsByAuthor(@PathVariable("author") String author) {
        if (author == null || author.trim().isEmpty()) {
            return Result.error("缺少作者名稱");
        }
        List<BoardVO> boards = boardService.getBoardsByAuthor(author);
        return Result.success(boards);
    }

    /**
     * 根據 ID 查公告
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<BoardVO> getBoardById(@PathVariable("id") Long id) {
        BoardVO board = boardService.getBoardById(id);
        return Result.success(board);
    }
    /**
     * 模糊查詢標題
     * @param keyword
     * @return
     */
    @GetMapping("/search")
    public Result<List<BoardVO>> searchBoards(@RequestParam String keyword) {
        List<BoardVO> boards = boardService.searchBoardsByTitle(keyword);
        return Result.success(boards);
    }

    /**
     * 更新公告
     * @param id
     * @param dto
     * @param session
     * @return
     */
    @PutMapping("/{id}")
    public Result<String> updateBoard(@PathVariable("id") Long id,
                                      @RequestBody BoardDTO dto,
                                      HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登入");
        }

        dto.setAuthor(user.getUsername()); // 避免讓前端亂改作者
        boardService.updateBoard(id, dto);
        messagingTemplate.convertAndSend("/topic/board", "update");

        return Result.success("更新成功");
    }

    /**
     * 刪除
     * @param id
     * @return
     */
    @DeleteMapping("/D/{id}")
    public Result<Void> deleteBoard(@PathVariable("id") Long id) {
        boardService.deleteBoard(id);
        messagingTemplate.convertAndSend("/topic/board", "delete");

        return Result.success();
    }

    /**
     * 檔案上傳
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("檔案為空");
        }

        String uploadPath = fileStorageConfiguration.getUploadDir();

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();  // 如果資料夾不存在，則創建
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;
        File dest = new File(uploadDir, newFilename);

        try {
            file.transferTo(dest);  // 儲存檔案
        } catch (IOException e) {
            log.error("檔案儲存失敗", e);
            return Result.error("檔案上傳失敗");
        }

        // 回傳檔案相對路徑
        String relativePath =  newFilename;
        return Result.success(relativePath);
    }

    /**
     * 下載檔案
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path path = Paths.get(fileStorageConfiguration.getUploadDir(), filename);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("檔案下載失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
