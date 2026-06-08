/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80016
 Source Host           : localhost:3306
 Source Schema         : ncu_mental_health

 Target Server Type    : MySQL
 Target Server Version : 80016
 File Encoding         : 65001

 Date: 29/05/2026 13:52:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for advisors
-- ----------------------------
DROP TABLE IF EXISTS `advisors`;
CREATE TABLE `advisors`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '负责学院',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `advisors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '辅导员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of advisors
-- ----------------------------
INSERT INTO `advisors` VALUES (1, 16, '信息工程学院', '学工办主任', '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `advisors` VALUES (2, 17, '理学院', '专职辅导员', '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `advisors` VALUES (3, 18, '人文学院', '学工办副主任', '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `advisors` VALUES (4, 19, '经济管理学院', '专职辅导员', '2026-05-20 22:05:24', '2026-05-20 22:05:24');

-- ----------------------------
-- Table structure for anonymous_messages
-- ----------------------------
DROP TABLE IF EXISTS `anonymous_messages`;
CREATE TABLE `anonymous_messages`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `counselor_id` bigint(20) NULL DEFAULT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `student_id`(`student_id`) USING BTREE,
  INDEX `counselor_id`(`counselor_id`) USING BTREE,
  CONSTRAINT `anonymous_messages_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `anonymous_messages_ibfk_2` FOREIGN KEY (`counselor_id`) REFERENCES `counselors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '匿名留言' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of anonymous_messages
-- ----------------------------
INSERT INTO `anonymous_messages` VALUES (3, 4, '学习压力大，我该怎么缓解？', '可以通过做适量的运动来缓解，或者做自己喜欢的事情来分散在学习上的注意力', 1, 'REPLIED', '2026-05-22 17:14:29', '2026-05-22 19:28:07');
INSERT INTO `anonymous_messages` VALUES (4, 1, '心情持续低落，没有食欲怎么办？', NULL, NULL, 'PENDING', '2026-05-22 19:21:04', '2026-05-22 19:21:04');

-- ----------------------------
-- Table structure for appointments
-- ----------------------------
DROP TABLE IF EXISTS `appointments`;
CREATE TABLE `appointments`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `counselor_id` bigint(20) NOT NULL,
  `time_slot_id` bigint(20) NOT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/COMPLETED/CANCELLED',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `cancel_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `time_slot_id`(`time_slot_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_counselor_id`(`counselor_id`) USING BTREE,
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`counselor_id`) REFERENCES `counselors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`time_slot_id`) REFERENCES `time_slots` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of appointments
-- ----------------------------
INSERT INTO `appointments` VALUES (6, 4, 2, 10, 'COMPLETED', '压力大，没有人聊天疏解，缺少朋友。', NULL, '2026-05-22 17:53:22', '2026-05-22 17:53:22');
INSERT INTO `appointments` VALUES (7, 2, 1, 9, 'COMPLETED', '长期情绪低落，提不起兴趣，对生活失去热情，怀疑自己有抑郁倾向', NULL, '2026-05-22 18:44:26', '2026-05-22 18:44:26');
INSERT INTO `appointments` VALUES (8, 1, 1, 9, 'COMPLETED', '情感问题', NULL, '2026-05-22 18:58:07', '2026-05-22 18:58:07');
INSERT INTO `appointments` VALUES (9, 9, 2, 14, 'COMPLETED', '辅导员转介：压力过大', NULL, '2026-05-25 19:11:13', '2026-05-25 19:11:13');
INSERT INTO `appointments` VALUES (10, 7, 3, 20, 'CONFIRMED', '辅导员转介：对职业方向没有规划，不确定未来发展道路', NULL, '2026-05-27 17:10:53', '2026-05-27 17:10:53');

-- ----------------------------
-- Table structure for articles
-- ----------------------------
DROP TABLE IF EXISTS `articles`;
CREATE TABLE `articles`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ARTICLE/NOTICE/ACTIVITY',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `author_id` bigint(20) NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `author_id`(`author_id`) USING BTREE,
  CONSTRAINT `articles_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章/通知' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of articles
-- ----------------------------
INSERT INTO `articles` VALUES (1, '如何应对考试焦虑', '考试焦虑可以通过呼吸训练、规划时间、调整认知有效缓解', 'ARTICLE', NULL, 1, '2026-05-20 22:05:25');
INSERT INTO `articles` VALUES (2, '认识抑郁症', '抑郁症不是心情不好，是需要科学干预的心理疾病', 'ARTICLE', NULL, 1, '2026-05-20 22:05:25');
INSERT INTO `articles` VALUES (3, '信息工程学院心理讲座', '5月25日举办压力管理主题讲座', 'ACTIVITY', '信息工程学院', 16, '2026-05-20 22:05:25');

-- ----------------------------
-- Table structure for consultation_records
-- ----------------------------
DROP TABLE IF EXISTS `consultation_records`;
CREATE TABLE `consultation_records`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `counselor_id` bigint(20) NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '咨询内容',
  `assessment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评估',
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LOW/MEDIUM/HIGH',
  `is_closed` int(11) NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_appointment`(`appointment_id`) USING BTREE,
  CONSTRAINT `consultation_records_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '咨询记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of consultation_records
-- ----------------------------
INSERT INTO `consultation_records` VALUES (2, 6, 4, 2, '来访者自述近期学习压力较大，情绪无处疏解，缺乏可以倾诉的朋友，感到孤独感明显。来访者表示当前没有出现明显的自伤、自杀念头，但长期的压力与孤独已对其情绪状态造成影响，表现为情绪低落、动力不足。', '来访者当前的困扰主要为发展性困扰，核心问题是压力管理与社交支持系统不足，无明显急性危机风险。来访者情绪稳定，认知清晰，有求助意愿，具备一定的问题解决能力，后续可通过压力管理技巧训练、社交技能辅导进行改善。', 'LOW', 0, '2026-05-22 18:17:32', '2026-05-22 18:17:32');
INSERT INTO `consultation_records` VALUES (3, 7, 2, 1, '来访者自述近一段时间持续情绪低落，对以往感兴趣的事情失去热情，学习和生活动力明显下降，日常活动难以维持。来访者怀疑自己存在抑郁倾向，情绪困扰已对其学习状态和人际交往造成影响，主动寻求专业支持。来访者认知清晰，求助意愿明确，无自伤、自杀相关表述。', '来访者当前表现符合抑郁情绪困扰的特征，核心问题为持续的低动力与情绪低落，有求助意愿，具备一定的自我觉察能力，无急性危机风险。建议后续通过认知行为取向的咨询，帮助其识别负性思维模式，重建日常行为节奏，同时可配合放松训练缓解情绪困扰。', 'MEDIUM', 0, '2026-05-22 18:45:39', '2026-05-22 18:45:39');
INSERT INTO `consultation_records` VALUES (4, 8, 1, 1, '来访者因恋爱关系问题长期情绪低落，近期经历争吵、冷战或分手，陷入自我怀疑与情绪内耗，学习与睡眠受到明显影响。来访者自述难以走出负面情绪，对亲密关系感到迷茫，担心影响后续的人际信任，主动寻求疏导。', '来访者的困扰属于亲密关系发展性问题，当前情绪反应与事件相关，无急性危机风险。来访者有求助意愿，具备情绪调节的基础能力，核心困扰为分手适应与自我价值重建。后续可通过情绪疏导、认知调整帮助其走出情绪低谷，重建对亲密关系的合理认知。', 'LOW', 0, '2026-05-22 19:12:25', '2026-05-22 19:12:25');
INSERT INTO `consultation_records` VALUES (5, 9, 9, 2, '本次咨询中，来访者主诉近期学业任务集中、考试压力较大，伴随焦虑、注意力难以集中、入睡困难等表现。来访者自述因担心成绩和未来发展，长期处于紧张状态，无法有效放松，且缺乏倾诉对象，情绪无处疏解。咨询师首先通过倾听与共情，帮助来访者梳理压力来源，肯定其当前的努力与感受，缓解其自我否定的倾向。随后，共同探讨了压力管理策略，包括任务拆解、时间规划、腹式呼吸放松法等，并引导来访者识别自身的不合理信念，建立更合理的认知模式。咨询过程中来访者情绪逐渐平稳，能够主动表达自身感受，并对后续的调整计划表现出积极态度。', '1. 按本次咨询中制定的计划，尝试每日任务拆解与时间管理；\n2. 坚持每日 5-10 分钟的呼吸放松练习，缓解睡前焦虑；\n3. 若情绪状态持续不佳或出现明显睡眠、饮食问题，建议及时预约后续咨询；\n4. 可与辅导员沟通，寻求学业上的支持与帮助。', 'LOW', 0, '2026-05-27 16:18:55', '2026-05-27 16:18:55');

-- ----------------------------
-- Table structure for counselors
-- ----------------------------
DROP TABLE IF EXISTS `counselors`;
CREATE TABLE `counselors`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `specialization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '擅长领域',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简介',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_active` int(11) NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝理由',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `counselors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '咨询师表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counselors
-- ----------------------------
INSERT INTO `counselors` VALUES (1, 12, '高级心理咨询师', '情绪管理,学业压力,人际关系', '国家二级心理咨询师，从事高校心理咨询工作12年。', 'http://localhost:8080/uploads/avatars/c8e594d8-ae96-4e92-af3a-e7f4a48ca04f.jpg', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24', NULL);
INSERT INTO `counselors` VALUES (2, 13, '中级心理咨询师', '恋爱心理,职业规划,自我成长', '应用心理学硕士，擅长青少年心理辅导', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24', NULL);
INSERT INTO `counselors` VALUES (3, 14, '初级心理咨询师', '家庭关系,社交焦虑,新生适应', '临床心理学硕士，咨询风格温和包容', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24', NULL);
INSERT INTO `counselors` VALUES (4, 15, '高级心理咨询师', '危机干预,创伤辅导,正念减压', '注册心理师，危机干预领域有深入研究', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24', NULL);

-- ----------------------------
-- Table structure for follow_up_tasks
-- ----------------------------
DROP TABLE IF EXISTS `follow_up_tasks`;
CREATE TABLE `follow_up_tasks`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `advisor_id` bigint(20) NOT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `student_id`(`student_id`) USING BTREE,
  INDEX `idx_advisor_id`(`advisor_id`) USING BTREE,
  CONSTRAINT `follow_up_tasks_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `follow_up_tasks_ibfk_2` FOREIGN KEY (`advisor_id`) REFERENCES `advisors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '回访任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follow_up_tasks
-- ----------------------------
INSERT INTO `follow_up_tasks` VALUES (1, 1, 1, 'PENDING', '学生已加入重点关注列表，请及时跟进回访。备注：风险预警：自测，风险等级：高风险，类型：焦虑', '2026-05-25 17:06:38', '2026-05-25 17:06:38');
INSERT INTO `follow_up_tasks` VALUES (2, 2, 1, 'PENDING', '学生已加入重点关注列表，请及时跟进回访。备注：风险预警：咨询师评估，风险等级：中风险，类型：咨询评估', '2026-05-25 17:06:38', '2026-05-25 17:06:38');
INSERT INTO `follow_up_tasks` VALUES (4, 9, 1, 'COMPLETED', '因学习太过于焦虑', '2026-05-25 17:53:07', '2026-05-25 17:53:07');
INSERT INTO `follow_up_tasks` VALUES (5, 2, 1, 'PENDING', '学生已加入重点关注列表，请及时跟进回访。备注：风险预警：自测，风险等级：中风险，类型：抑郁', '2026-05-25 18:00:05', '2026-05-25 18:00:05');
INSERT INTO `follow_up_tasks` VALUES (6, 7, 4, 'COMPLETED', '因对职业方向没有规划，不确定未来发展道路而焦虑', '2026-05-27 16:55:00', '2026-05-27 16:55:00');

-- ----------------------------
-- Table structure for key_concern_students
-- ----------------------------
DROP TABLE IF EXISTS `key_concern_students`;
CREATE TABLE `key_concern_students`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advisor_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_advisor_student`(`advisor_id`, `student_id`) USING BTREE,
  INDEX `student_id`(`student_id`) USING BTREE,
  CONSTRAINT `key_concern_students_ibfk_1` FOREIGN KEY (`advisor_id`) REFERENCES `advisors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `key_concern_students_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '重点关注学生' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of key_concern_students
-- ----------------------------
INSERT INTO `key_concern_students` VALUES (1, 1, 1, '风险预警：自测，风险等级：高风险，类型：焦虑', '2026-05-25 16:57:24');
INSERT INTO `key_concern_students` VALUES (3, 1, 9, '风险预警：自测，风险等级：中风险，类型：焦虑', '2026-05-25 17:53:07');
INSERT INTO `key_concern_students` VALUES (4, 1, 2, '风险预警：自测，风险等级：中风险，类型：抑郁', '2026-05-25 18:00:05');
INSERT INTO `key_concern_students` VALUES (5, 4, 7, '风险预警：自测，风险等级：高风险，类型：焦虑', '2026-05-27 16:55:00');

-- ----------------------------
-- Table structure for referral_suggestions
-- ----------------------------
DROP TABLE IF EXISTS `referral_suggestions`;
CREATE TABLE `referral_suggestions`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advisor_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `assigned_counselor_id` bigint(20) NULL DEFAULT NULL,
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `handle_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `student_id`(`student_id`) USING BTREE,
  INDEX `idx_advisor_id`(`advisor_id`) USING BTREE,
  CONSTRAINT `referral_suggestions_ibfk_1` FOREIGN KEY (`advisor_id`) REFERENCES `advisors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `referral_suggestions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '转介建议' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of referral_suggestions
-- ----------------------------
INSERT INTO `referral_suggestions` VALUES (5, 1, 9, 2, '压力过大', 'ACCEPTED', '2026-05-25 19:08:51', '2026-05-25 19:08:51');
INSERT INTO `referral_suggestions` VALUES (6, 4, 7, 3, '对职业方向没有规划，不确定未来发展道路', 'ACCEPTED', '2026-05-27 16:57:08', '2026-05-27 16:57:08');

-- ----------------------------
-- Table structure for risk_alerts
-- ----------------------------
DROP TABLE IF EXISTS `risk_alerts`;
CREATE TABLE `risk_alerts`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `advisor_id` bigint(20) NOT NULL COMMENT '对应辅导员',
  `alert_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SELF_ASSESSMENT/COUNSELOR',
  `alert_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_read` int(11) NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_advisor_id`(`advisor_id`) USING BTREE,
  CONSTRAINT `risk_alerts_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `risk_alerts_ibfk_2` FOREIGN KEY (`advisor_id`) REFERENCES `advisors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '心理预警表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of risk_alerts
-- ----------------------------
INSERT INTO `risk_alerts` VALUES (3, 1, 1, 'SELF_ASSESSMENT', 'ANXIETY', 'HIGH', 1, '2026-05-25 15:33:51');
INSERT INTO `risk_alerts` VALUES (4, 1, 1, 'SELF_ASSESSMENT', 'DEPRESSION', 'MEDIUM', 1, '2026-05-22 17:08:47');
INSERT INTO `risk_alerts` VALUES (5, 3, 2, 'SELF_ASSESSMENT', 'DEPRESSION', 'MEDIUM', 1, '2026-05-22 17:52:52');
INSERT INTO `risk_alerts` VALUES (6, 2, 1, 'SELF_ASSESSMENT', 'DEPRESSION', 'MEDIUM', 1, '2026-05-22 18:42:48');
INSERT INTO `risk_alerts` VALUES (7, 2, 1, 'COUNSELOR', 'CONSULTATION', 'MEDIUM', 1, '2026-05-22 18:45:39');
INSERT INTO `risk_alerts` VALUES (8, 9, 1, 'SELF_ASSESSMENT', 'ANXIETY', 'MEDIUM', 1, '2026-05-25 17:52:23');
INSERT INTO `risk_alerts` VALUES (9, 7, 4, 'SELF_ASSESSMENT', 'ANXIETY', 'HIGH', 1, '2026-05-27 16:54:12');

-- ----------------------------
-- Table structure for self_assessments
-- ----------------------------
DROP TABLE IF EXISTS `self_assessments`;
CREATE TABLE `self_assessments`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `scale_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `total_score` int(11) NOT NULL,
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  CONSTRAINT `self_assessments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '心理自评' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of self_assessments
-- ----------------------------
INSERT INTO `self_assessments` VALUES (12, 8, 'DEPRESSION', 24, 'NORMAL', '[1,4,1,3,3,2,3,3,3,2]', '2026-05-22 17:06:02');
INSERT INTO `self_assessments` VALUES (13, 8, 'DEPRESSION', 30, 'NORMAL', '[3,3,2,2,3,3,2,2,3,3]', '2026-05-22 17:06:32');
INSERT INTO `self_assessments` VALUES (14, 8, 'ANXIETY', 33, 'LOW', '[3,3,2,3,2,2,2,2,2,3]', '2026-05-22 17:07:35');
INSERT INTO `self_assessments` VALUES (15, 1, 'DEPRESSION', 38, 'MEDIUM', '[3,2,3,3,2,3,2,3,2,3]', '2026-05-22 17:08:47');
INSERT INTO `self_assessments` VALUES (16, 1, 'ANXIETY', 34, 'LOW', '[2,3,2,3,2,3,2,3,2,3]', '2026-05-22 17:09:02');
INSERT INTO `self_assessments` VALUES (17, 1, 'ANXIETY', 36, 'MEDIUM', '[3,3,3,3,2,4,2,3,2,2]', '2026-05-22 17:09:17');
INSERT INTO `self_assessments` VALUES (18, 4, 'DEPRESSION', 35, 'LOW', '[3,2,4,1,2,4,2,3,4,3]', '2026-05-22 17:10:07');
INSERT INTO `self_assessments` VALUES (19, 3, 'DEPRESSION', 38, 'MEDIUM', '[3,2,4,2,2,3,2,3,2,3]', '2026-05-22 17:52:52');
INSERT INTO `self_assessments` VALUES (20, 2, 'DEPRESSION', 41, 'MEDIUM', '[3,2,3,4,2,4,2,4,2,3]', '2026-05-22 18:42:48');
INSERT INTO `self_assessments` VALUES (21, 1, 'ANXIETY', 41, 'HIGH', '[4,4,4,3,2,3,3,3,2,3]', '2026-05-25 15:33:51');
INSERT INTO `self_assessments` VALUES (22, 9, 'ANXIETY', 38, 'MEDIUM', '[4,3,3,3,2,3,2,3,2,3]', '2026-05-25 17:52:23');
INSERT INTO `self_assessments` VALUES (23, 7, 'ANXIETY', 40, 'HIGH', '[4,3,3,3,2,3,3,4,2,3]', '2026-05-27 16:54:12');

-- ----------------------------
-- Table structure for students
-- ----------------------------
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `advisor_id` bigint(20) NULL DEFAULT NULL COMMENT '所属辅导员ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id`) USING BTREE,
  UNIQUE INDEX `student_no`(`student_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_advisor_id`(`advisor_id`) USING BTREE,
  CONSTRAINT `students_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of students
-- ----------------------------
INSERT INTO `students` VALUES (1, 2, '20241001', '信息工程学院', '计算机241班', '2024', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (2, 3, '20241002', '信息工程学院', '计算机242班', '2024', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (3, 4, '20231001', '理学院', '数学231班', '2023', 2, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (4, 5, '20241003', '人文学院', '中文241班', '2024', 3, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (5, 6, '20231002', '经济管理学院', '会计231班', '2023', 4, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (6, 7, '20241004', '法学院', '法学241班', '2024', 3, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (7, 8, '20221001', '外国语学院', '英语221班', '2022', 4, '2026-05-20 22:05:24', '2026-05-25 17:51:30');
INSERT INTO `students` VALUES (8, 9, '20241005', '建筑工程学院', '土木241班', '2024', 2, '2026-05-20 22:05:24', '2026-05-25 17:51:30');
INSERT INTO `students` VALUES (9, 10, '20231003', '信息工程学院', '软件231班', '2023', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `students` VALUES (10, 11, '20241006', '艺术与设计学院', '设计241班', '2024', 3, '2026-05-20 22:05:24', '2026-05-25 17:51:30');

-- ----------------------------
-- Table structure for system_configs
-- ----------------------------
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_configs
-- ----------------------------
INSERT INTO `system_configs` VALUES (1, 'max_future_days', '14', '最大可预约天数');
INSERT INTO `system_configs` VALUES (2, 'cancel_deadline_hours', '24', '取消预约截止小时数');
INSERT INTO `system_configs` VALUES (3, 'phq9_threshold', '15', 'PHQ-9高风险阈值');
INSERT INTO `system_configs` VALUES (4, 'gad7_threshold', '10', 'GAD-7高风险阈值');

-- ----------------------------
-- Table structure for time_slots
-- ----------------------------
DROP TABLE IF EXISTS `time_slots`;
CREATE TABLE `time_slots`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `counselor_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `max_appointments` int(11) NULL DEFAULT 1,
  `booked_count` int(11) NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_counselor_id`(`counselor_id`) USING BTREE,
  CONSTRAINT `time_slots_ibfk_1` FOREIGN KEY (`counselor_id`) REFERENCES `counselors` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '可预约时段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of time_slots
-- ----------------------------
INSERT INTO `time_slots` VALUES (8, 1, '2026-05-22', '18:00:12', '19:00:21', 2, 0, 'ACTIVE', '2026-05-22 17:48:40', '2026-05-22 17:48:40');
INSERT INTO `time_slots` VALUES (9, 1, '2026-05-22', '09:00:57', '10:00:57', 2, 2, 'ACTIVE', '2026-05-22 17:49:23', '2026-05-22 17:49:23');
INSERT INTO `time_slots` VALUES (10, 2, '2026-05-22', '09:00:47', '10:00:47', 2, 1, 'INACTIVE', '2026-05-22 17:50:05', '2026-05-22 17:50:05');
INSERT INTO `time_slots` VALUES (11, 3, '2026-05-22', '19:00:19', '20:00:19', 2, 0, 'INACTIVE', '2026-05-22 17:50:44', '2026-05-22 17:50:44');
INSERT INTO `time_slots` VALUES (12, 3, '2026-05-22', '09:00:50', '10:00:50', 2, 0, 'INACTIVE', '2026-05-22 17:51:11', '2026-05-22 17:51:11');
INSERT INTO `time_slots` VALUES (13, 4, '2026-05-23', '09:00:25', '11:00:25', 3, 0, 'ACTIVE', '2026-05-22 17:51:54', '2026-05-22 17:51:54');
INSERT INTO `time_slots` VALUES (14, 2, '2026-05-25', '09:00:40', '10:00:40', 2, 1, 'INACTIVE', '2026-05-25 19:10:09', '2026-05-25 19:10:09');
INSERT INTO `time_slots` VALUES (15, 2, '2026-05-27', '10:30:10', '11:30:10', 2, 0, 'ACTIVE', '2026-05-25 19:10:35', '2026-05-25 19:10:35');
INSERT INTO `time_slots` VALUES (16, 2, '2026-05-27', '16:00:24', '17:00:24', 2, 0, 'ACTIVE', '2026-05-27 16:26:52', '2026-05-27 16:26:52');
INSERT INTO `time_slots` VALUES (17, 2, '2026-05-28', '09:00:03', '10:00:50', 2, 0, 'ACTIVE', '2026-05-27 16:38:51', '2026-05-27 16:38:51');
INSERT INTO `time_slots` VALUES (18, 2, '2026-05-28', '14:00:20', '15:00:20', 2, 0, 'ACTIVE', '2026-05-27 16:38:51', '2026-05-27 16:38:51');
INSERT INTO `time_slots` VALUES (19, 2, '2026-05-28', '16:00:38', '17:00:38', 2, 0, 'ACTIVE', '2026-05-27 16:38:51', '2026-05-27 16:38:51');
INSERT INTO `time_slots` VALUES (20, 3, '2026-05-27', '09:00:58', '10:00:58', 2, 1, 'BOOKED', '2026-05-27 17:10:40', '2026-05-27 17:10:40');
INSERT INTO `time_slots` VALUES (21, 3, '2026-05-27', '13:00:12', '14:00:12', 2, 0, 'ACTIVE', '2026-05-27 17:10:40', '2026-05-27 17:10:40');
INSERT INTO `time_slots` VALUES (22, 3, '2026-05-27', '15:00:26', '16:00:26', 2, 0, 'ACTIVE', '2026-05-27 17:10:40', '2026-05-27 17:10:40');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'STUDENT/COUNSELOR/ADVISOR/ADMIN',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', '彭冬生', '13870000001', 'pengds@ncu.edu.cn', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (2, 'student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '陈思远', '13870000101', 'chensy2024@ncu.edu.cn', '信息工程学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (3, 'student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '林晓雨', '13870000102', 'linxy2024@ncu.edu.cn', '信息工程学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (4, 'student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '王浩然', '13870000103', 'wanghr2023@ncu.edu.cn', '理学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (5, 'student4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '赵雅婷', '13870000104', 'zhaoyt2024@ncu.edu.cn', '人文学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (6, 'student5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '黄志强', '13870000105', 'huangzq2023@ncu.edu.cn', '经济管理学院', 0, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (7, 'student6', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '周佳怡', '13870000106', 'zhoujy2024@ncu.edu.cn', '法学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (8, 'student7', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '吴俊杰', '13870000107', 'wujj2022@ncu.edu.cn', '外国语学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (9, 'student8', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '刘悦然', '13870000108', 'liuyr2024@ncu.edu.cn', '建筑工程学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (10, 'student9', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '郑雨萱', '13870000109', 'zhengyx2023@ncu.edu.cn', '信息工程学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (11, 'student10', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', '许文韬', '13870000110', 'xuwt2024@ncu.edu.cn', '艺术与设计学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (12, 'counselor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'COUNSELOR', '陈慧敏', '13870000201', 'chenhm@ncu.edu.cn', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (13, 'counselor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'COUNSELOR', '刘建国', '13870006980', 'liujg@ncu.edu.cn', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (14, 'counselor3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'COUNSELOR', '杨文博', '13870000203', 'yangwb@ncu.edu.cn', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (15, 'counselor4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'COUNSELOR', '沈秋岚', '13870000204', 'shenql@ncu.edu.cn', NULL, 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (16, 'advisor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADVISOR', '赵明辉', '13870000301', 'zhaomh@ncu.edu.cn', '信息工程学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (17, 'advisor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADVISOR', '孙丽华', '13870000302', 'sunlh@ncu.edu.cn', '理学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (18, 'advisor3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADVISOR', '张雅文', '13870000303', 'zhangyw@ncu.edu.cn', '人文学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (19, 'advisor4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADVISOR', '马德昌', '13870000304', 'madc@ncu.edu.cn', '经济管理学院', 1, '2026-05-20 22:05:24', '2026-05-20 22:05:24');
INSERT INTO `users` VALUES (21, 'counselor7', '$2a$10$KLtbMDtvu1CsGHXykkdamOvySmih/FvP/m.LvH2Ig428w9AwDNfyq', 'COUNSELOR', '王维', '19075859963', '1907765922@qq.com', '', 1, '2026-05-20 23:34:01', '2026-05-20 23:34:01');

SET FOREIGN_KEY_CHECKS = 1;
