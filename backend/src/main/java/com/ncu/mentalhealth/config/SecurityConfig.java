package com.ncu.mentalhealth.config;

import com.ncu.mentalhealth.mapper.UserMapper;
import com.ncu.mentalhealth.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserMapper userMapper;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(userMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configure(http))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 登录注册 所有人都能访问
                        .requestMatchers("/api/auth/**").permitAll()

                        // 文章、图片、静态资源
                        .requestMatchers("/api/articles/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // 辅导员可以访问学生咨询师列表接口（必须在/api/student/**之前）
                        .requestMatchers("/api/student/counselors").hasAnyAuthority("ROLE_STUDENT", "ROLE_ADVISOR")

                        // ✅ 管理员接口（使用 ROLE_ 前缀）
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // ✅ 学生接口（使用 ROLE_ 前缀）
                        .requestMatchers("/api/student/**").hasAuthority("ROLE_STUDENT")

                        // ✅ 咨询师接口（使用 ROLE_ 前缀）
                        .requestMatchers("/api/counselor/**").hasAuthority("ROLE_COUNSELOR")

                        // ✅ 辅导员接口（使用 ROLE_ 前缀）
                        .requestMatchers("/api/advisor/**").hasAuthority("ROLE_ADVISOR")

                        // 其他接口必须登录
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}