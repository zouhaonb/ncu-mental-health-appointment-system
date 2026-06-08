<template>
  <div class="register-page">
    <!-- ===== Left: Brand Panel ===== -->
    <div class="brand-panel">
      <div class="brand-bg">
        <div class="bg-blob blob-1"></div>
        <div class="bg-blob blob-2"></div>
        <div class="bg-blob blob-3"></div>
        <div class="bg-noise"></div>
      </div>

      <div class="particles">
        <span class="particle" v-for="i in 12" :key="i" :style="{ '--i': i }"></span>
      </div>

      <!-- Camphor tree -->
      <div class="tree-scene">
        <svg class="camphor-tree" viewBox="0 0 200 240" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M97 240 L97 140 Q95 130 90 120 L88 115 Q95 118 100 120 Q105 118 112 115 L110 120 Q105 130 103 140 L103 240 Z" fill="rgba(255,255,255,0.12)" />
          <path d="M97 160 Q80 155 65 140 Q60 135 58 128" stroke="rgba(255,255,255,0.15)" stroke-width="3" fill="none" stroke-linecap="round" />
          <path d="M103 150 Q120 142 135 130 Q138 126 140 120" stroke="rgba(255,255,255,0.15)" stroke-width="3" fill="none" stroke-linecap="round" />
          <ellipse cx="100" cy="85" rx="65" ry="50" fill="rgba(255,255,255,0.06)" />
          <ellipse cx="75" cy="75" rx="42" ry="35" fill="rgba(255,255,255,0.05)" />
          <ellipse cx="125" cy="70" rx="38" ry="32" fill="rgba(255,255,255,0.05)" />
          <ellipse cx="90" cy="55" rx="35" ry="28" fill="rgba(255,255,255,0.04)" />
          <ellipse cx="115" cy="50" rx="30" ry="25" fill="rgba(255,255,255,0.04)" />
          <ellipse cx="100" cy="40" rx="25" ry="22" fill="rgba(255,255,255,0.03)" />
          <circle cx="60" cy="80" r="18" fill="rgba(255,255,255,0.04)" />
          <circle cx="140" cy="75" r="16" fill="rgba(255,255,255,0.04)" />
          <circle cx="85" cy="45" r="14" fill="rgba(255,255,255,0.03)" />
          <circle cx="118" cy="42" r="12" fill="rgba(255,255,255,0.03)" />
        </svg>
      </div>

      <div class="brand-content">
        <h1 class="brand-title">
          <span class="title-cn">南昌大学</span>
          <span class="title-sub">心理健康服务预约系统</span>
        </h1>
        <div class="brand-divider"></div>
        <p class="brand-desc">创建账号，开启你的心理健康之旅</p>
        <div class="brand-steps">
          <div class="step-item" style="--d:0.6s">
            <span class="step-num">1</span>
            <div class="step-text">
              <strong>注册账号</strong>
              <span>填写基本信息</span>
            </div>
          </div>
          <div class="step-connector"></div>
          <div class="step-item" style="--d:0.75s">
            <span class="step-num">2</span>
            <div class="step-text">
              <strong>选择咨询师</strong>
              <span>浏览专业团队</span>
            </div>
          </div>
          <div class="step-connector"></div>
          <div class="step-item" style="--d:0.9s">
            <span class="step-num">3</span>
            <div class="step-text">
              <strong>预约咨询</strong>
              <span>开始心灵之旅</span>
            </div>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        <p>&copy; 2026 南昌大学心理健康教育中心</p>
      </div>
    </div>

    <!-- ===== Right: Form Panel ===== -->
    <div class="form-panel">
      <svg class="corner-deco top-right" viewBox="0 0 120 120" fill="none">
        <path d="M120 0 Q80 10 60 40 Q40 70 0 80 Q30 60 50 45 Q70 30 120 0Z" fill="#1E4D8C" opacity="0.04"/>
        <path d="M120 0 Q90 5 70 25 Q50 45 20 60 Q45 40 65 30 Q85 20 120 0Z" fill="#1E4D8C" opacity="0.03"/>
      </svg>
      <svg class="corner-deco bottom-left" viewBox="0 0 120 120" fill="none">
        <path d="M0 120 Q40 110 60 80 Q80 50 120 40 Q90 60 70 75 Q50 90 0 120Z" fill="#1E4D8C" opacity="0.04"/>
        <path d="M0 120 Q30 115 50 95 Q70 75 100 60 Q75 80 55 90 Q35 100 0 120Z" fill="#1E4D8C" opacity="0.03"/>
      </svg>

      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">创建账号</h2>
          <p class="form-subtitle">填写以下信息完成注册</p>
        </div>

        <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="80px" class="register-form">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（字母数字下划线）"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item label="姓名" prop="name">
            <el-input
              v-model="registerForm.name"
              placeholder="请输入真实姓名"
              prefix-icon="UserFilled"
              size="large"
            />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-select v-model="registerForm.role" placeholder="请选择身份" style="width: 100%" size="large">
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
              size="large"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱地址"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>

          <el-form-item label="学院" prop="college">
            <el-input
              v-model="registerForm.college"
              placeholder="请输入所在学院"
              prefix-icon="School"
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              <span v-if="!loading">注 册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login" class="link-text">立即登录</router-link>
        </div>
      </div>
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
/* ========================================
   DESIGN TOKENS (same as Login.vue)
   ======================================== */

/* ========================================
   PAGE LAYOUT
   ======================================== */
.register-page {
  width: 100%;
  height: 100vh;
  display: flex;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', sans-serif;
}

/* ========================================
   LEFT: BRAND PANEL
   ======================================== */
.brand-panel {
  position: relative;
  width: 44%;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(155deg, #0B1D3A 0%, #132E5A 25%, #1E4D8C 55%, #2B6CB0 80%, #3A7FBD 100%);
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-blob {
  position: absolute;
  border-radius: 40% 60% 55% 45% / 50% 40% 60% 50%;
  background: rgba(255, 255, 255, 0.04);
  animation: morphBlob 25s ease-in-out infinite;
}

.blob-1 { width: 450px; height: 450px; top: -12%; left: -8%; }
.blob-2 { width: 300px; height: 300px; bottom: -8%; right: -4%; animation-delay: -8s; background: rgba(74, 155, 217, 0.06); }
.blob-3 { width: 180px; height: 180px; top: 45%; right: 12%; animation-delay: -15s; background: rgba(255,255,255,0.03); }

@keyframes morphBlob {
  0%, 100% { border-radius: 40% 60% 55% 45% / 50% 40% 60% 50%; transform: translate(0, 0) scale(1); }
  33% { border-radius: 55% 45% 40% 60% / 45% 55% 45% 55%; transform: translate(15px, -20px) scale(1.05); }
  66% { border-radius: 45% 55% 60% 40% / 60% 45% 55% 45%; transform: translate(-10px, 15px) scale(0.97); }
}

.bg-noise {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.5'/%3E%3C/svg%3E");
  background-size: 128px;
  mix-blend-mode: overlay;
  pointer-events: none;
}

.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 3px;
  height: 3px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  left: calc(var(--i) * 8% - 4%);
  bottom: -10px;
  animation: particleRise calc(12s + var(--i) * 2s) linear infinite;
  animation-delay: calc(var(--i) * -1.5s);
}

@keyframes particleRise {
  0% { transform: translateY(0) translateX(0); opacity: 0; }
  10% { opacity: 0.4; }
  90% { opacity: 0.1; }
  100% { transform: translateY(-100vh) translateX(30px); opacity: 0; }
}

.tree-scene {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 260px;
  height: 300px;
  opacity: 0.5;
  pointer-events: none;
  animation: treeBreath 8s ease-in-out infinite;
}

.camphor-tree { width: 100%; height: 100%; filter: drop-shadow(0 0 40px rgba(74, 155, 217, 0.15)); }

@keyframes treeBreath {
  0%, 100% { transform: translateX(-50%) scale(1); }
  50% { transform: translateX(-50%) scale(1.02); }
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 52px;
  animation: fadeInUp 0.8s ease-out both;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

.brand-title {
  margin: 0 0 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-cn {
  display: block;
  font-size: 34px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 6px;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
}

.title-sub {
  display: block;
  font-size: 16px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4px;
}

.brand-divider {
  width: 48px;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent);
  margin: 0 auto 16px;
}

.brand-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.8;
  margin: 0 0 36px;
  letter-spacing: 1px;
}

/* Steps */
.brand-steps {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  animation: fadeInUp 0.6s ease-out both;
  animation-delay: var(--d, 0s);
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(74, 155, 217, 0.2);
  border: 1.5px solid rgba(74, 155, 217, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.step-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.step-text strong {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.step-text span {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.step-connector {
  width: 1px;
  height: 16px;
  background: linear-gradient(180deg, rgba(74, 155, 217, 0.3), rgba(74, 155, 217, 0.1));
  margin-left: 15px;
}

.brand-footer {
  position: absolute;
  bottom: 28px;
  color: rgba(255, 255, 255, 0.3);
  font-size: 11px;
  letter-spacing: 0.5px;
  z-index: 2;
}

.brand-footer p { margin: 0; }

/* ========================================
   RIGHT: FORM PANEL
   ======================================== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FAF8F5;
  position: relative;
  overflow: hidden;
}

.corner-deco {
  position: absolute;
  width: 140px;
  height: 140px;
  pointer-events: none;
  opacity: 0.8;
}

.top-right { top: 0; right: 0; }
.bottom-left { bottom: 0; left: 0; }

.form-wrapper {
  width: 460px;
  max-height: 90vh;
  padding: 36px 44px;
  background: #fff;
  border-radius: 20px;
  box-shadow:
    0 0 0 1px rgba(30, 77, 140, 0.05),
    0 2px 4px rgba(30, 77, 140, 0.03),
    0 12px 40px rgba(30, 77, 140, 0.08);
  overflow-y: auto;
  position: relative;
  z-index: 1;
  animation: cardEnter 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
  animation-delay: 0.2s;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.form-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #1E4D8C, #4A9BD9);
  border-radius: 0 0 3px 3px;
}

.form-header {
  margin-bottom: 28px;
  text-align: center;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0 0 8px;
  letter-spacing: 0.5px;
}

.form-subtitle {
  font-size: 13px;
  color: #9CA3AF;
  margin: 0;
}

.register-form .el-form-item {
  margin-bottom: 18px;
}

/* ========================================
   REGISTER BUTTON
   ======================================== */
.register-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 3px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #1E4D8C 0%, #2B6CB0 50%, #1E4D8C 100%);
  background-size: 200% 200%;
  color: #fff;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.12), transparent);
  transition: left 0.5s ease;
}

.register-btn:hover {
  background-position: 100% 0;
  box-shadow: 0 8px 25px rgba(30, 77, 140, 0.35);
  transform: translateY(-1px);
}

.register-btn:hover::before { left: 100%; }

.register-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(30, 77, 140, 0.25);
}

/* ========================================
   LOGIN LINK
   ======================================== */
.login-link {
  text-align: center;
  margin-top: 8px;
  font-size: 13px;
  color: #9CA3AF;
}

.link-text {
  color: #2B6CB0;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.2s;
}

.link-text:hover { color: #1E4D8C; }

/* ========================================
   ELEMENT PLUS OVERRIDES
   ======================================== */
:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 6px 14px;
  box-shadow: 0 0 0 1px #E5E7EB inset;
  transition: all 0.25s ease;
  background: #FAFAFA;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #D1D5DB inset;
  background: #fff;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #2B6CB0 inset, 0 0 0 4px rgba(43, 108, 176, 0.08) inset;
  background: #fff;
}

:deep(.el-input__inner) {
  font-size: 14px;
  height: 24px;
  color: #1A1A2E;
}

:deep(.el-input__inner::placeholder) { color: #C0C4CC; }
:deep(.el-input__prefix .el-icon) { color: #9CA3AF; }

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4B5563;
  font-size: 13px;
}

:deep(.el-select .el-input__wrapper) { border-radius: 12px; }
:deep(.el-form-item__error) { font-size: 12px; padding-top: 4px; }

/* ========================================
   SCROLLBAR
   ======================================== */
.form-wrapper::-webkit-scrollbar { width: 4px; }
.form-wrapper::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 2px; }
.form-wrapper::-webkit-scrollbar-track { background: transparent; }

/* ========================================
   RESPONSIVE
   ======================================== */
@media (max-width: 900px) {
  .brand-panel { display: none; }

  .form-panel {
    background: linear-gradient(155deg, #0B1D3A 0%, #1E4D8C 50%, #2B6CB0 100%);
  }

  .corner-deco { display: none; }

  .form-wrapper {
    width: 90%;
    max-width: 460px;
    margin: 0 20px;
  }
}
</style>