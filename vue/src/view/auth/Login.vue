<template>
  <div class="login-container">
    <div class="login-header">
      <div class="logo-icon">
        <el-icon :size="36"><User /></el-icon>
      </div>
      <h1 class="system-title">南昌大学心理健康服务预约系统</h1>
      <p class="system-subtitle">关注心理健康，呵护心灵成长</p>
    </div>

    <div class="login-box">
      <h2 class="login-title">账号登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="default"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="default"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="default"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        <span>还没有账号？</span>
        <router-link to="/register">
          <el-link type="primary" :underline="false">立即注册</el-link>
        </router-link>
      </div>

      <el-divider style="margin: 16px 0" />

      <div class="test-accounts">
        <p class="test-title">测试账号（密码均为 123456）</p>
        <div class="test-item">
          <el-tag size="small" type="danger">管理员</el-tag>
          <code>admin</code>
          <span class="test-separator">|</span>
          <el-tag size="small" type="success">咨询师</el-tag>
          <code>counselor1~4</code>
        </div>
        <div class="test-item">
          <el-tag size="small" type="primary">学生</el-tag>
          <code>student1~10</code>
          <span class="test-separator">|</span>
          <el-tag size="small" type="warning">辅导员</el-tag>
          <code>advisor1~4</code>
        </div>
      </div>
    </div>

    <div class="footer">
      <p>© 2026 南昌大学心理健康教育中心</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authAPI } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authAPI.login(loginForm)
        userStore.setUserInfo(res.data)
        ElMessage.success('登录成功')

        const roleRoutes = {
          'STUDENT': '/student/home',
          'COUNSELOR': '/counselor/home',
          'ADVISOR': '/advisor/home',
          'ADMIN': '/admin/users'
        }

        router.push(roleRoutes[res.data.role] || '/login')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
  color: white;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  backdrop-filter: blur(10px);
}

.logo-icon .el-icon {
  color: white;
}

.system-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 6px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.system-subtitle {
  font-size: 13px;
  margin: 0;
  opacity: 0.9;
}

.login-box {
  width: 420px;
  padding: 32px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

.login-title {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.register-link {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

.register-link .el-link {
  margin-left: 4px;
}

.test-accounts {
  margin-top: 16px;
}

.test-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
  text-align: center;
}

.test-item {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 12px;
}

.test-item code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  color: #606266;
  font-family: 'Courier New', monospace;
}

.test-separator {
  color: #dcdfe6;
  margin: 0 4px;
}

.footer {
  position: absolute;
  bottom: 24px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}

.footer p {
  margin: 0;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-button--primary) {
  border-radius: 6px;
}
</style>