<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约处理</span>
          <el-radio-group v-model="filterStatus" @change="loadData">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="PENDING">待处理</el-radio-button>
            <el-radio-button label="CONFIRMED">已确认</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="appointments" v-loading="loading">
        <el-table-column label="学生">
          <template #default="{ row }">
            {{ row.user?.name || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="学号">
          <template #default="{ row }">
            {{ row.student?.studentNo || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="预约时间">
          <template #default="{ row }">
            {{ row.timeSlot?.date }} {{ row.timeSlot?.startTime }}
          </template>
        </el-table-column>
        <el-table-column prop="appointment.reason" label="预约原因" show-overflow-tooltip />
        <el-table-column prop="appointment.status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.appointment.status)">
              {{ getStatusText(row.appointment.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.appointment.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.appointment.status === 'PENDING'"
              type="warning"
              size="small"
              @click="showRejectDialog(row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.appointment.status === 'CONFIRMED' && !row.hasConsultationRecord"
              type="primary"
              size="small"
              @click="goToConsultationRecord(row)"
            >
              填写记录
            </el-button>
            <el-button
              v-if="row.appointment.status === 'CONFIRMED' && row.hasConsultationRecord"
              type="success"
              size="small"
              @click="handleComplete(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.appointment.status === 'COMPLETED'"
              type="info"
              size="small"
              disabled
            >
              已完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="拒绝预约" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { counselorAPI } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const appointments = ref([])
const filterStatus = ref('')
const rejectDialogVisible = ref(false)
const currentAppointment = ref(null)
const counselorId = ref(null)

const rejectForm = reactive({ reason: '' })

const getStatusType = (status) => {
  const types = { 'PENDING': 'warning', 'CONFIRMED': 'success', 'COMPLETED': 'info', 'CANCELLED': 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 'PENDING': '待确认', 'CONFIRMED': '已确认', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    if (!counselorId.value) {
      // 先获取咨询师信息，得到正确的 counselorId
      console.log('开始获取咨询师信息...')
      const counselorInfo = await counselorAPI.getInfo()
      console.log('咨询师信息响应:', counselorInfo)

      if (!counselorInfo || !counselorInfo.data || !counselorInfo.data.id) {
        console.error('咨询师信息格式错误:', counselorInfo)
        ElMessage.error('获取咨询师信息失败')
        return
      }

      counselorId.value = counselorInfo.data.id
      console.log('获取到 counselorId:', counselorId.value)
    }

    console.log('开始获取预约数据, counselorId:', counselorId.value, 'status:', filterStatus.value)
    const res = await counselorAPI.getAppointments(counselorId.value, filterStatus.value)
    console.log('预约数据响应:', res)

    appointments.value = res.data || []
    console.log('预约数据:', appointments.value)
  } catch (error) {
    console.error('加载预约数据失败:', error)
    console.error('错误详情:', error.response || error.message || error)
    ElMessage.error('加载预约数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确定要确认该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await counselorAPI.confirmAppointment(row.appointment.id)
    ElMessage.success('确认成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const showRejectDialog = (row) => {
  currentAppointment.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  try {
    await counselorAPI.rejectAppointment(currentAppointment.value.appointment.id, rejectForm)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要标记为已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await counselorAPI.completeAppointment(row.appointment.id)
    ElMessage.success('已完成')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const goToConsultationRecord = (row) => {
  router.push({
    path: '/counselor/consultation-record',
    query: {
      tab: 'write',
      appointmentId: row.appointment.id
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
