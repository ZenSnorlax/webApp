package org.zensnorlax.service;

import jakarta.servlet.http.HttpServletResponse;
import org.zensnorlax.common.Result;
import org.zensnorlax.model.vo.UserLoginVo;
import org.zensnorlax.model.vo.UserRegisterVo;

@SuppressWarnings("rawtypes")
public interface UserService {
    Result confirmCode(UserRegisterVo userRegisterVo);

    Result generateCode(UserRegisterVo userRegisterVo);

    // 用户登录验证
    Result login(UserLoginVo userRegisterVo, HttpServletResponse response);

    Result getUserInfo(Long id);

    /**
     * 关注用户
     *
     * @param followeeId 关注者ID
     * @return 关注结果
     */
    Result followUser(Long followeeId);

    /**
     * @param followeeId 关注者ID
     * @return 取消关注结果
     */
    Result unfollowUser(Long followeeId);

    /**
     * 查看关注列表
     *
     * @return 关注列表
     */
    Result getFollowList();
}