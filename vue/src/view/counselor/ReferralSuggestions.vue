<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>转介建议</span>
          <el-tag type="info">查看辅导员为您指派的转介建议</el-tag>
        </div>
      </template>

      <el-table :data="suggestions" v-loading="loading">
        <el-table-column label="学生姓名">
          <template #default="{ row }">
            {{ row.user?.name || row.student?.name || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="学号">
          <template #default="{ row }">
            {{ row.student?.studentNo || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="学院">
          <template #default="{ row }">
            {{ row.student?.college || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="班级">
          <template #default="{ row }">
            {{ row.student?.className || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="发起辅导员">
          <template #default="{ row }">
            {{ row.advisorUser?.name || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="转介原因" show-overflow-tooltip width="250" />
        <el-table-column label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.handleStatus)">
              {{ getStatusText(row.handleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.handleStatus === 'PENDING'"
              type="success"
              size="small"
              @click="showAcceptDialog(row)"
            >
              接受
            </el-button>
            <el-button
              v-if="row.handleStatus === 'PENDING'"
              type="danger"
              size="small"
              @click="handleReject(row.id)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.handleStatus !== 'PENDING'"
              type="info"
              size="small"
              disabled
            >
              {{ getStatusText(row.handleStatus) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="suggestions.length === 0" description="暂无转介建议" />
    </el-card>

    <!-- 接受转介并选择时间的对话框 -->
    <el-dialog v-model="acceptDialogVisible" title="接受转介建议" width="700px">
      <el-form :model="acceptForm" label-width="120px">
        <el-form-item label="学生">
          <el-input :value="`${currentSuggestion?.user?.name || ''} (${currentSuggestion?.student?.studentNo || ''})`" disabled />
        </el-form-item>
        <el-form-item label="转介原因">
          <el-input :value="currentSuggestion?.reason || ''" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="选择预约时间" required>
          <el-select
            v-model="acceptForm.timeSlotId"
            placeholder="请选择预约时间段"
            style="width: 100%"
            :loading="loadingSlots"
          >
            <el-option
              v-for="slot in availableSlots"
              :key="slot.id"
              :label="`${slot.date} ${slot.startTime}-${slot.endTime}`"
              :value="slot.id"
            />
          </el-select>
          <el-tag v-if="availableSlots.length === 0 && !loadingSlots" type="warning" style="margin-top: 10px">
            暂无可用时间段，请先设置可预约时间
          </el-tag>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="acceptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAccept" :loading="submitting" :disabled="!acceptForm.timeSlotId">
          确认接受
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { counselorAPI } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const loadingSlots = ref(false)
const submitting = ref(false)
const suggestions = ref([])
const availableSlots = ref([])
const acceptDialogVisible = ref(false)
const currentSuggestion = ref(null)
const counselorInfo = ref(null)

const acceptForm = ref({
  timeSlotId: null
})

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'ACCEPTED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'ACCEPTED': '已接受',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await counselorAPI.getReferralSuggestions()
    suggestions.value = res.data || []
  } catch (error) {
    console.error('加载转介建议失败:', error)
    ElMessage.error('加载转介建议失败')
  } finally {
    loading.value = false
  }
}

const loadAvailableSlots = async () => {
  loadingSlots.value = true
  try {
    // 先获取咨询师信息
    if (!counselorInfo.value) {
      const infoRes = await counselorAPI.getInfo()
      counselorInfo.value = infoRes.data
    }

    const res = await counselorAPI.getMyTimeSlots(counselorInfo.value.id)
    // 过滤出可用的时间段（ACTIVE状态且日期>=今天）
    const today = new Date().toISOString().split('T')[0]
    availableSlots.value = (res.data || []).filter(slot =>
      slot.status === 'ACTIVE' && slot.date >= today
    )
    console.log('可用时间段:', availableSlots.value)
  } catch (error) {
    console.error('加载时间段失败:', error)
    ElMessage.error('加载时间段失败：' + (error.response?.data?.message || error.message))
  } finally {
    loadingSlots.value = false
  }
}

const showAcceptDialog = async (row) => {
  currentSuggestion.value = row
  acceptForm.value.timeSlotId = null
  acceptDialogVisible.value = true

  // 加载可用的时间段
  await loadAvailableSlots()
}

const confirmAccept = async () => {
  if (!acceptForm.value.timeSlotId) {
    ElMessage.warning('请选择预约时间')
    return
  }

  submitting.value = true
  try {
    await counselorAPI.acceptReferralWithTimeSlot(currentSuggestion.value.id, acceptForm.value.timeSlotId)
    ElMessage.success('已接受转介建议，预约记录已创建')
    acceptDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定拒绝此转介建议吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await counselorAPI.handleReferralSuggestion(id, 'REJECT')
    ElMessage.success('已拒绝转介建议')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
