package com.Emanon.controller;


import com.Emanon.dto.LoginDTO;
import com.Emanon.entity.User;
import com.Emanon.result.Result;
import com.Emanon.service.UserService;
import com.Emanon.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@Api(tags = "登入相關接口")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation(value = "登入")
    public Result<String> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        log.info("登录：{}", loginDTO);

        User user = userService.findByUsername(loginDTO.getUsername());

        if (user == null) {
            // 沒有帳號，自動註冊
            user = userService.createUser(loginDTO.getUsername(), loginDTO.getPassword());
            session.setAttribute("user", user);
            return Result.success("帳號已自動註冊並登入成功");
        }

        // 有帳號，檢查密碼
        if (user.getPassword().equals(loginDTO.getPassword())) {
            session.setAttribute("user", user);
            log.info("/login:Session 中的用户：{}", user);
            return Result.success("登入成功");
        } else {
            return Result.error("密碼錯誤");
        }

//        if ("admin".equals(loginDTO.getUsername()) && "1234".equals(loginDTO.getPassword())) {
//            session.setAttribute("user", new UserVO("admin", "admin"));
//            return Result.success("登入成功");
//        }
//        return Result.error("帳號或密碼錯誤");
    }

    @GetMapping("/me")
    public Result<String> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        log.info("/me:Session 中的用户：{}", user);
        if (user != null) {
            return Result.success(user.getUsername());
        } else {
            return Result.error("未登入");
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "已登出";
    }

}
