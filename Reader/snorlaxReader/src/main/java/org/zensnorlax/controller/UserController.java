package org.zensnorlax.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zensnorlax.common.CheckCookie;
import org.zensnorlax.common.Result;
import org.zensnorlax.model.vo.UserLoginVo;
import org.zensnorlax.model.vo.UserRegisterVo;
import org.zensnorlax.service.UserService;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/24 10:49
 */

@Slf4j
@RestController
@RequestMapping("/user")
@SuppressWarnings("rawtypes")
@CrossOrigin(origins = "http://localhost:1421", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    //确认注册验证码
    @PostMapping("/register/confirm_code")
    public Result confirmCode(@RequestBody UserRegisterVo userRegisterVo) {
        return userService.confirmCode(userRegisterVo);
    }

    //生成注册验证码
    @PostMapping("/register/generate_code")
    public Result generateCode(@RequestBody UserRegisterVo userRegisterVo) {
        return userService.generateCode(userRegisterVo);
    }

    //用户登录
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginVo userLoginVo, HttpServletResponse response) {
        return userService.login(userLoginVo, response);
    }

    //检测cookie是否有效
    @CheckCookie
    @GetMapping("/check_cookie")
    public Result checkCookie() {
        return Result.success(null, "cookie is valid");
    }

    //获取用户信息
    @CheckCookie
    @GetMapping("/{id}/info")
    public Result getUserInfo(@PathVariable("id") Long id) {
        return userService.getUserInfo(id);
    }

    //关注用户
    @CheckCookie
    @PostMapping("/follow/{followee_id}")
    public Result followUser(@PathVariable Long followee_id) {
        return userService.followUser(followee_id);
    }

    //取消关注用户
    @CheckCookie
    @DeleteMapping("/follow/{followee_id}")
    public Result unfollowUser(@PathVariable Long followee_id) {
        return userService.unfollowUser(followee_id);
    }

    //查看关注用户列表
    @CheckCookie
    @GetMapping("/follow/list")
    public Result getFollowList() {
        return userService.getFollowList();
    }
}


