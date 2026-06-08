<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>我的预约</span>
      </template>

      <el-table :data="appointments" v-loading="loading">
        <el-table-column label="咨询师">
          <template #default="{ row }">
            {{ row.counselor?.name || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="预约时间">
          <template #default="{ row }">
            {{ row.timeSlot?.date }} {{ row.timeSlot?.startTime }}
          </template>
        </el-table-column>
        <el-table-column prop="appointment.reason" label="预约原因" />
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
              v-if="row.appointment.status === 'PENDING' || row.appointment.status === 'CONFIRMED'"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="cancelDialogVisible" title="取消预约" width="500px">
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCancel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const appointments = ref([])
const cancelDialogVisible = ref(false)
const currentAppointment = ref(null)
const studentId = ref(null)

const cancelForm = reactive({
  cancelReason: ''
})

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

    console.log('开始获取预约数据, studentId:', studentId.value)
    const res = await studentAPI.getMyAppointments(studentId.value)
    console.log('预约数据响应:', res)

    appointments.value = res.data || []
    console.log('预约数据:', appointments.value)
  } catch (error) {
    console.error('加载预约数据失败:', error)
    ElMessage.error('加载预约数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleCancel = (row) => {
  currentAppointment.value = row
  cancelForm.cancelReason = ''
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelForm.cancelReason) {
    ElMessage.warning('请填写取消原因')
    return
  }

  try {
    await ElMessageBox.confirm('确定要取消预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await studentAPI.cancelAppointment(currentAppointment.value.appointment.id, cancelForm)
    ElMessage.success('取消成功')
    cancelDialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
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
</style>
