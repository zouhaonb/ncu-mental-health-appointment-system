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
