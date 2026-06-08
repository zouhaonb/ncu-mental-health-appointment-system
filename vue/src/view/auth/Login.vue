<template>
  <div class="login-page">
    <!-- ===== Left: Brand Panel ===== -->
    <div class="brand-panel">
      <!-- Background layers -->
      <div class="brand-bg">
        <div class="bg-blob blob-1"></div>
        <div class="bg-blob blob-2"></div>
        <div class="bg-blob blob-3"></div>
        <div class="bg-noise"></div>
      </div>

      <!-- Floating particles -->
      <div class="particles">
        <span class="particle" v-for="i in 12" :key="i" :style="{ '--i': i }"></span>
      </div>

      <!-- Camphor tree illustration -->
      <div class="tree-scene">
        <svg class="camphor-tree" viewBox="0 0 200 240" fill="none" xmlns="http://www.w3.org/2000/svg">
          <!-- Trunk -->
          <path d="M97 240 L97 140 Q95 130 90 120 L88 115 Q95 118 100 120 Q105 118 112 115 L110 120 Q105 130 103 140 L103 240 Z" fill="rgba(255,255,255,0.12)" />
          <!-- Left branch -->
          <path d="M97 160 Q80 155 65 140 Q60 135 58 128" stroke="rgba(255,255,255,0.15)" stroke-width="3" fill="none" stroke-linecap="round" />
          <!-- Right branch -->
          <path d="M103 150 Q120 142 135 130 Q138 126 140 120" stroke="rgba(255,255,255,0.15)" stroke-width="3" fill="none" stroke-linecap="round" />
          <!-- Canopy - layered organic shapes -->
          <ellipse cx="100" cy="85" rx="65" ry="50" fill="rgba(255,255,255,0.06)" />
          <ellipse cx="75" cy="75" rx="42" ry="35" fill="rgba(255,255,255,0.05)" />
          <ellipse cx="125" cy="70" rx="38" ry="32" fill="rgba(255,255,255,0.05)" />
          <ellipse cx="90" cy="55" rx="35" ry="28" fill="rgba(255,255,255,0.04)" />
          <ellipse cx="115" cy="50" rx="30" ry="25" fill="rgba(255,255,255,0.04)" />
          <ellipse cx="100" cy="40" rx="25" ry="22" fill="rgba(255,255,255,0.03)" />
          <!-- Leaf clusters (subtle highlights) -->
          <circle cx="60" cy="80" r="18" fill="rgba(255,255,255,0.04)" />
          <circle cx="140" cy="75" r="16" fill="rgba(255,255,255,0.04)" />
          <circle cx="85" cy="45" r="14" fill="rgba(255,255,255,0.03)" />
          <circle cx="118" cy="42" r="12" fill="rgba(255,255,255,0.03)" />
        </svg>
      </div>

      <!-- Content -->
      <div class="brand-content">
        <h1 class="brand-title">
          <span class="title-cn">南昌大学</span>
          <span class="title-sub">心理健康服务预约系统</span>
        </h1>
        <div class="brand-divider"></div>
        <p class="brand-desc">关注心理健康，呵护心灵成长</p>
        <div class="brand-features">
          <div class="feature-item" style="--d:0.6s">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 6L9 17l-5-5"/></svg>
            <span>专业咨询师团队</span>
          </div>
          <div class="feature-item" style="--d:0.7s">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 6L9 17l-5-5"/></svg>
            <span>匿名倾诉通道</span>
          </div>
          <div class="feature-item" style="--d:0.8s">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 6L9 17l-5-5"/></svg>
            <span>预约便捷灵活</span>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        <p>&copy; 2026 南昌大学心理健康教育中心</p>
      </div>
    </div>

    <!-- ===== Right: Form Panel ===== -->
    <div class="form-panel">
      <!-- Corner leaf decorations -->
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
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请登录你的账号以继续</p>
        </div>

        <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="username">
            <label class="input-label">用户名</label>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <label class="input-label">密码</label>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register" class="link-text">立即注册</router-link>
        </div>

        <div class="divider-area">
          <span class="divider-line"></span>
          <span class="divider-text">测试账号</span>
          <span class="divider-line"></span>
        </div>

        <div class="test-accounts">
          <p class="test-hint">密码均为 <code>123456</code></p>
          <div class="test-grid">
            <div class="test-chip admin">
              <span class="chip-dot"></span>
              <span class="chip-label">管理员</span>
              <code>admin</code>
            </div>
            <div class="test-chip counselor">
              <span class="chip-dot"></span>
              <span class="chip-label">咨询师</span>
              <code>counselor1~4</code>
            </div>
            <div class="test-chip student">
              <span class="chip-dot"></span>
              <span class="chip-label">学生</span>
              <code>student1~10</code>
            </div>
            <div class="test-chip advisor">
              <span class="chip-dot"></span>
              <span class="chip-label">辅导员</span>
              <code>advisor1~4</code>
            </div>
          </div>
        </div>
      </div>
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
/* ========================================
   DESIGN TOKENS
   ======================================== */
/* Primary: #1E4D8C  Secondary: #2B6CB0  Accent: #4A9BD9
   Surface: #FAF8F5  Text: #1A1A2E  Muted: #6B7280 */

/* ========================================
   PAGE LAYOUT
   ======================================== */
.login-page {
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
  width: 48%;
  min-width: 420px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(155deg, #0B1D3A 0%, #132E5A 25%, #1E4D8C 55%, #2B6CB0 80%, #3A7FBD 100%);
  overflow: hidden;
}

/* Background blobs — organic, not circular */
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

.blob-1 {
  width: 500px; height: 500px;
  top: -15%; left: -10%;
  animation-delay: 0s;
}

.blob-2 {
  width: 350px; height: 350px;
  bottom: -10%; right: -5%;
  animation-delay: -8s;
  background: rgba(74, 155, 217, 0.06);
}

.blob-3 {
  width: 200px; height: 200px;
  top: 40%; right: 15%;
  animation-delay: -15s;
  background: rgba(255, 255, 255, 0.03);
}

@keyframes morphBlob {
  0%, 100% { border-radius: 40% 60% 55% 45% / 50% 40% 60% 50%; transform: translate(0, 0) scale(1); }
  33% { border-radius: 55% 45% 40% 60% / 45% 55% 45% 55%; transform: translate(15px, -20px) scale(1.05); }
  66% { border-radius: 45% 55% 60% 40% / 60% 45% 55% 45%; transform: translate(-10px, 15px) scale(0.97); }
}

/* Noise grain overlay */
.bg-noise {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.5'/%3E%3C/svg%3E");
  background-size: 128px;
  mix-blend-mode: overlay;
  pointer-events: none;
}

/* Floating particles */
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

/* Camphor tree */
.tree-scene {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 280px;
  height: 320px;
  opacity: 0.6;
  pointer-events: none;
  animation: treeBreath 8s ease-in-out infinite;
}

.camphor-tree {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 0 40px rgba(74, 155, 217, 0.15));
}

@keyframes treeBreath {
  0%, 100% { transform: translateX(-50%) scale(1); }
  50% { transform: translateX(-50%) scale(1.02); }
}

/* Brand content */
.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 56px;
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
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 6px;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
}

.title-sub {
  display: block;
  font-size: 17px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4px;
}

.brand-divider {
  width: 48px;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.6), transparent);
  margin: 0 auto 20px;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.8;
  margin: 0 0 36px;
  letter-spacing: 1px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.75);
  font-size: 13px;
  letter-spacing: 0.5px;
  animation: fadeInUp 0.6s ease-out both;
  animation-delay: var(--d, 0s);
}

.feature-item svg {
  flex-shrink: 0;
  color: #4A9BD9;
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

/* Corner leaf decorations */
.corner-deco {
  position: absolute;
  width: 140px;
  height: 140px;
  pointer-events: none;
  opacity: 0.8;
}

.top-right {
  top: 0;
  right: 0;
}

.bottom-left {
  bottom: 0;
  left: 0;
}

/* Form card */
.form-wrapper {
  width: 400px;
  padding: 44px 44px 36px;
  background: #fff;
  border-radius: 20px;
  box-shadow:
    0 0 0 1px rgba(30, 77, 140, 0.05),
    0 2px 4px rgba(30, 77, 140, 0.03),
    0 12px 40px rgba(30, 77, 140, 0.08);
  position: relative;
  z-index: 1;
  animation: cardEnter 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
  animation-delay: 0.2s;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* Subtle top accent line */
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
  margin-bottom: 32px;
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
  letter-spacing: 0.3px;
}

/* ========================================
   FORM INPUTS
   ======================================== */
.input-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #4B5563;
  margin-bottom: 6px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.login-form .el-form-item {
  margin-bottom: 22px;
}

/* ========================================
   LOGIN BUTTON
   ======================================== */
.login-btn {
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

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.12), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover {
  background-position: 100% 0;
  box-shadow: 0 8px 25px rgba(30, 77, 140, 0.35);
  transform: translateY(-1px);
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(30, 77, 140, 0.25);
}

/* ========================================
   REGISTER LINK
   ======================================== */
.register-link {
  text-align: center;
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

.link-text:hover {
  color: #1E4D8C;
}

/* ========================================
   DIVIDER
   ======================================== */
.divider-area {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 28px 0 20px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, #E5E7EB, transparent);
}

.divider-text {
  font-size: 11px;
  color: #B0B8C4;
  white-space: nowrap;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-weight: 600;
}

/* ========================================
   TEST ACCOUNTS
   ======================================== */
.test-hint {
  font-size: 11px;
  color: #B0B8C4;
  margin: 0 0 12px;
  text-align: center;
}

.test-hint code {
  background: #F3F4F6;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;
  color: #6B7280;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.test-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.test-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #FAFAFA;
  border: 1px solid #F0F0F0;
  border-radius: 10px;
  transition: all 0.2s ease;
  cursor: default;
}

.test-chip:hover {
  border-color: #D1D5DB;
  background: #F5F5F5;
}

.chip-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.test-chip.admin .chip-dot { background: #EF4444; }
.test-chip.counselor .chip-dot { background: #10B981; }
.test-chip.student .chip-dot { background: #3B82F6; }
.test-chip.advisor .chip-dot { background: #F59E0B; }

.chip-label {
  font-size: 11px;
  font-weight: 600;
  color: #4B5563;
  min-width: 36px;
}

.test-chip code {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 11px;
  color: #6B7280;
  background: none;
  padding: 0;
}

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

:deep(.el-input__inner::placeholder) {
  color: #C0C4CC;
}

:deep(.el-input__prefix .el-icon) {
  color: #9CA3AF;
}

:deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
}

/* ========================================
   RESPONSIVE
   ======================================== */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    background: linear-gradient(155deg, #0B1D3A 0%, #1E4D8C 50%, #2B6CB0 100%);
  }

  .corner-deco { display: none; }

  .form-wrapper {
    width: 90%;
    max-width: 400px;
    margin: 0 20px;
  }
}
</style>