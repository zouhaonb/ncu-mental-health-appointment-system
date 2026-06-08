<template>
  <div class="register-container">
    <div class="register-header">
      <div class="logo-icon">
        <el-icon :size="36"><User /></el-icon>
      </div>
      <h1 class="system-title">南昌大学心理健康服务预约系统</h1>
      <p class="system-subtitle">关注心理健康，呵护心灵成长</p>
    </div>

    <div class="register-box">
      <h2 class="register-title">账号注册</h2>
      <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（字母数字下划线）"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="请输入真实姓名"
            prefix-icon="UserFilled"
          />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择身份" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="咨询师" value="COUNSELOR" />
            <el-option label="辅导员" value="ADVISOR" />
          </el-select>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="学院" prop="college">
          <el-input
            v-model="registerForm.college"
            placeholder="请输入所在学院"
            prefix-icon="School"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        <span>已有账号？</span>
        <router-link to="/login">
          <el-link type="primary" :underline="false">立即登录</el-link>
        </router-link>
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
import { authAPI } from '@/api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: 'STUDENT',
  phone: '',
  email: '',
  college: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...userData } = registerForm
        await authAPI.register(userData)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
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
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.register-header {
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

.register-box {
  width: 480px;
  padding: 32px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  max-height: 80vh;
  overflow-y: auto;
}

.register-title {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.login-link {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

.login-link .el-link {
  margin-left: 4px;
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

.register-box::-webkit-scrollbar {
  width: 6px;
}

.register-box::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.register-box::-webkit-scrollbar-track {
  background: transparent;
}
</style>