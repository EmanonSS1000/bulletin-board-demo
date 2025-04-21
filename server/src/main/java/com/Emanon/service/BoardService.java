package com.Emanon.service;

import com.Emanon.dto.BoardDTO;
import com.Emanon.entity.Board;
import com.Emanon.repository.BoardRepository;
import com.Emanon.vo.BoardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    //新增一筆公告。
    public void createBoard(BoardDTO dto) {
        Board board = new Board();
        board.setTitle(dto.getTitle());
        board.setAuthor(dto.getAuthor());
        board.setContent(dto.getContent());
        board.setDeadline(dto.getDeadline());
        board.setPublishDate(LocalDate.now()); // 自動設當下時間
        board.setFilePath(dto.getFilePath());
        // 檔案處理未實作
        boardRepository.save(board);
    }
    // 取得尚未過期的公告（deadline > 現在時間），並處理分頁。
    public Page<BoardVO> getActiveBoards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());
        Page<Board> boardPage = boardRepository.findByDeadlineAfter(LocalDate.now(), pageable);

        return boardPage.map(this::toVO); // 使用 map 方法轉換為 VO
    }
    //根據作者名稱查詢公告。
    public List<BoardVO> getBoardsByAuthor(String author) {
        List<Board> entities = boardRepository.findByAuthor(author);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }
    public BoardVO getBoardById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        return toVO(board);
    }
    //更新指定 ID 的公告。
    public void updateBoard(Long id, BoardDTO dto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到公告 ID：" + id));

        board.setTitle(dto.getTitle());
        board.setAuthor(dto.getAuthor());
        board.setContent(dto.getContent());
        board.setDeadline(dto.getDeadline());
        board.setPublishDate(LocalDate.now());
        board.setFilePath(dto.getFilePath());


        boardRepository.save(board); // 儲存變更
    }
    //根據 ID 刪除公告。
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }
    //根據標題關鍵字模糊查詢公告。
    public List<BoardVO> searchBoardsByTitle(String keyword) {
        List<Board> entities = boardRepository.findByTitleContaining(keyword);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }
    //將 Entity 轉換為 VO（回傳前端用）。
    private BoardVO toVO(Board entity) {
        BoardVO vo = new BoardVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setAuthor(entity.getAuthor());
        vo.setContent(entity.getContent());
        vo.setPublishDate(entity.getPublishDate());
        vo.setDeadline(entity.getDeadline());
        vo.setFileUrl(entity.getFilePath());
        return vo;
    }



}
