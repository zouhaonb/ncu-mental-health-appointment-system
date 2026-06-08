<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">心理健康系统</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/advisor/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/advisor/alerts">
          <el-icon><Warning /></el-icon>
          <span>心理预警</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" style="margin-left: 10px" />
        </el-menu-item>
        <el-menu-item index="/advisor/concern-students">
          <el-icon><Star /></el-icon>
          <span>重点关注</span>
        </el-menu-item>
        <el-menu-item index="/advisor/referrals">
          <el-icon><Connection /></el-icon>
          <span>转介管理</span>
        </el-menu-item>
        <el-menu-item index="/advisor/follow-ups">
          <el-icon><Phone /></el-icon>
          <span>回访任务</span>
        </el-menu-item>
        <el-menu-item index="/advisor/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>学院统计</span>
        </el-menu-item>
        <el-menu-item index="/advisor/articles">
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
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { advisorAPI } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const unreadCount = ref(0)

// 监听刷新未读数量的事件
const handleRefreshUnread = () => {
  loadUnreadCount()
}

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

const loadUnreadCount = async () => {
  try {
    const res = await advisorAPI.getUnreadAlertCount(userStore.userId)
    unreadCount.value = res.data
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadUnreadCount()
  // 监听自定义事件
  window.addEventListener('refresh-unread-count', handleRefreshUnread)
})

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener('refresh-unread-count', handleRefreshUnread)
})
</script>

<style scoped>
.layout-container { height: 100vh; }
.el-aside { background-color: #304156; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4b;
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
}
.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.welcome { font-size: 16px; color: #606266; }
</style>
