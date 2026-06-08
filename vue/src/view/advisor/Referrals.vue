<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>转介管理</span>
          <el-button type="primary" @click="showAddDialog">发起转介</el-button>
        </div>
      </template>

      <el-table :data="referrals" v-loading="loading">
        <el-table-column label="学生">
          <template #default="{ row }">
            {{ row.user?.name || row.student?.name || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="学号">
          <template #default="{ row }">
            {{ row.student?.studentNo || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="指派咨询师">
          <template #default="{ row }">
            {{ row.counselorUser?.name || row.counselor?.name || '未指派' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="转介原因" show-overflow-tooltip />
        <el-table-column prop="handleStatus" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.handleStatus)">
              {{ getStatusText(row.handleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.handleStatus === 'PENDING'"
              type="danger"
              size="small"
              @click="handleCancel(row)"
              plain
            >
              取消
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发起转介建议" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择学生">
          <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="`${student.name} (${student.studentNo})`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="指派咨询师">
          <el-select v-model="form.assignedCounselorId" placeholder="请选择咨询师" style="width: 100%" clearable>
            <el-option
              v-for="counselor in counselors"
              :key="counselor.id"
              :label="counselor.name"
              :value="counselor.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="转介原因">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请说明转介原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { advisorAPI, counselorAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const referrals = ref([])
const students = ref([])
const counselors = ref([])
const dialogVisible = ref(false)

const form = reactive({
  studentId: null,
  assignedCounselorId: null,
  reason: ''
})

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'ACCEPTED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'ACCEPTED': '已接受',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await advisorAPI.getReferralSuggestions(userStore.userId)
    referrals.value = res.data || []
  } catch (error) {
    console.error('加载转介列表失败:', error)
    ElMessage.error('加载转介列表失败')
  } finally {
    loading.value = false
  }
}

const loadStudents = async () => {
  try {
    const concernsRes = await advisorAPI.getKeyConcernStudents(userStore.userId)
    const concerns = concernsRes.data || []

    const pendingStudents = []
    for (let concern of concerns) {
      const studentId = concern.student?.id || concern.concern?.studentId
      const student = concern.student
      const user = concern.user

      try {
        const appointmentsRes = await advisorAPI.getStudentAppointments(studentId)
        const hasAppointments = appointmentsRes.data && appointmentsRes.data.length > 0

        const consultationsRes = await advisorAPI.getStudentConsultations(studentId)
        const hasConsultation = consultationsRes.data && consultationsRes.data.length > 0

        const referralsRes = await advisorAPI.getReferralSuggestions(userStore.userId)
        const referrals = referralsRes.data || []
        const hasPendingReferral = referrals.some(r =>
          r.studentId === studentId && r.handleStatus === 'PENDING'
        )

        if (!hasAppointments && !hasConsultation && !hasPendingReferral) {
          pendingStudents.push({
            id: studentId,
            name: user?.name || '未知',
            studentNo: student?.studentNo || '未知',
            college: student?.college || '未知',
            className: student?.className || '未知'
          })
        }
      } catch (error) {
        console.error(`检查学生${studentId}记录失败:`, error)
        pendingStudents.push({
          id: studentId,
          name: user?.name || '未知',
          studentNo: student?.studentNo || '未知',
          college: student?.college || '未知',
          className: student?.className || '未知'
        })
      }
    }

    students.value = pendingStudents
    console.log('需要转介的学生:', students.value)
  } catch (error) {
    console.error('加载需要转介的学生失败:', error)
    ElMessage.error('加载学生列表失败')
    students.value = []
  }
}

const loadCounselors = async () => {
  try {
    const res = await counselorAPI.getList()
    counselors.value = res.data || []
    console.log('咨询师列表:', counselors.value)
  } catch (error) {
    console.error('加载咨询师列表失败:', error)
    ElMessage.error('加载咨询师列表失败')
    counselors.value = []
  }
}

const showAddDialog = async () => {
  form.studentId = null
  form.assignedCounselorId = null
  form.reason = ''
  dialogVisible.value = true

  await Promise.all([
    loadStudents(),
    loadCounselors()
  ])
}

const handleSubmit = async () => {
  if (!form.studentId || !form.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    const advisorInfo = await advisorAPI.getInfo()
    const data = {
      advisorId: advisorInfo.data.id,
      ...form
    }

    await advisorAPI.createReferralSuggestion(data)
    ElMessage.success('转介建议已提交')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该转介建议吗？取消后咨询师将无法看到此建议。',
      '提示',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )

    await advisorAPI.cancelReferralSuggestion(row.id)
    ElMessage.success('已取消转介建议')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.no-action {
  color: #c0c4cc;
  font-size: 14px;
}
</style>
