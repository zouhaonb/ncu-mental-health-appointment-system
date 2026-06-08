<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon students">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-label">学院学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon alerts">
              <el-icon :size="40"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalAlerts }}</div>
              <div class="stat-label">预警总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon unread">
              <el-icon :size="40"><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.unreadAlerts }}</div>
              <div class="stat-label">未读预警</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon concern">
              <el-icon :size="40"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.concernCount }}</div>
              <div class="stat-label">重点关注</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>学院统计详情</span>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="学院名称">
          {{ stats.college || '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="学生总数">
          {{ stats.totalStudents }}
        </el-descriptions-item>
        <el-descriptions-item label="预警总数">
          {{ stats.totalAlerts }}
        </el-descriptions-item>
        <el-descriptions-item label="未读预警">
          {{ stats.unreadAlerts }}
        </el-descriptions-item>
        <el-descriptions-item label="重点关注学生">
          {{ stats.concernCount }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { advisorAPI } from '@/api'

const userStore = useUserStore()

const stats = ref({
  college: '',
  totalStudents: 0,
  totalAlerts: 0,
  unreadAlerts: 0,
  concernCount: 0
})

const loadData = async () => {
  try {
    const res = await advisorAPI.getCollegeStatistics(userStore.userId)
    stats.value = res.data
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.stat-card { margin-bottom: 20px; }
.stat-content { display: flex; align-items: center; gap: 15px; }
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}
.stat-icon.students { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.alerts { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.unread { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.concern { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
</style>
