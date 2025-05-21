package org.zensnorlax.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zensnorlax.common.Result;
import org.zensnorlax.common.ResultEnum;
import org.zensnorlax.common.ZenException;
import org.zensnorlax.config.EmailClient;
import org.zensnorlax.config.JsonRedisTemplate;
import org.zensnorlax.config.JsonWebToken;
import org.zensnorlax.mapper.FollowMapper;
import org.zensnorlax.mapper.UserMapper;
import org.zensnorlax.model.pojo.Follow;
import org.zensnorlax.model.pojo.User;
import org.zensnorlax.model.vo.UserInfoVo;
import org.zensnorlax.model.vo.UserLoginVo;
import org.zensnorlax.model.vo.UserRegisterVo;
import org.zensnorlax.service.UserService;
import org.zensnorlax.util.AuthContextHolder;
import org.zensnorlax.util.VerificationCode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/24 10:53
 */

@SuppressWarnings("rawtypes")
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JsonRedisTemplate jsonRedisTemplate;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private JsonWebToken jsonWebToken;

    @Value("${cookie.expiration-time}")
    private Long cookieExpirationSecond;

    // 确认用户注册时输入的验证码
    @Transactional(rollbackFor = ZenException.class)
    @Override
    public Result confirmCode(UserRegisterVo userRegisterVo) {
        UserRegisterVo cachedUserRegisterVo = (UserRegisterVo) jsonRedisTemplate.opsForValue().get(userRegisterVo.getEmail());
        if (cachedUserRegisterVo == null) {
            return Result.error(ResultEnum.VERIFYCODE_EXPIRED);
        }
        if (!userRegisterVo.equals(cachedUserRegisterVo)) {
            return Result.error(ResultEnum.VERIFYCODE_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userRegisterVo, user);

        try {
            userMapper.insert(user);
        } catch (Exception e) {
            throw new ZenException("数据库操作失败", e);
        }
        return Result.success(null, "注册成功");
    }

    // 生成并发送验证码给用户
    @Override
    public Result generateCode(UserRegisterVo userRegisterVo) {
        try {
            if (userMapper.selectOne(new QueryWrapper<User>().eq("email", userRegisterVo.getEmail())) != null) {
                return Result.error(ResultEnum.USER_ALREADY_EXIST);
            }
        } catch (Exception e) {
            throw new ZenException("数据库操作失败", e);
        }
        userRegisterVo.setVerifyCode(VerificationCode.generateVerificationCode());
        emailClient.sendSimpleEmail(userRegisterVo.getEmail(), "Verify Code", userRegisterVo.getVerifyCode());
        jsonRedisTemplate.opsForValue().set(userRegisterVo.getEmail(), userRegisterVo, 10, TimeUnit.MINUTES);
        return Result.success(null, "验证码已发送至邮箱");
    }

    // 用户登录验证
    @Override
    public Result login(UserLoginVo userLoginVo, HttpServletResponse response) {
        try {
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().eq("email", userLoginVo.getEmail())
            );

            if (user == null) {
                return Result.error(ResultEnum.USER_NOT_EXIST);
            }
            if (!user.getPassword().equals(userLoginVo.getPassword())) {
                return Result.error(ResultEnum.USER_PASSWORD_ERROR);
            }

            String token = generateToken(user);

            // 创建 Cookie 并设置参数
            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setHttpOnly(true);    // 防止 JavaScript 访问
            tokenCookie.setPath("/");         // 设置 Cookie 路径
            tokenCookie.setMaxAge(Math.toIntExact(cookieExpirationSecond));      // Cookie 有效时间（秒）
            response.addCookie(tokenCookie);  // 添加 Cookie 到响应头

            Cookie uidCookie = new Cookie("uid", String.valueOf(user.getId()));
            uidCookie.setHttpOnly(true);
            uidCookie.setPath("/");
            uidCookie.setMaxAge(Math.toIntExact(cookieExpirationSecond));
            response.addCookie(uidCookie);

            return Result.success(null, "登录成功");
        } catch (Exception e) {
            throw new ZenException("数据库操作失败", e);
        }
    }

    // 获取用户信息
    @Override
    public Result getUserInfo(Long id) {
        User user = userMapper.selectById(id);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(user, userInfoVo);
        return Result.success(userInfoVo);
    }

    // 关注用户
    @Override
    public Result followUser(Long followeeId) {

        try {
            Long followerId = AuthContextHolder.getUserId();
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            followMapper.insert(follow);

        } catch (Exception e) {
            throw new ZenException("关注用户失败", e);
        }
        return Result.success(null, "关注成功");
    }

    // 生成 JSON Web Token
    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        return jsonWebToken.generateToken(claims);
    }

    // 取消关注用户
    @Override
    public Result unfollowUser(Long followeeId) {
        try {
            Long followerId = AuthContextHolder.getUserId();
            LambdaQueryWrapper<Follow> followQueryWrapper = new LambdaQueryWrapper<Follow>();
            followQueryWrapper.allEq(Map.of(Follow::getFollowerId, followerId, Follow::getFolloweeId, followeeId));
            followMapper.delete(followQueryWrapper);
        } catch (Exception e) {
            throw new ZenException("取消关注用户失败", e);
        }
        return Result.success(null, "取消关注成功");
    }

    // 查看关注用户列表
    @Override
    public Result getFollowList() {
        try {
            Long userId = AuthContextHolder.getUserId();
            LambdaQueryWrapper<Follow> followQueryWrapper = new LambdaQueryWrapper<Follow>();
            followQueryWrapper.eq(Follow::getFollowerId, userId);
            followQueryWrapper.select(Follow::getFolloweeId);
            return Result.success(followMapper.selectList(followQueryWrapper));
        } catch (Exception e) {
            throw new ZenException("获取关注列表失败", e);
        }
    }
}
