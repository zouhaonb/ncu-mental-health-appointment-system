<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <svg class="logo-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <ellipse cx="12" cy="9" rx="8" ry="7" fill="rgba(74,155,217,0.25)"/>
          <ellipse cx="9" cy="8" rx="5" ry="4.5" fill="rgba(74,155,217,0.2)"/>
          <ellipse cx="15" cy="7.5" rx="4.5" ry="4" fill="rgba(74,155,217,0.2)"/>
          <rect x="11" y="14" width="2" height="8" rx="1" fill="rgba(255,255,255,0.3)"/>
        </svg>
        <span>心理健康系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1B3A5C"
        text-color="#B0C4DE"
        active-text-color="#3D7EC7"
      >
        <el-menu-item index="/counselor/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/counselor/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/counselor/time-slots">
          <el-icon><Clock /></el-icon>
          <span>可预约时段</span>
        </el-menu-item>
        <el-menu-item index="/counselor/appointments">
          <el-icon><Calendar /></el-icon>
          <span>预约处理</span>
        </el-menu-item>
        <el-menu-item index="/counselor/consultation-record">
          <el-icon><Document /></el-icon>
          <span>咨询记录</span>
        </el-menu-item>
        <el-menu-item index="/counselor/anonymous-messages">
          <el-icon><ChatDotRound /></el-icon>
          <span>匿名留言</span>
        </el-menu-item>
        <el-menu-item index="/counselor/referral-suggestions">
          <el-icon><Share /></el-icon>
          <span>转介建议</span>
        </el-menu-item>
        <el-menu-item index="/counselor/articles">
          <el-icon><Reading /></el-icon>
          <span>心灵广场</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-content">
          <span class="welcome">欢迎, {{ userStore.name }}</span>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { HomeFilled, User, Calendar, ChatDotRound, Share, Document, Clock, Reading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.el-aside { background-color: #1B3A5C; overflow: hidden; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-size: 17px;
  font-weight: 700;
  background-color: #152D4A;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  letter-spacing: 1px;
}
.logo-icon { color: #8EB8E0; flex-shrink: 0; }
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e8ecf1;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 4px rgba(27, 58, 92, 0.04);
}
.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.welcome { font-size: 15px; color: #4a5568; font-weight: 500; }
:deep(.el-menu) { border-right: none; }
:deep(.el-menu-item) {
  height: 48px; line-height: 48px;
  margin: 2px 8px; padding-left: 20px !important;
  transition: all 0.2s ease;
}
:deep(.el-menu-item:hover) { background-color: rgba(61, 126, 199, 0.12) !important; }
:deep(.el-menu-item.is-active) {
  background-color: rgba(61, 126, 199, 0.18) !important;
  border-radius: 8px; font-weight: 600;
}
</style>
