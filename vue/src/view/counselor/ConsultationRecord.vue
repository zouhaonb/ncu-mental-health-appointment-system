<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>咨询记录管理</span>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 填写记录 -->
        <el-tab-pane label="填写记录" name="write">
          <el-form :model="form" label-width="120px" style="max-width: 800px">
            <el-form-item label="选择预约" required>
              <el-select
                v-model="form.appointmentId"
                placeholder="请选择已确认的预约"
                style="width: 100%"
                @change="handleAppointmentChange"
              >
                <el-option
                  v-for="item in availableAppointments"
                  :key="item.appointment.id"
                  :label="`${item.user?.name || '未知学生'} - ${item.timeSlot?.date} ${item.timeSlot?.startTime}`"
                  :value="item.appointment.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="咨询内容" required>
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请详细记录本次咨询的内容"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="评估意见" required>
              <el-input
                v-model="form.assessment"
                type="textarea"
                :rows="4"
                placeholder="请输入评估意见"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="风险等级" required>
              <el-radio-group v-model="form.riskLevel">
                <el-radio label="LOW">低风险</el-radio>
                <el-radio label="MEDIUM">中风险</el-radio>
                <el-radio label="HIGH">高风险</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-alert
                v-if="form.riskLevel === 'HIGH'"
                title="高风险预警：提交后将自动通知辅导员"
                type="warning"
                :closable="false"
                style="margin-bottom: 15px"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">提交记录</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 咨询历史 -->
        <el-tab-pane label="咨询历史" name="history">
          <div class="search-bar">
            <el-input
              v-model="searchForm.studentName"
              placeholder="请输入学生姓名"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="margin-left: 10px"
            />
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleResetSearch">重置</el-button>
          </div>

          <el-table :data="historyData" v-loading="loading" border stripe>
            <el-table-column prop="studentName" label="学生姓名" width="120" />
            <el-table-column prop="appointmentDate" label="咨询日期" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险等级" width="120">
              <template #default="{ row }">
                <el-tag :type="getRiskTagType(row.riskLevel)" size="small">
                  {{ getRiskText(row.riskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="appointmentStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getAppointmentStatusType(row.appointmentStatus)" size="small">
                  {{ getAppointmentStatusText(row.appointmentStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="咨询内容" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleViewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; justify-content: flex-end"
            @size-change="loadHistory"
            @current-change="loadHistory"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="咨询记录详情" width="700px">
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="学生姓名">{{ currentRecord.studentName }}</el-descriptions-item>
        <el-descriptions-item label="咨询日期">{{ formatDateTime(currentRecord.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskTagType(currentRecord.riskLevel)">
            {{ getRiskText(currentRecord.riskLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否结案">
          <el-tag :type="currentRecord.isClosed ? 'success' : 'warning'">
            {{ currentRecord.isClosed ? '已结案' : '未结案' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="咨询内容">
          <div style="white-space: pre-wrap">{{ currentRecord.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="评估意见">
          <div style="white-space: pre-wrap">{{ currentRecord.assessment }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { counselorAPI } from '@/api'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('write')
const loading = ref(false)
const submitting = ref(false)
const counselorId = ref(null)

// 表单数据
const form = reactive({
  appointmentId: null,
  studentId: null,
  counselorId: null,
  content: '',
  assessment: '',
  riskLevel: 'LOW'
})

// 搜索表单
const searchForm = reactive({
  studentName: '',
  dateRange: null
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 数据
const availableAppointments = ref([])
const historyData = ref([])
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

// 加载可预约的列表
const loadAvailableAppointments = async () => {
  try {
    const res = await counselorAPI.getAppointments(counselorId.value, 'CONFIRMED')
    availableAppointments.value = res.data || []
  } catch (error) {
    console.error('加载预约列表失败:', error)
  }
}

// 加载咨询历史
const loadHistory = async () => {
  loading.value = true
  try {
    const res = await counselorAPI.getConsultationRecords(counselorId.value)
    let data = res.data || []

    // 将 isClosed 映射为 isCompleted，并添加 appointmentStatus
    data = data.map(item => ({
      ...item,
      isCompleted: item.isClosed === 1 || item.isClosed === true,
      appointmentStatus: item.appointmentStatus || 'COMPLETED'
    }))

    // 搜索过滤
    if (searchForm.studentName) {
      data = data.filter(item =>
        item.studentName?.includes(searchForm.studentName)
      )
    }

    pagination.total = data.length
    const start = (pagination.currentPage - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    historyData.value = data.slice(start, end)
  } catch (error) {
    console.error('加载咨询历史失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// Tab切换
const handleTabClick = (tab) => {
  if (tab.props.name === 'history') {
    loadHistory()
  }
}

// 选择预约
const handleAppointmentChange = (appointmentId) => {
  const selected = availableAppointments.value.find(
    item => item.appointment.id === appointmentId
  )
  if (selected) {
    form.studentId = selected.appointment.studentId
  }
}

// 提交记录
const handleSubmit = async () => {
  if (!form.appointmentId) {
    ElMessage.warning('请选择预约')
    return
  }
  if (!form.content || !form.content.trim()) {
    ElMessage.warning('请填写咨询内容')
    return
  }
  if (!form.assessment || !form.assessment.trim()) {
    ElMessage.warning('请填写评估意见')
    return
  }

  form.counselorId = counselorId.value

  submitting.value = true
  try {
    await counselorAPI.saveConsultationRecord(form)
    ElMessage.success('保存成功')
    handleReset()
    loadAvailableAppointments()
    loadHistory()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  form.appointmentId = null
  form.studentId = null
  form.content = ''
  form.assessment = ''
  form.riskLevel = 'LOW'
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadHistory()
}

// 重置搜索
const handleResetSearch = () => {
  searchForm.studentName = ''
  searchForm.dateRange = null
  pagination.currentPage = 1
  loadHistory()
}

// 查看详情
const handleViewDetail = (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 工具函数
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN')
}

const getRiskText = (level) => {
  const map = { 'LOW': '低风险', 'MEDIUM': '中风险', 'HIGH': '高风险' }
  return map[level] || level
}

const getRiskTagType = (level) => {
  const map = { 'LOW': 'success', 'MEDIUM': 'warning', 'HIGH': 'danger' }
  return map[level] || ''
}

const getAppointmentStatusText = (status) => {
  const texts = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

const getAppointmentStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return types[status] || ''
}

// 初始化
onMounted(async () => {
  try {
    const counselorInfo = await counselorAPI.getInfo()
    if (counselorInfo && counselorInfo.data && counselorInfo.data.id) {
      counselorId.value = counselorInfo.data.id
      await loadAvailableAppointments()

      // 处理路由参数
      const tabParam = route.query.tab
      const appointmentIdParam = route.query.appointmentId

      if (tabParam) {
        activeTab.value = tabParam

        // 如果是填写记录Tab且有预约ID参数，自动选中该预约
        if (tabParam === 'write' && appointmentIdParam) {
          const appointmentId = Number(appointmentIdParam)
          form.appointmentId = appointmentId

          // 触发预约选择事件，自动填充学生ID
          handleAppointmentChange(appointmentId)

          console.log('自动选中预约:', appointmentId)
        }
      }
    }
  } catch (error) {
    console.error('获取咨询师信息失败:', error)
  }
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
</style>