<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon assessment">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalAssessments }}</div>
              <div class="stat-label">测评次数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon appointment">
              <el-icon :size="40"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalAppointments }}</div>
              <div class="stat-label">预约次数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon :size="40"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingAppointments }}</div>
              <div class="stat-label">待确认预约</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon message">
              <el-icon :size="40"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.messages }}</div>
              <div class="stat-label">留言数</div>
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
              <span>最近预约</span>
              <el-button link type="primary" @click="$router.push('/student/my-appointments')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentAppointments" style="width: 100%">
            <el-table-column label="咨询师">
              <template #default="{ row }">
                {{ row.counselor?.name || '未知' }}
              </template>
            </el-table-column>
            <el-table-column label="时间">
              <template #default="{ row }">
                {{ row.timeSlot?.date }} {{ row.timeSlot?.startTime }}
              </template>
            </el-table-column>
            <el-table-column prop="appointment.status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.appointment.status)">
                  {{ getStatusText(row.appointment.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近测评</span>
              <el-button link type="primary" @click="$router.push('/student/assessment-history')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentAssessments" style="width: 100%">
            <el-table-column prop="scaleType" label="量表类型">
              <template #default="{ row }">
                {{ row.scaleType === 'DEPRESSION' ? '抑郁自评' : '焦虑自评' }}
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="得分" />
            <el-table-column prop="riskLevel" label="风险等级">
              <template #default="{ row }">
                <el-tag :type="getRiskType(row.riskLevel)">
                  {{ getRiskText(row.riskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>快速入口</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="primary" size="large" style="width: 100%" @click="$router.push('/student/assessment')">
            <el-icon><Document /></el-icon>
            <span>开始测评</span>
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" size="large" style="width: 100%" @click="$router.push('/student/counselors')">
            <el-icon><UserFilled /></el-icon>
            <span>预约咨询</span>
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" size="large" style="width: 100%" @click="$router.push('/student/anonymous-message')">
            <el-icon><ChatDotRound /></el-icon>
            <span>匿名留言</span>
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" size="large" style="width: 100%" @click="$router.push('/student/articles')">
            <el-icon><Reading /></el-icon>
            <span>阅读文章</span>
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'

const userStore = useUserStore()
const studentId = ref(null)

const stats = ref({
  totalAssessments: 0,
  totalAppointments: 0,
  pendingAppointments: 0,
  messages: 0
})

const recentAppointments = ref([])
const recentAssessments = ref([])

const getStatusType = (status) => {
  const types = { 'PENDING': 'warning', 'CONFIRMED': 'success', 'COMPLETED': 'info', 'CANCELLED': 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 'PENDING': '待确认', 'CONFIRMED': '已确认', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
  return texts[status] || status
}

const getRiskType = (level) => {
  const types = { 'NORMAL': 'info', 'LOW': 'success', 'MEDIUM': 'warning', 'HIGH': 'danger' }
  return types[level] || ''
}

const getRiskText = (level) => {
  const texts = { 'NORMAL': '正常', 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险' }
  return texts[level] || level
}

const loadData = async () => {
  try {
    // 先获取学生信息，得到正确的 studentId
    if (!studentId.value) {
      console.log('开始获取学生信息...')
      const studentInfo = await studentAPI.getInfo()
      console.log('学生信息响应:', studentInfo)

      if (studentInfo && studentInfo.data && studentInfo.data.id) {
        studentId.value = studentInfo.data.id
        console.log('获取到 studentId:', studentId.value)
      } else {
        console.error('学生信息格式错误:', studentInfo)
        ElMessage.error('获取学生信息失败')
        return
      }
    }

    // 1. 获取预约数据
    console.log('开始获取预约数据, studentId:', studentId.value)
    const appointmentsRes = await studentAPI.getMyAppointments(studentId.value)
    console.log('预约数据响应:', appointmentsRes)

    const appointments = appointmentsRes.data || []
    recentAppointments.value = appointments.slice(0, 5)

    // 统计预约次数
    stats.value.totalAppointments = appointments.length

    // 统计待确认预约数（PENDING状态）
    stats.value.pendingAppointments = appointments.filter(
      app => app.appointment.status === 'PENDING'
    ).length
    console.log('待确认预约数:', stats.value.pendingAppointments)

    // 2. 获取测评数据
    console.log('开始获取测评数据, userId:', userStore.userId)
    const assessmentsRes = await studentAPI.getAssessmentHistory(userStore.userId)
    console.log('测评数据响应:', assessmentsRes)

    const assessments = assessmentsRes.data || []
    recentAssessments.value = assessments.slice(0, 5)
    stats.value.totalAssessments = assessments.length
    console.log('测评次数:', stats.value.totalAssessments)

    // 3. 获取留言数据
    console.log('开始获取留言数据, userId:', userStore.userId)
    const messagesRes = await studentAPI.getMyAnonymousMessages(userStore.userId)
    console.log('留言数据响应:', messagesRes)

    const messages = messagesRes.data || []
    stats.value.messages = messages.length
    console.log('留言数:', stats.value.messages)

    console.log('首页数据加载完成:', stats.value)
  } catch (error) {
    console.error('加载首页数据失败:', error)
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.assessment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.appointment {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.message {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
