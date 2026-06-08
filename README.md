# 🌿 南昌大学心理健康服务预约系统

> 基于 Vue 3 + Spring Boot 的高校心理健康服务管理平台，支持学生预约咨询、咨询师管理、辅导员监督和系统管理四大角色。

![Vue 3](https://img.shields.io/badge/Vue-3.3-4FC08D?style=flat-square&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=spring-boot)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.3-409EFF?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5-red?style=flat-square)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=json-web-tokens)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

---

## ✨ 功能特性

### 🎓 学生端
- **心理测评** — 抑郁/焦虑量表自评，自动计算风险等级
- **预约咨询** — 浏览咨询师列表，选择时段在线预约
- **匿名留言** — 匿名倾诉心理困扰，保护隐私
- **测评历史** — 查看历次测评结果和趋势
- **心灵广场** — 浏览心理健康文章和学院活动

### 👩‍⚕️ 咨询师端
- **预约管理** — 确认/拒绝学生预约，查看预约日历
- **时段管理** — 设置可预约的时间段
- **咨询记录** — 记录咨询内容，评估风险等级
- **匿名回复** — 回复学生匿名留言
- **转介建议** — 向辅导员提交转介建议

### 🧑‍💼 辅导员端
- **心理预警** — 接收高风险学生预警，实时推送
- **重点关注** — 标记和跟踪重点关注学生
- **转介管理** — 处理咨询师转介建议
- **回访任务** — 创建和跟踪回访任务
- **学院统计** — 查看学院心理健康数据

### ⚙️ 管理员端
- **用户管理** — 增删改查用户，批量导入导出 Excel
- **咨询师审核** — 审核咨询师注册申请
- **文章管理** — 发布心理健康文章和活动通知
- **系统配置** — 管理系统参数配置
- **数据统计** — 预约趋势、学院分布、完成率等图表

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────┐
│                   前端 (Vue 3)               │
│  Vue 3 + Vue Router + Pinia + Element Plus  │
│  ECharts (数据图表) + Axios (HTTP 请求)       │
├─────────────────────────────────────────────┤
│                  后端 (Spring Boot)           │
│  Spring Boot 3 + MyBatis Plus + JWT          │
│  Redis (Token 缓存) + MySQL (数据持久化)       │
└─────────────────────────────────────────────┘
```

**后端技术栈：**
- Spring Boot 3.x — 应用框架
- MyBatis Plus — ORM 框架
- JWT — 身份认证
- Redis — Token 缓存、会话管理
- MySQL 8.0 — 数据库

**前端技术栈：**
- Vue 3 (Composition API + `<script setup>`)
- Vue Router 4 — 路由管理
- Pinia — 状态管理
- Element Plus — UI 组件库
- ECharts — 数据可视化
- Axios — HTTP 请求
- Vite — 构建工具

---

## 📁 项目结构

```
PsychologicalReservationSystem/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/ncu/mentalhealth/
│   │   ├── config/                   # 配置类 (JWT, CORS, MyBatis, Redis)
│   │   ├── controller/               # REST API 控制器
│   │   ├── entity/                   # 数据库实体类 (15 张表)
│   │   ├── mapper/                   # MyBatis Mapper 接口
│   │   ├── service/                  # 业务逻辑层
│   │   │   └── impl/                 # 服务实现类
│   │   └── MentalHealthApplication   # 启动类
│   └── src/main/resources/
│       ├── application.yml           # 应用配置
│       └── mapper/                   # MyBatis XML 映射文件
├── vue/                              # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 接口封装
│   │   ├── assets/                   # 静态资源
│   │   ├── components/               # 公共组件
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # Pinia 状态管理
│   │   ├── styles/                   # 全局主题样式
│   │   └── view/                     # 页面组件
│   │       ├── auth/                 # 登录/注册
│   │       ├── student/              # 学生端 (8 个页面)
│   │       ├── counselor/            # 咨询师端 (7 个页面)
│   │       ├── advisor/              # 辅导员端 (6 个页面)
│   │       ├── admin/                # 管理员端 (5 个页面)
│   │       └── shared/               # 共享页面
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── ncu_mental_health.sql             # 数据库初始化脚本 (含测试数据)
└── README.md
```

---

## 🚀 快速开始

### 环境要求

| 环境 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |
| Maven | 3.8+ |

### 1. 克隆项目

```bash
git clone https://github.com/zouhaonb/ncu-mental-health-appointment-system.git
cd ncu-mental-health-appointment-system
```

### 2. 初始化数据库

```bash
mysql -u root -p < ncu_mental_health.sql
```

脚本会自动创建 `ncu_mental_health` 数据库，包含 15 张表和测试数据。

### 3. 启动 Redis

```bash
redis-server
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 5. 启动前端

```bash
cd vue
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`。

### 6. 登录系统

打开浏览器访问 `http://localhost:3000`，使用以下测试账号：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `123456` |
| 咨询师 | `counselor1` ~ `counselor4` | `123456` |
| 学生 | `student1` ~ `student10` | `123456` |
| 辅导员 | `advisor1` ~ `advisor4` | `123456` |

---

## 🗄️ 数据库设计

系统包含 15 张核心数据表：

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `users` | 用户基础表 | username, password, role, name, college |
| `students` | 学生扩展表 | user_id, student_no, college, advisor_id |
| `counselors` | 咨询师扩展表 | user_id, title, specialization, is_active |
| `advisors` | 辅导员扩展表 | user_id, college, title |
| `appointments` | 预约表 | student_id, counselor_id, time_slot_id, status |
| `time_slots` | 可预约时段 | counselor_id, date, start_time, end_time |
| `consultation_records` | 咨询记录 | appointment_id, content, risk_level |
| `self_assessments` | 心理测评 | student_id, scale_type, total_score, risk_level |
| `anonymous_messages` | 匿名留言 | student_id, content, reply, status |
| `risk_alerts` | 风险预警 | student_id, advisor_id, risk_level |
| `referral_suggestions` | 转介建议 | advisor_id, student_id, assigned_counselor_id |
| `follow_up_tasks` | 回访任务 | student_id, advisor_id, status, feedback |
| `key_concern_students` | 重点关注 | advisor_id, student_id, notes |
| `articles` | 文章/活动 | title, content, type, college |
| `system_configs` | 系统配置 | config_key, config_value, description |

---

## 🔐 角色权限

| 功能 | 学生 | 咨询师 | 辅导员 | 管理员 |
|------|:----:|:------:|:------:|:------:|
| 心理测评 | ✅ | — | — | — |
| 预约咨询 | ✅ | ✅ | — | — |
| 匿名留言 | ✅ | ✅ | — | — |
| 咨询记录 | — | ✅ | — | — |
| 心理预警 | — | — | ✅ | — |
| 重点关注 | — | — | ✅ | — |
| 用户管理 | — | — | — | ✅ |
| 咨询师审核 | — | — | — | ✅ |
| 文章管理 | ✅(查看) | ✅(查看) | ✅(查看) | ✅ |
| 数据统计 | — | — | ✅ | ✅ |

---

## 📄 License

[MIT License](LICENSE) © 2026 南昌大学心理健康教育中心
