package com.generator.rental.config;

import com.generator.rental.entity.User;
import com.generator.rental.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    private static final String AUTH_TOKEN_PREFIX = "auth:token:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Support legacy token in header if needed, or just Bearer
            token = request.getHeader("X-Auth-Token");
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            Object userIdObj = redisTemplate.opsForValue().get(AUTH_TOKEN_PREFIX + token);
            Object userIdObj = null;
            try {
                userIdObj = redisTemplate.opsForValue().get(AUTH_TOKEN_PREFIX + token);
            } catch (Exception e) {
                log.error("Redis 连接异常，跳过检验：{}", e.getMessage());
            }
            if (userIdObj != null) {
                Long userId = Long.valueOf(userIdObj.toString());
                User user = userMapper.selectById(userId);

                if (user != null && user.getStatus() == User.Status.ACTIVE) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
