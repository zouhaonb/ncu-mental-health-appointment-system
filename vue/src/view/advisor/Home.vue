<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon alert">
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

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon students">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-label">学院学生</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新预警</span>
              <el-button link type="primary" @click="$router.push('/advisor/alerts')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentAlerts" style="width: 100%">
            <el-table-column label="学生">
              <template #default="{ row }">
                {{ row.studentName || '未知' }}
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险等级">
              <template #default="{ row }">
                <el-tag :type="getRiskType(row.riskLevel)">
                  {{ getRiskText(row.riskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertSource" label="来源">
              <template #default="{ row }">
                {{ row.alertSource === 'SELF_ASSESSMENT' ? '心理测评' : '咨询师上报' }}
              </template>
            </el-table-column>
            <el-table-column prop="isRead" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.isRead ? 'info' : 'danger'">
                  {{ row.isRead ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-button type="warning" size="large" style="width: 100%" @click="$router.push('/advisor/alerts')">
                <el-icon><Warning /></el-icon>
                <span>处理预警</span>
              </el-button>
            </el-col>
            <el-col :span="12">
              <el-button type="success" size="large" style="width: 100%" @click="$router.push('/advisor/follow-ups')">
                <el-icon><Phone /></el-icon>
                <span>回访任务</span>
              </el-button>
            </el-col>
            <el-col :span="12" style="margin-top: 15px">
              <el-button type="primary" size="large" style="width: 100%" @click="$router.push('/advisor/concern-students')">
                <el-icon><Star /></el-icon>
                <span>重点关注</span>
              </el-button>
            </el-col>
            <el-col :span="12" style="margin-top: 15px">
              <el-button type="info" size="large" style="width: 100%" @click="$router.push('/advisor/statistics')">
                <el-icon><DataAnalysis /></el-icon>
                <span>统计数据</span>
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { advisorAPI } from '@/api'

const userStore = useUserStore()

const stats = ref({
  totalAlerts: 0,
  unreadAlerts: 0,
  concernCount: 0,
  totalStudents: 0
})

const recentAlerts = ref([])

const getRiskType = (level) => {
  const types = { 'LOW': 'success', 'MEDIUM': 'warning', 'HIGH': 'danger' }
  return types[level] || ''
}

const getRiskText = (level) => {
  const texts = { 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险' }
  return texts[level] || level
}

const loadData = async () => {
  try {
    const statsRes = await advisorAPI.getCollegeStatistics(userStore.userId)
    stats.value = statsRes.data

    const alertsRes = await advisorAPI.getRiskAlerts(userStore.userId)
    recentAlerts.value = alertsRes.data.slice(0, 5)
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-container { padding: 20px; }
.stat-card { margin-bottom: 20px; }
.stat-content { display: flex; align-items: center; gap: 15px; }
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}
.stat-icon.alert {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
}
.stat-icon.unread {
  background: linear-gradient(135deg, #4ECDC4 0%, #6ED5CF 100%);
}
.stat-icon.concern {
  background: linear-gradient(135deg, #FFA07A 0%, #FFB899 100%);
}
.stat-icon.students {
  background: linear-gradient(135deg, #7BC67E 0%, #98D49B 100%);
}
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
