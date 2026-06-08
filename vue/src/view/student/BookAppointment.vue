<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>预约咨询</span>
      </template>

      <el-alert :title="`咨询师: ${counselorName}`" type="info" :closable="false" style="margin-bottom: 20px" />

      <h3>选择可预约时段</h3>

      <el-table :data="availableSlots" v-loading="loading" @row-click="selectSlot" highlight-current-row>
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column label="剩余名额">
          <template #default="{ row }">
            {{ row.maxAppointments - row.bookedCount }}
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="预约原因">
            <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请简要描述您想咨询的问题" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="!selectedSlot">
              提交预约
            </el-button>
            <el-button @click="$router.back()">返回</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const availableSlots = ref([])
const selectedSlot = ref(null)
const counselorName = ref('')

const form = reactive({
  reason: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const slotsRes = await studentAPI.getAvailableSlots(route.params.counselorId)
    availableSlots.value = slotsRes.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const selectSlot = (row) => {
  selectedSlot.value = row
}

const handleSubmit = async () => {
  if (!form.reason) {
    ElMessage.warning('请填写预约原因')
    return
  }

  if (!selectedSlot.value) {
    ElMessage.warning('请选择预约时段')
    return
  }

  submitting.value = true
  try {
    const data = {
      studentId: userStore.userId,
      counselorId: route.params.counselorId,
      timeSlotId: selectedSlot.value.id,
      reason: form.reason
    }

    await studentAPI.createAppointment(data)
    ElMessage.success('预约成功，等待咨询师确认')
    router.push('/student/my-appointments')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
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

h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
