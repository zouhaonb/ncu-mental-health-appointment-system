<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon :size="40"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingAppointments }}</div>
              <div class="stat-label">待处理预约</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon confirmed">
              <el-icon :size="40"><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.confirmedAppointments }}</div>
              <div class="stat-label">已确认预约</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon completed">
              <el-icon :size="40"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedAppointments }}</div>
              <div class="stat-label">已完成咨询</div>
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
              <div class="stat-value">{{ stats.unreadMessages }}</div>
              <div class="stat-label">未回复留言</div>
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
              <el-button link type="primary" @click="$router.push('/counselor/appointments')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentAppointments" style="width: 100%">
            <el-table-column label="学生">
              <template #default="{ row }">
                {{ row.user?.name || '未知' }}
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
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button
                  v-if="row.appointment.status === 'PENDING'"
                  type="success"
                  size="small"
                  @click="handleConfirm(row)"
                >
                  确认
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日日程</span>
            </div>
          </template>
          <el-empty v-if="todayAppointments.length === 0" description="今日暂无安排" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="app in todayAppointments"
              :key="app.id"
              :timestamp="app.timeSlot.startTime"
              placement="top"
            >
              <el-card>
                <h4>{{ app.student?.name }}</h4>
                <p>{{ app.appointment.reason }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { counselorAPI } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const stats = ref({
  pendingAppointments: 0,
  confirmedAppointments: 0,
  completedAppointments: 0,
  unreadMessages: 0
})

const recentAppointments = ref([])
const todayAppointments = ref([])
const counselorId = ref(null)

const getStatusType = (status) => {
  const types = { 'PENDING': 'warning', 'CONFIRMED': 'success', 'COMPLETED': 'info', 'CANCELLED': 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 'PENDING': '待确认', 'CONFIRMED': '已确认', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
  return texts[status] || status
}

const loadData = async () => {
  if (!counselorId.value) {
    console.error('counselorId 未加载')
    return
  }

  try {
    const appointmentsRes = await counselorAPI.getAppointments(counselorId.value)
    const appointments = appointmentsRes.data || []

    recentAppointments.value = appointments.slice(0, 5)

    stats.value.pendingAppointments = appointments.filter(
      app => app.appointment.status === 'PENDING'
    ).length
    stats.value.confirmedAppointments = appointments.filter(
      app => app.appointment.status === 'CONFIRMED'
    ).length
    stats.value.completedAppointments = appointments.filter(
      app => app.appointment.status === 'COMPLETED'
    ).length

    const today = new Date().toISOString().split('T')[0]
    todayAppointments.value = appointments.filter(
      app => app.timeSlot?.date === today && app.appointment.status === 'CONFIRMED'
    )

    console.log('加载预约数据成功:', {
      total: appointments.length,
      pending: stats.value.pendingAppointments,
      confirmed: stats.value.confirmedAppointments,
      completed: stats.value.completedAppointments,
      today: todayAppointments.value.length
    })
  } catch (error) {
    console.error('加载预约数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

const loadCounselorInfo = async () => {
  try {
    const res = await counselorAPI.getInfo()
    if (res && res.data && res.data.id) {
      counselorId.value = res.data.id
      console.log('获取咨询师ID成功:', counselorId.value)
      await loadData()
      await loadMessageStats()
    } else {
      console.error('获取咨询师信息失败:', res)
      ElMessage.error('获取咨询师信息失败')
    }
  } catch (error) {
    console.error('获取咨询师信息失败:', error)
    ElMessage.error('获取咨询师信息失败')
  }
}

const loadMessageStats = async () => {
  try {
    const res = await counselorAPI.getAnonymousMessages()
    const messages = res.data || []

    stats.value.unreadMessages = messages.filter(
      msg => msg.status === 'PENDING'
    ).length

    console.log('加载留言统计成功:', {
      total: messages.length,
      unread: stats.value.unreadMessages
    })
  } catch (error) {
    console.error('加载留言统计失败:', error)
  }
}

const handleConfirm = async (row) => {
  try {
    await counselorAPI.confirmAppointment(row.appointment.id)
    ElMessage.success('确认成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadCounselorInfo()
})
</script>

<style scoped>
.home-container { padding: 20px; }
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
.stat-icon.pending { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.confirmed { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.completed { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.message { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
