<template>
  <div class="time-slot-page">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Calendar /></el-icon>
            <span class="header-title">我的可预约时段</span>
            <el-badge :value="activeSlotCount" :max="99" class="slot-badge" type="primary" />
          </div>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">
            添加时段
          </el-button>
        </div>
      </template>

      <div class="table-wrapper" v-loading="loading">
        <el-table
          :data="timeSlots"
          stripe
          :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600', fontSize: '14px' }"
          :row-style="{ height: '64px' }"
          empty-text="暂无时段数据，点击右上角添加时段"
          style="width: 100%"
        >
          <el-table-column prop="date" label="日期" min-width="140" align="left">
            <template #default="{ row }">
              <div class="date-cell">
                <el-icon class="date-icon"><Clock /></el-icon>
                <span>{{ row.date }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="时间段" min-width="140" align="left">
            <template #default="{ row }">
              <div class="time-cell">
                <span class="time-tag">{{ formatTime(row.startTime) }}</span>
                <span class="time-separator-text">-</span>
                <span class="time-tag">{{ formatTime(row.endTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="最大预约数" min-width="120" align="center">
            <template #default="{ row }">
              <span class="max-appointments">{{ row.maxAppointments }}人</span>
            </template>
          </el-table-column>

          <el-table-column label="预约情况" min-width="160" align="center">
            <template #default="{ row }">
              <div class="booking-cell">
                <el-progress
                  :percentage="getBookingPercentage(row)"
                  :color="getProgressColor(row)"
                  :stroke-width="6"
                  :show-text="false"
                  class="booking-progress"
                />
                <span class="booking-text">{{ row.bookedCount }} / {{ row.maxAppointments }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" min-width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getStatusType(row)"
                effect="light"
                size="default"
                round
                class="status-tag"
              >
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" min-width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                :disabled="!canEdit(row)"
                :icon="Edit"
                class="action-btn"
              >
                编辑
              </el-button>
              <el-tooltip
                v-if="!canToggleStatus(row)"
                :content="getDisableReason(row)"
                placement="top"
                :show-after="300"
              >
                <el-button
                  :type="getButtonType(row)"
                  size="small"
                  disabled
                  :icon="row.status === 'ACTIVE' || row.status === 'BOOKED' ? 'Lock' : 'Unlock'"
                  class="action-btn"
                >
                  {{ row.status === 'ACTIVE' || row.status === 'BOOKED' ? '停用' : '启用' }}
                </el-button>
              </el-tooltip>
              <el-button
                v-else
                :type="getButtonType(row)"
                size="small"
                @click="handleToggleStatus(row)"
                :icon="row.status === 'ACTIVE' || row.status === 'BOOKED' ? 'CircleClose' : 'CircleCheck'"
                class="action-btn"
              >
                {{ row.status === 'ACTIVE' || row.status === 'BOOKED' ? '停用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="批量添加时段"
      width="600px"
      :close-on-click-modal="false"
      class="add-slot-dialog"
    >
      <el-form :model="form" label-width="100px" class="slot-form">
        <el-form-item label="选择日期" required>
          <el-date-picker
            v-model="form.date"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="最大预约数">
          <el-input-number
            v-model="form.maxAppointments"
            :min="1"
            :max="20"
            style="width: 100%"
          />
        </el-form-item>

        <el-divider content-position="left">
          <el-icon><Timer /></el-icon>
          时间段设置
        </el-divider>

        <div class="time-ranges-container">
          <transition-group name="list">
            <div v-for="(timeRange, index) in form.timeRanges" :key="index" class="time-range-item">
              <div class="time-range-header">
                <span class="range-index">时段 {{ index + 1 }}</span>
                <el-button
                  type="danger"
                  size="small"
                  @click="removeTimeRange(index)"
                  :disabled="form.timeRanges.length === 1"
                  :icon="Delete"
                  circle
                  plain
                />
              </div>
              <el-row :gutter="12">
                <el-col :span="11">
                  <el-time-picker
                    v-model="timeRange.startTime"
                    placeholder="开始时间"
                    style="width: 100%"
                    format="HH:mm"
                    value-format="HH:mm:ss"
                  />
                </el-col>
                <el-col :span="2" class="time-separator">
                  <span>至</span>
                </el-col>
                <el-col :span="11">
                  <el-time-picker
                    v-model="timeRange.endTime"
                    placeholder="结束时间"
                    style="width: 100%"
                    format="HH:mm"
                    value-format="HH:mm:ss"
                  />
                </el-col>
              </el-row>
            </div>
          </transition-group>
        </div>

        <el-button
          type="primary"
          plain
          @click="addTimeRange"
          class="add-range-btn"
          :icon="Plus"
        >
          添加时间段
        </el-button>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认添加
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑时段"
      width="500px"
      :close-on-click-modal="false"
      class="edit-slot-dialog"
    >
      <el-form :model="editForm" label-width="100px" class="slot-form">
        <el-form-item label="日期" required>
          <el-date-picker
            v-model="editForm.date"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            :disabled="editForm.isExpired || editForm.hasBooking"
          />
        </el-form-item>

        <el-form-item label="时间段" required>
          <el-row :gutter="12">
            <el-col :span="11">
              <el-time-picker
                v-model="editForm.startTime"
                placeholder="开始时间"
                style="width: 100%"
                format="HH:mm"
                value-format="HH:mm:ss"
                :disabled="editForm.isExpired || editForm.hasBooking"
              />
            </el-col>
            <el-col :span="2" class="time-separator">
              <span>至</span>
            </el-col>
            <el-col :span="11">
              <el-time-picker
                v-model="editForm.endTime"
                placeholder="结束时间"
                style="width: 100%"
                format="HH:mm"
                value-format="HH:mm:ss"
                :disabled="editForm.isExpired || editForm.hasBooking"
              />
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="最大预约数" required>
          <el-input-number
            v-model="editForm.maxAppointments"
            :min="editForm.currentBookedCount"
            :max="20"
            style="width: 100%"
            :disabled="editForm.isExpired"
          />
          <div v-if="editForm.hasBooking" class="form-tip">
            当前已有 {{ editForm.currentBookedCount }} 个预约，最大预约数不能小于当前预约数
          </div>
        </el-form-item>

        <el-alert
          v-if="editForm.isExpired"
          title="已过期的时段仅可修改最大预约数"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 12px"
        />

        <el-alert
          v-if="editForm.hasBooking"
          title="该时段已有预约，仅可修改最大预约数"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 12px"
        />
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateSubmit" :loading="submitting">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Plus,
  Clock,
  Lock,
  Unlock,
  CircleClose,
  CircleCheck,
  Timer,
  Delete,
  Edit
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { counselorAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const timeSlots = ref([])
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const counselorId = ref(null)

// 最大可预约天数，默认14天，由后端在创建时段时根据系统配置校验
const maxFutureDays = ref(14)

const form = reactive({
  date: '',
  maxAppointments: 1,
  timeRanges: [
    { startTime: '', endTime: '' }
  ]
})

const editForm = reactive({
  id: null,
  date: '',
  startTime: '',
  endTime: '',
  maxAppointments: 1,
  currentBookedCount: 0,
  isExpired: false,
  hasBooking: false
})

const activeSlotCount = computed(() => {
  return timeSlots.value.filter(slot => {
    return !isExpired(slot) && (slot.status === 'ACTIVE' || slot.status === 'BOOKED')
  }).length
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.substring(0, 5)
}

const isExpired = (row) => {
  const now = new Date()
  const slotEndDateTime = new Date(row.date + 'T' + row.endTime)
  return slotEndDateTime < now
}

const canEdit = (row) => {
  if (isExpired(row)) return false
  if (row.bookedCount > 0 && row.status !== 'INACTIVE') return false
  return true
}

const getStatusType = (row) => {
  if (isExpired(row)) return 'info'
  if (row.status === 'FULL') return 'danger'
  if (row.status === 'BOOKED') return 'warning'
  return row.status === 'ACTIVE' ? 'success' : 'info'
}

const getStatusText = (row) => {
  if (isExpired(row)) return '已过期'
  if (row.status === 'FULL') return '已满'
  if (row.status === 'BOOKED') return '已预约'
  return row.status === 'ACTIVE' ? '可用' : '停用'
}

const getButtonType = (row) => {
  if (row.status === 'ACTIVE' || row.status === 'BOOKED') {
    return 'warning'
  }
  return 'success'
}

const getBookingPercentage = (row) => {
  if (!row.maxAppointments) return 0
  return Math.round((row.bookedCount / row.maxAppointments) * 100)
}

const getProgressColor = (row) => {
  const percentage = getBookingPercentage(row)
  if (percentage >= 100) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#409eff'
}

const canToggleStatus = (row) => {
  if (isExpired(row)) return false
  if (row.status === 'FULL') return false
  if (row.status === 'ACTIVE' || row.status === 'BOOKED' || row.status === 'INACTIVE') {
    return true
  }
  return false
}

const getDisableReason = (row) => {
  if (isExpired(row)) return '已过期的时段不可编辑'
  if (row.status === 'FULL') return '时段已满，无法停用'
  return ''
}

const addTimeRange = () => {
  form.timeRanges.push({ startTime: '', endTime: '' })
}

const removeTimeRange = (index) => {
  if (form.timeRanges.length > 1) {
    form.timeRanges.splice(index, 1)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    if (!counselorId.value) {
      const counselorInfo = await counselorAPI.getInfo()
      counselorId.value = counselorInfo.data.id
      console.log('获取到 counselorId:', counselorId.value)
    }

    const res = await counselorAPI.getMyTimeSlots(counselorId.value)
    // 强制替换数组以确保 Vue 响应式更新
    timeSlots.value = []
    await new Promise(resolve => setTimeout(resolve, 50))
    timeSlots.value = res.data || []
    console.log('时段数据:', timeSlots.value)
  } catch (error) {
    console.error('加载时段数据失败:', error)
    ElMessage.error('加载时段数据失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  form.date = ''
  form.maxAppointments = 1
  form.timeRanges = [
    { startTime: '', endTime: '' }
  ]
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.date) {
    ElMessage.warning('请选择日期')
    return
  }

  const validTimeRanges = form.timeRanges.filter(range => range.startTime && range.endTime)
  if (validTimeRanges.length === 0) {
    ElMessage.warning('请至少添加一个时间段')
    return
  }

  for (let i = 0; i < validTimeRanges.length; i++) {
    const range = validTimeRanges[i]
    if (range.startTime >= range.endTime) {
      ElMessage.warning(`第${i + 1}个时间段的开始时间必须早于结束时间`)
      return
    }
  }

  submitting.value = true
  try {
    if (!counselorId.value) {
      const counselorInfo = await counselorAPI.getInfo()
      counselorId.value = counselorInfo.data.id
    }

    const slots = validTimeRanges.map(range => ({
      counselorId: counselorId.value,
      date: form.date,
      startTime: range.startTime,
      endTime: range.endTime,
      maxAppointments: form.maxAppointments
    }))

    console.log('批量提交时段数据:', slots)
    await counselorAPI.batchAddTimeSlots(slots)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('添加时段失败:', error)
    ElMessage.error(error.response?.data?.message || '添加时段失败')
  } finally {
    submitting.value = false
  }
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.date = row.date
  editForm.startTime = row.startTime
  editForm.endTime = row.endTime
  editForm.maxAppointments = row.maxAppointments
  editForm.currentBookedCount = row.bookedCount
  editForm.isExpired = isExpired(row)
  editForm.hasBooking = row.bookedCount > 0
  editDialogVisible.value = true
}

const handleUpdateSubmit = async () => {
  if (!editForm.date) {
    ElMessage.warning('请选择日期')
    return
  }

  if (!editForm.startTime || !editForm.endTime) {
    ElMessage.warning('请选择时间段')
    return
  }

  if (editForm.startTime >= editForm.endTime) {
    ElMessage.warning('开始时间必须早于结束时间')
    return
  }

  if (editForm.maxAppointments < editForm.currentBookedCount) {
    ElMessage.warning(`最大预约数不能小于当前预约数: ${editForm.currentBookedCount}`)
    return
  }

  submitting.value = true
  try {
    const updateData = {
      date: editForm.date,
      startTime: editForm.startTime,
      endTime: editForm.endTime,
      maxAppointments: editForm.maxAppointments
    }

    await counselorAPI.updateTimeSlot(editForm.id, updateData)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('更新时段失败:', error)
    ElMessage.error(error.response?.data?.message || '更新时段失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  const isCurrentlyActive = row.status === 'ACTIVE' || row.status === 'BOOKED'
  const action = isCurrentlyActive ? '停用' : '启用'

  try {
    await ElMessageBox.confirm(
      isCurrentlyActive
        ? `确定要停用此时段吗？停用后学生将无法预约此时段。`
        : `确定要启用此时段吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await counselorAPI.toggleTimeSlotStatus(row.id)

    // 立即更新本地状态
    row.status = isCurrentlyActive ? 'INACTIVE' : 'ACTIVE'

    ElMessage.success(`${action}成功`)

    // 后台刷新数据
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.response?.data?.message || `${action}失败`)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.time-slot-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.main-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
  color: #409eff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.slot-badge {
  margin-left: 4px;
}

.table-wrapper {
  margin-top: 8px;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-icon {
  color: #909399;
  font-size: 14px;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-tag {
  background: #f0f5ff;
  color: #409eff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: 0.5px;
}

.time-separator-text {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

.max-appointments {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.booking-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 0 20px;
}

.booking-progress {
  width: 100%;
}

.booking-text {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.status-tag {
  min-width: 60px;
  text-align: center;
}

.action-btn {
  min-width: 70px;
}

.add-slot-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 24px;
}

.add-slot-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

.edit-slot-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 24px;
}

.edit-slot-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

.slot-form {
  padding: 20px 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

.time-ranges-container {
  margin: 16px 0;
}

.time-range-item {
  background: #fafafa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.time-range-item:hover {
  border-color: #c0c4cc;
  background: #f5f7fa;
}

.time-range-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.range-index {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.time-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.add-range-btn {
  width: 100%;
  margin-top: 16px;
  border-style: dashed;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table .el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table .el-table__row:hover) {
  background-color: #f5f7fa !important;
}

:deep(.el-tag) {
  font-weight: 500;
  border: none;
}

:deep(.el-progress__bar) {
  border-radius: 3px;
}

:deep(.el-divider__text) {
  background-color: #fff;
  color: #909399;
  font-size: 13px;
}
</style>