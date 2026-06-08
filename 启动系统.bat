@echo off
chcp 65001 >nul 2>&1
title 心理健康预约系统 - 启动中...

echo ========================================
echo    南昌大学心理健康服务预约系统 启动脚本
echo ========================================
echo.

:: 检查 MySQL 是否在运行
echo [1/4] 检查 MySQL 服务...
mysql -u root -p990921 -e "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] MySQL 未运行或连接失败！
    echo 请确保 MySQL 已启动，用户名: root，密码: 990921
    echo.
    pause
    exit /b 1
)
echo [√] MySQL 连接正常

:: 检查 Redis 是否在运行
echo [2/4] 检查 Redis 服务...
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] Redis 未运行，正在尝试启动...
    start "" redis-server
    timeout /t 3 /nobreak >nul
    redis-cli ping >nul 2>&1
    if %errorlevel% neq 0 (
        echo [错误] Redis 启动失败，请手动启动 Redis
        pause
        exit /b 1
    )
)
echo [√] Redis 连接正常

:: 检查并创建数据库
echo [3/4] 初始化数据库...
mysql -u root -p990921 -e "CREATE DATABASE IF NOT EXISTS ncu_mental_health DEFAULT CHARACTER SET utf8mb4" 2>nul
mysql -u root -p990921 ncu_mental_health < "%~dp0ncu_mental_health.sql" 2>nul
if %errorlevel% neq 0 (
    echo [提示] 数据库可能已存在，继续启动...
) else (
    echo [√] 数据库初始化完成
)
echo [√] 数据库就绪

:: 启动后端
echo [4/4] 启动后端 (端口 8080)...
echo.
echo ========================================
echo   后端启动中，请等待...
echo   前端将自动打开 http://localhost:3000
echo   按 Ctrl+C 可停止所有服务
echo ========================================
echo.

:: 在新窗口启动后端
start "后端 - Spring Boot" cmd /k "cd /d %~dp0 && mvn spring-boot:run"

:: 等待后端启动
echo 等待后端服务启动 (约30秒)...
timeout /t 30 /nobreak >nul

:: 在新窗口启动前端
start "前端 - Vue" cmd /k "cd /d %~dp0vue && npm run dev"

:: 打开浏览器
timeout /t 5 /nobreak >nul
start http://localhost:3000

echo.
echo [√] 所有服务已启动！
echo.
echo   前端: http://localhost:3000
echo   后端: http://localhost:8080
echo.
echo   测试账号 (密码均为 123456):
echo   -----------------------------------------
echo   管理员:  admin
echo   学生:    student1 ~ student10
echo   咨询师:  counselor1 ~ counselor4
echo   辅导员:  advisor1 ~ advisor4
echo   -----------------------------------------
echo.
echo 关闭此窗口不会停止服务，需要分别关闭"后端"和"前端"窗口。
pause
