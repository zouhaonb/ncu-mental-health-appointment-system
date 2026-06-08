package com.ncu.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.*;
import com.ncu.mentalhealth.mapper.*;
import com.ncu.mentalhealth.service.AdminService;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final CounselorMapper counselorMapper;
    private final AdvisorMapper advisorMapper;
    private final ArticleMapper articleMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final AppointmentMapper appointmentMapper;
    private final SelfAssessmentMapper selfAssessmentMapper;
    private final RiskAlertMapper riskAlertMapper;
    private final AnonymousMessageMapper anonymousMessageMapper;
    private final ConsultationRecordMapper consultationRecordMapper;
    private final KeyConcernStudentMapper keyConcernStudentMapper;
    private final FollowUpTaskMapper followUpTaskMapper;
    private final ReferralSuggestionMapper referralSuggestionMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<List<User>> getUserList(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        List<User> userList = userMapper.selectList(wrapper);
        return Result.success(userList);
    }

    @Override
    public Result<List<String>> getCollegeList() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getCollege)
               .isNotNull(User::getCollege)
               .ne(User::getCollege, "")
               .groupBy(User::getCollege);
        List<User> users = userMapper.selectList(wrapper);
        List<String> colleges = users.stream()
                                     .map(User::getCollege)
                                     .distinct()
                                     .sorted()
                                     .toList();
        return Result.success(colleges);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createUser(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        int rows = userMapper.insert(user);
        return rows > 0 ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        int rows = userMapper.updateById(user);
        return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long studentId = null;
        Long counselorId = null;
        Long advisorId = null;

        if ("COUNSELOR".equals(user.getRole())) {
            LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Counselor::getUserId, userId);
            Counselor counselor = counselorMapper.selectOne(wrapper);
            if (counselor != null) {
                counselorId = counselor.getId();
            }
            counselorMapper.delete(wrapper);
        } else if ("STUDENT".equals(user.getRole())) {
            LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Student::getUserId, userId);
            Student student = studentMapper.selectOne(wrapper);
            if (student != null) {
                studentId = student.getId();
            }
            studentMapper.delete(wrapper);
        } else if ("ADVISOR".equals(user.getRole())) {
            LambdaQueryWrapper<Advisor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Advisor::getUserId, userId);
            Advisor advisor = advisorMapper.selectOne(wrapper);
            if (advisor != null) {
                advisorId = advisor.getId();
            }
            advisorMapper.delete(wrapper);
        }

        if (studentId != null) {
            LambdaQueryWrapper<Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
            appointmentWrapper.eq(Appointment::getStudentId, studentId);
            appointmentMapper.delete(appointmentWrapper);

            LambdaQueryWrapper<SelfAssessment> assessmentWrapper = new LambdaQueryWrapper<>();
            assessmentWrapper.eq(SelfAssessment::getStudentId, studentId);
            selfAssessmentMapper.delete(assessmentWrapper);

            LambdaQueryWrapper<RiskAlert> alertWrapper = new LambdaQueryWrapper<>();
            alertWrapper.eq(RiskAlert::getStudentId, studentId);
            riskAlertMapper.delete(alertWrapper);

            LambdaQueryWrapper<AnonymousMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(AnonymousMessage::getStudentId, studentId);
            anonymousMessageMapper.delete(messageWrapper);

            LambdaQueryWrapper<KeyConcernStudent> concernWrapper = new LambdaQueryWrapper<>();
            concernWrapper.eq(KeyConcernStudent::getStudentId, studentId);
            keyConcernStudentMapper.delete(concernWrapper);

            LambdaQueryWrapper<FollowUpTask> followUpWrapper = new LambdaQueryWrapper<>();
            followUpWrapper.eq(FollowUpTask::getStudentId, studentId);
            followUpTaskMapper.delete(followUpWrapper);

            LambdaQueryWrapper<ReferralSuggestion> referralWrapper = new LambdaQueryWrapper<>();
            referralWrapper.eq(ReferralSuggestion::getStudentId, studentId);
            referralSuggestionMapper.delete(referralWrapper);
        }

        if (counselorId != null) {
            LambdaQueryWrapper<Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
            appointmentWrapper.eq(Appointment::getCounselorId, counselorId);
            appointmentMapper.delete(appointmentWrapper);

            LambdaQueryWrapper<ConsultationRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ConsultationRecord::getCounselorId, counselorId);
            consultationRecordMapper.delete(recordWrapper);

            LambdaQueryWrapper<AnonymousMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.eq(AnonymousMessage::getCounselorId, counselorId);
            anonymousMessageMapper.delete(messageWrapper);

            LambdaQueryWrapper<ReferralSuggestion> referralWrapper = new LambdaQueryWrapper<>();
            referralWrapper.eq(ReferralSuggestion::getAssignedCounselorId, counselorId);
            referralSuggestionMapper.delete(referralWrapper);

            LambdaQueryWrapper<TimeSlot> timeSlotWrapper = new LambdaQueryWrapper<>();
            timeSlotWrapper.eq(TimeSlot::getCounselorId, counselorId);
            timeSlotMapper.delete(timeSlotWrapper);
        }

        if (advisorId != null) {
            LambdaQueryWrapper<RiskAlert> alertWrapper = new LambdaQueryWrapper<>();
            alertWrapper.eq(RiskAlert::getAdvisorId, advisorId);
            riskAlertMapper.delete(alertWrapper);

            LambdaQueryWrapper<KeyConcernStudent> concernWrapper = new LambdaQueryWrapper<>();
            concernWrapper.eq(KeyConcernStudent::getAdvisorId, advisorId);
            keyConcernStudentMapper.delete(concernWrapper);

            LambdaQueryWrapper<FollowUpTask> followUpWrapper = new LambdaQueryWrapper<>();
            followUpWrapper.eq(FollowUpTask::getAdvisorId, advisorId);
            followUpTaskMapper.delete(followUpWrapper);

            LambdaQueryWrapper<ReferralSuggestion> referralWrapper = new LambdaQueryWrapper<>();
            referralWrapper.eq(ReferralSuggestion::getAdvisorId, advisorId);
            referralSuggestionMapper.delete(referralWrapper);
        }

        int rows = userMapper.deleteById(userId);
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> toggleUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        int rows = userMapper.updateById(user);
        return rows > 0 ? Result.success("状态已更新") : Result.error("操作失败");
    }

    @Override
    public Result<List<Map<String, Object>>> getCounselorList(String status, String keyword) {
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Counselor::getIsActive, Integer.parseInt(status));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Counselor::getTitle, keyword)
                    .or().like(Counselor::getSpecialization, keyword)
                    .or().like(Counselor::getDescription, keyword));
        }
        
        wrapper.orderByDesc(Counselor::getCreatedAt);
        List<Counselor> counselors = counselorMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = counselors.stream().map(counselor -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", counselor.getId());
            map.put("userId", counselor.getUserId());
            map.put("title", counselor.getTitle());
            map.put("specialization", counselor.getSpecialization());
            map.put("description", counselor.getDescription());
            map.put("avatar", counselor.getAvatar());
            map.put("isActive", counselor.getIsActive());
            map.put("rejectReason", counselor.getRejectReason());
            map.put("createdAt", counselor.getCreatedAt());
            map.put("updatedAt", counselor.getUpdatedAt());
            
            User user = userMapper.selectById(counselor.getUserId());
            if (user != null) {
                map.put("name", user.getName());
                map.put("phone", user.getPhone());
                map.put("email", user.getEmail());
                map.put("college", user.getCollege());
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> approveCounselor(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }
        
        counselor.setIsActive(1);
        counselor.setRejectReason(null);
        int rows = counselorMapper.updateById(counselor);
        return rows > 0 ? Result.success("审核通过") : Result.error("操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> rejectCounselor(Long counselorId, String reason) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }
        
        counselor.setIsActive(2);
        counselor.setRejectReason(reason);
        int rows = counselorMapper.updateById(counselor);
        return rows > 0 ? Result.success("已拒绝") : Result.error("操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateCounselor(Map<String, Object> counselorData) {
        Long counselorId = Long.valueOf(counselorData.get("id").toString());
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }
        
        if (counselorData.containsKey("title")) {
            counselor.setTitle((String) counselorData.get("title"));
        }
        if (counselorData.containsKey("specialization")) {
            counselor.setSpecialization((String) counselorData.get("specialization"));
        }
        if (counselorData.containsKey("description")) {
            counselor.setDescription((String) counselorData.get("description"));
        }
        
        int rows = counselorMapper.updateById(counselor);
        return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> toggleCounselorStatus(Long counselorId) {
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }
        
        counselor.setIsActive(counselor.getIsActive() == -1 ? 1 : -1);
        int rows = counselorMapper.updateById(counselor);
        return rows > 0 ? Result.success("状态已更新") : Result.error("操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createCounselor(Map<String, Object> counselorData) {
        String username = (String) counselorData.get("username");
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode((String) counselorData.get("password")));
        user.setName((String) counselorData.get("name"));
        user.setPhone((String) counselorData.get("phone"));
        user.setEmail((String) counselorData.get("email"));
        user.setCollege((String) counselorData.get("college"));
        user.setRole("COUNSELOR");
        user.setStatus(1);
        
        int userRows = userMapper.insert(user);
        if (userRows <= 0) {
            return Result.error("创建失败");
        }
        
        Counselor counselor = new Counselor();
        counselor.setUserId(user.getId());
        counselor.setTitle((String) counselorData.get("title"));
        counselor.setSpecialization((String) counselorData.get("specialization"));
        counselor.setDescription((String) counselorData.get("description"));
        counselor.setIsActive(1);
        
        int counselorRows = counselorMapper.insert(counselor);
        return counselorRows > 0 ? Result.success("创建成功") : Result.error("创建失败");
    }

    @Override
    public Result<Map<String, Long>> getCounselorStatistics() {
        Long total = counselorMapper.selectCount(null);
        
        LambdaQueryWrapper<Counselor> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Counselor::getIsActive, 0);
        Long pending = counselorMapper.selectCount(pendingWrapper);
        
        LambdaQueryWrapper<Counselor> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Counselor::getIsActive, 1);
        Long approved = counselorMapper.selectCount(approvedWrapper);
        
        LambdaQueryWrapper<Counselor> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Counselor::getIsActive, 2);
        Long rejected = counselorMapper.selectCount(rejectedWrapper);
        
        LambdaQueryWrapper<Counselor> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(Counselor::getIsActive, -1);
        Long disabled = counselorMapper.selectCount(disabledWrapper);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("disabled", disabled);
        
        return Result.success(stats);
    }

    @Override
    public Result<List<Article>> getArticleList() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getCreatedAt);
        return Result.success(articleMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createArticle(Article article) {
        int rows = articleMapper.insert(article);
        return rows > 0 ? Result.success("发布成功") : Result.error("发布失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateArticle(Article article) {
        int rows = articleMapper.updateById(article);
        return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteArticle(Long articleId) {
        int rows = articleMapper.deleteById(articleId);
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Override
    public Result<List<SystemConfig>> getConfigList() {
        return Result.success(systemConfigMapper.selectList(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateConfig(SystemConfig config) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, config.getConfigKey());
        SystemConfig existing = systemConfigMapper.selectOne(wrapper);
        
        int rows;
        if (existing != null) {
            existing.setConfigValue(config.getConfigValue());
            existing.setDescription(config.getDescription());
            rows = systemConfigMapper.updateById(existing);
        } else {
            rows = systemConfigMapper.insert(config);
        }
        
        return rows > 0 ? Result.success("配置已更新") : Result.error("更新失败");
    }

    @Override
    public Result<Map<String, Object>> getSystemStatistics() {
        Long totalUsers = userMapper.selectCount(null);
        Long totalStudents = studentMapper.selectCount(null);
        Long totalCounselors = counselorMapper.selectCount(null);
        Long totalAdvisors = advisorMapper.selectCount(null);
        
        Long totalAppointments = appointmentMapper.selectCount(null);
        Long totalAssessments = selfAssessmentMapper.selectCount(null);
        Long totalAlerts = riskAlertMapper.selectCount(null);

        LambdaQueryWrapper<Appointment> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Appointment::getCreatedAt, java.time.LocalDate.now().atStartOfDay());
        Long todayAppointments = appointmentMapper.selectCount(todayWrapper);

        LambdaQueryWrapper<Appointment> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Appointment::getStatus, "PENDING");
        Long pendingAppointments = appointmentMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<Appointment> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Appointment::getStatus, "COMPLETED");
        Long completedAppointments = appointmentMapper.selectCount(completedWrapper);

        List<Map<String, Object>> collegeAppointments = new java.util.ArrayList<>();
        
        // 1. 先获取所有学院（从students表中获取所有不重复的学院）
        LambdaQueryWrapper<Student> studentCollegeWrapper = new LambdaQueryWrapper<>();
        studentCollegeWrapper.select(Student::getCollege)
                     .isNotNull(Student::getCollege)
                     .ne(Student::getCollege, "")
                     .groupBy(Student::getCollege);
        List<Student> collegeStudents = studentMapper.selectList(studentCollegeWrapper);
        
        // 2. 获取所有预约记录
        List<Appointment> allAppointments = appointmentMapper.selectList(null);
        
        // 3. 按学院统计预约人数（去重）- 通过student表关联获取学院信息
        java.util.Map<String, java.util.Set<Long>> collegeStudentMap = new java.util.HashMap<>();
        for (Appointment appointment : allAppointments) {
            Student student = studentMapper.selectById(appointment.getStudentId());
            if (student != null && student.getCollege() != null && !student.getCollege().isEmpty()) {
                String college = student.getCollege();
                collegeStudentMap.computeIfAbsent(college, k -> new java.util.HashSet<>())
                                .add(appointment.getStudentId());
            }
        }
        
        // 4. 遍历所有学院，确保每个学院都出现在结果中（即使预约人数为0）
        for (Student student : collegeStudents) {
            String college = student.getCollege();
            long count = collegeStudentMap.getOrDefault(college, new java.util.HashSet<>()).size();
            
            Map<String, Object> collegeData = new java.util.HashMap<>();
            collegeData.put("college", college);
            collegeData.put("count", count);
            collegeAppointments.add(collegeData);
        }
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalCounselors", totalCounselors);
        stats.put("totalAdvisors", totalAdvisors);
        stats.put("totalAppointments", totalAppointments);
        stats.put("todayAppointments", todayAppointments);
        stats.put("pendingAppointments", pendingAppointments);
        stats.put("completedAppointments", completedAppointments);
        stats.put("totalAssessments", totalAssessments);
        stats.put("totalAlerts", totalAlerts);
        stats.put("collegeAppointments", collegeAppointments);
        
        return Result.success(stats);
    }

    @Override
    public Result<Map<String, Object>> getHistoryStatistics(String startDate, String endDate) {
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.parse(startDate + "T00:00:00");
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.parse(endDate + "T23:59:59");
        
        // 查询时间范围内的预约记录
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Appointment::getCreatedAt, startDateTime)
               .le(Appointment::getCreatedAt, endDateTime);
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);
        
        // 按日期统计预约数量
        java.util.Map<String, Long> dailyAppointments = new java.util.HashMap<>();
        // 按状态统计
        java.util.Map<String, Long> statusStats = new java.util.HashMap<>();
        // 按学院统计
        java.util.Map<String, java.util.Set<Long>> collegeStudentMap = new java.util.HashMap<>();
        
        for (Appointment appointment : appointments) {
            // 按日期统计
            String dateStr = appointment.getCreatedAt().toLocalDate().toString();
            dailyAppointments.merge(dateStr, 1L, Long::sum);
            
            // 按状态统计
            String status = appointment.getStatus();
            statusStats.merge(status, 1L, Long::sum);
            
            // 按学院统计（去重）
            Student student = studentMapper.selectById(appointment.getStudentId());
            if (student != null && student.getCollege() != null && !student.getCollege().isEmpty()) {
                String college = student.getCollege();
                collegeStudentMap.computeIfAbsent(college, k -> new java.util.HashSet<>())
                                .add(appointment.getStudentId());
            }
        }
        
        // 获取所有学院列表（确保即使0预约的学院也显示）
        LambdaQueryWrapper<Student> studentCollegeWrapper = new LambdaQueryWrapper<>();
        studentCollegeWrapper.select(Student::getCollege)
                     .isNotNull(Student::getCollege)
                     .ne(Student::getCollege, "")
                     .groupBy(Student::getCollege);
        List<Student> collegeStudents = studentMapper.selectList(studentCollegeWrapper);
        
        // 构建学院统计数据
        List<Map<String, Object>> collegeAppointments = new java.util.ArrayList<>();
        for (Student student : collegeStudents) {
            String college = student.getCollege();
            long count = collegeStudentMap.getOrDefault(college, new java.util.HashSet<>()).size();
            
            Map<String, Object> collegeData = new java.util.HashMap<>();
            collegeData.put("college", college);
            collegeData.put("count", count);
            collegeAppointments.add(collegeData);
        }
        
        // 转换为前端可用的格式
        List<Map<String, Object>> dailyData = new java.util.ArrayList<>();
        dailyAppointments.entrySet().stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .forEach(entry -> {
                Map<String, Object> dayData = new java.util.HashMap<>();
                dayData.put("date", entry.getKey());
                dayData.put("count", entry.getValue());
                dailyData.add(dayData);
            });
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalAppointments", (long) appointments.size());
        result.put("dailyAppointments", dailyData);
        result.put("statusStatistics", statusStats);
        result.put("collegeAppointments", collegeAppointments);
        
        // 计算完成率
        Long completed = statusStats.getOrDefault("COMPLETED", 0L);
        Long total = (long) appointments.size();
        double completionRate = total > 0 ? (completed.doubleValue() / total * 100) : 0;
        result.put("completionRate", String.format("%.2f", completionRate));
        
        return Result.success(result);
    }
}
