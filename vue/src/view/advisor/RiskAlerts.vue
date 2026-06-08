<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 焦虑预警 -->
      <el-tab-pane label="焦虑预警" name="ANXIETY">
        <div class="tab-header">
          <el-badge :value="anxietyUnreadCount" :max="99" :hidden="anxietyUnreadCount === 0" class="badge">
            <span class="tab-title">焦虑预警</span>
          </el-badge>
          <el-tag type="danger" size="large">高风险: {{ anxietyHighCount }}</el-tag>
          <el-tag type="warning" size="large">中风险: {{ anxietyMediumCount }}</el-tag>
        </div>
        <el-table :data="anxietyAlerts" v-loading="loading" stripe>
          <el-table-column label="学生" width="120">
            <template #default="{ row }">
              <span class="student-name">{{ row.studentName || '未知' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getRiskType(row.riskLevel)" effect="dark" size="large">
                {{ getRiskText(row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isRead ? 'info' : 'danger'" effect="light">
                {{ row.isRead ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center">
            <template #default="{ row }">
              <el-button
                v-if="!row.isRead"
                type="primary"
                size="small"
                @click="handleMarkAsRead(row.id)"
              >
                标记已读
              </el-button>
              <el-button
                v-if="!isInConcern(row.studentId)"
                type="warning"
                size="small"
                @click="handleAddConcern(row)"
              >
                重点关注
              </el-button>
              <el-button
                v-else
                type="info"
                size="small"
                disabled
              >
                已关注
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="anxietyAlerts.length === 0" description="暂无焦虑预警" />
      </el-tab-pane>

      <!-- 抑郁预警 -->
      <el-tab-pane label="抑郁预警" name="DEPRESSION">
        <div class="tab-header">
          <el-badge :value="depressionUnreadCount" :max="99" :hidden="depressionUnreadCount === 0" class="badge">
            <span class="tab-title">抑郁预警</span>
          </el-badge>
          <el-tag type="danger" size="large">高风险: {{ depressionHighCount }}</el-tag>
          <el-tag type="warning" size="large">中风险: {{ depressionMediumCount }}</el-tag>
        </div>
        <el-table :data="depressionAlerts" v-loading="loading" stripe>
          <el-table-column label="学生" width="120">
            <template #default="{ row }">
              <span class="student-name">{{ row.studentName || '未知' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getRiskType(row.riskLevel)" effect="dark" size="large">
                {{ getRiskText(row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isRead ? 'info' : 'danger'" effect="light">
                {{ row.isRead ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center">
            <template #default="{ row }">
              <el-button
                v-if="!row.isRead"
                type="primary"
                size="small"
                @click="handleMarkAsRead(row.id)"
              >
                标记已读
              </el-button>
              <el-button
                v-if="!isInConcern(row.studentId)"
                type="warning"
                size="small"
                @click="handleAddConcern(row)"
              >
                重点关注
              </el-button>
              <el-button
                v-else
                type="info"
                size="small"
                disabled
              >
                已关注
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="depressionAlerts.length === 0" description="暂无抑郁预警" />
      </el-tab-pane>

      <!-- 咨询师推荐 -->
      <el-tab-pane label="咨询师推荐" name="CONSULTATION">
        <div class="tab-header">
          <el-badge :value="consultationUnreadCount" :max="99" :hidden="consultationUnreadCount === 0" class="badge">
            <span class="tab-title">咨询师推荐</span>
          </el-badge>
          <el-tag type="danger" size="large">高风险: {{ consultationHighCount }}</el-tag>
          <el-tag type="warning" size="large">中风险: {{ consultationMediumCount }}</el-tag>
        </div>
        <el-table :data="consultationAlerts" v-loading="loading" stripe>
          <el-table-column label="学生" width="120">
            <template #default="{ row }">
              <span class="student-name">{{ row.studentName || '未知' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="风险等级" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getRiskType(row.riskLevel)" effect="dark" size="large">
                {{ getRiskText(row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isRead ? 'info' : 'danger'" effect="light">
                {{ row.isRead ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="预警时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center">
            <template #default="{ row }">
              <el-button
                v-if="!row.isRead"
                type="primary"
                size="small"
                @click="handleMarkAsRead(row.id)"
              >
                标记已读
              </el-button>
              <el-button
                v-if="!isInConcern(row.studentId)"
                type="warning"
                size="small"
                @click="handleAddConcern(row)"
              >
                重点关注
              </el-button>
              <el-button
                v-else
                type="info"
                size="small"
                disabled
              >
                已关注
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="consultationAlerts.length === 0" description="暂无咨询师推荐" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { advisorAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const alerts = ref([])
const activeTab = ref('ANXIETY')
const concernStudentIds = ref(new Set()) // 存储已在重点关注中的学生ID

// 分类计算
const anxietyAlerts = computed(() => {
  return alerts.value.filter(alert => alert.alertType === 'ANXIETY')
})

const depressionAlerts = computed(() => {
  return alerts.value.filter(alert => alert.alertType === 'DEPRESSION')
})

const consultationAlerts = computed(() => {
  return alerts.value.filter(alert => alert.alertType === 'CONSULTATION')
})

// 统计数量
const anxietyHighCount = computed(() => {
  return anxietyAlerts.value.filter(a => a.riskLevel === 'HIGH').length
})

const anxietyMediumCount = computed(() => {
  return anxietyAlerts.value.filter(a => a.riskLevel === 'MEDIUM').length
})

const depressionHighCount = computed(() => {
  return depressionAlerts.value.filter(a => a.riskLevel === 'HIGH').length
})

const depressionMediumCount = computed(() => {
  return depressionAlerts.value.filter(a => a.riskLevel === 'MEDIUM').length
})

const consultationHighCount = computed(() => {
  return consultationAlerts.value.filter(a => a.riskLevel === 'HIGH').length
})

const consultationMediumCount = computed(() => {
  return consultationAlerts.value.filter(a => a.riskLevel === 'MEDIUM').length
})

// 未读数量统计
const anxietyUnreadCount = computed(() => {
  return anxietyAlerts.value.filter(a => !a.isRead).length
})

const depressionUnreadCount = computed(() => {
  return depressionAlerts.value.filter(a => !a.isRead).length
})

const consultationUnreadCount = computed(() => {
  return consultationAlerts.value.filter(a => !a.isRead).length
})

// 判断学生是否已在重点关注中
const isInConcern = (studentId) => {
  return concernStudentIds.value.has(studentId)
}

const getRiskType = (level) => {
  const types = { 'LOW': 'success', 'MEDIUM': 'warning', 'HIGH': 'danger' }
  return types[level] || ''
}

const getRiskText = (level) => {
  const texts = { 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险' }
  return texts[level] || level
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await advisorAPI.getRiskAlerts(userStore.userId)
    alerts.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载重点关注学生列表
const loadConcernStudents = async () => {
  try {
    const res = await advisorAPI.getKeyConcernStudents(userStore.userId)
    concernStudentIds.value = new Set(
      res.data.map(item => item.student?.id || item.concern?.studentId)
    )
  } catch (error) {
    console.error('加载重点关注列表失败:', error)
  }
}

const handleMarkAsRead = async (id) => {
  try {
    await advisorAPI.markAlertAsRead(id)
    ElMessage.success('已标记为已读')
    loadData()

    // 触发刷新未读数量事件
    window.dispatchEvent(new CustomEvent('refresh-unread-count'))
  } catch (error) {
    console.error(error)
  }
}

const handleAddConcern = async (row) => {
  try {
    // 先获取辅导员信息得到 advisorId
    const advisorInfo = await advisorAPI.getInfo()
    const data = {
      advisorId: advisorInfo.data.id,
      studentId: row.studentId,
      notes: `风险预警：${row.alertSource === 'SELF_ASSESSMENT' ? '自测' : '咨询师评估'}，风险等级：${getRiskText(row.riskLevel)}，类型：${row.alertType === 'DEPRESSION' ? '抑郁' : row.alertType === 'ANXIETY' ? '焦虑' : '咨询评估'}`
    }

    await advisorAPI.addKeyConcernStudent(data)
    ElMessage.success('已添加到重点关注')

    // 更新本地重点关注列表
    concernStudentIds.value.add(row.studentId)

    // 触发刷新未读数量事件
    window.dispatchEvent(new CustomEvent('refresh-unread-count'))
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      console.error(error)
      ElMessage.error('添加失败')
    }
  }
}

onMounted(() => {
  loadData()
  loadConcernStudents()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.tab-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding: 10px 0;
}

.badge {
  margin-right: 10px;
}

.tab-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.student-name {
  font-weight: 500;
  color: #303133;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-table) {
  margin-top: 10px;
}
</style>
