package com.ncu.mentalhealth.config;

import com.ncu.mentalhealth.service.TokenBlacklistService;
import com.ncu.mentalhealth.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("Token已被加入黑名单 - URI: {}", uri);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
                    return;
                }
                
                if (jwtUtils.validateToken(token)) {
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);
                    
                    log.info("========== JWT认证调试信息 ==========");
                    log.info("请求URI: {}", uri);
                    log.info("UserId: {}", userId);
                    log.info("Role from token: '{}'", role);
                    log.info("Role长度: {}", role != null ? role.length() : 0);
                    
                    String authority = role;
                    if (!role.startsWith("ROLE_")) {
                        authority = "ROLE_" + role;
                    }
                    log.info("最终权限: '{}'", authority);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userId, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority(authority))
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("认证成功，已设置权限: {}", authentication.getAuthorities());
                    log.info("========================================");
                } else {
                    log.warn("JWT验证失败 - URI: {}", uri);
                }
            } catch (Exception e) {
                log.error("JWT认证失败 - URI: {}, Error: {}", uri, e.getMessage(), e);
            }
        } else {
            log.debug("请求未携带Token - URI: {}", uri);
        }
        
        filterChain.doFilter(request, response);
    }

}
