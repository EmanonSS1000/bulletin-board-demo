package com.Emanon.handler;

import com.Emanon.constant.MessageConstant;
import com.Emanon.exception.BaseException;
import com.Emanon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，统一处理项目中的各種異常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 處理自訂業務異常
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 處理資料庫唯一鍵違反異常（如：帳號重複）
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("异常信息：{}", ex.getMessage());
        String errorMsg = ex.getMessage();
        if (errorMsg.contains("Duplicate entry")) {
            String[] errorList = errorMsg.split(" ");
            return Result.error(errorList[2] + MessageConstant.ALREADY_EXISTS);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 處理所有未捕捉的其他異常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleOtherExceptions(Exception ex) {
        log.error("未知異常：", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
