package org.zensnorlax.common;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zensnorlax.config.JsonWebToken;
import org.zensnorlax.util.AuthContextHolder;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/23 21:25
 */
@Aspect
@Component
@Slf4j
public class ZenAspect {

    @Autowired
    private JsonWebToken jsonWebToken;

    /**
     * 环绕通知，用于在调用 controller 层方法前后进行 Token 验证
     */
    @Around(
            "execution(* org.zensnorlax.controller..*(..)) " +
                    "&& @annotation(checkCookie)"
    )
    public Object validateCookie(ProceedingJoinPoint joinPoint, CheckCookie checkCookie) throws Throwable {
        // 0. 判断是否需要 Token
        boolean isRequired = checkCookie.required();
        if (!isRequired) {
            return joinPoint.proceed();
        }
        // 1. 获取请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 2. 解析 Token
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null || token.isEmpty()) {
            return Result.error(ResultEnum.NOT_LOGIN);
        }

        // 3 获取用户 ID 失败，抛出异常
        try {
            Long userId = jsonWebToken.extractClaim(token, "id", Long.class);
            AuthContextHolder.setUserId(userId);
        } catch (ExpiredJwtException e) {
            return Result.error(ResultEnum.TOKEN_EXPIRED);
        } catch (Exception e) {
            return Result.error(ResultEnum.TOKEN_ERROR);
        }
        return joinPoint.proceed();

    }
}
