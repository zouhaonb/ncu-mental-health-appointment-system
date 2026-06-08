<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 所有重点关注学生 -->
      <el-tab-pane label="所有重点关注学生" name="ALL">
        <el-table :data="students" v-loading="loading" stripe>
          <el-table-column label="姓名">
            <template #default="{ row }">
              {{ row.user?.name || '未知' }}
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
          <el-table-column prop="notes" label="备注" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="添加时间" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleRemove(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="students.length === 0" description="暂无重点关注学生" />
      </el-tab-pane>

      <!-- 待转介学生 -->
      <el-tab-pane label="待转介学生" name="PENDING_REFERRAL">
        <div class="tab-header">
          <el-badge :value="pendingReferralStudents.length" :max="99" class="badge">
            <span class="tab-title">待转介学生</span>
          </el-badge>
          <el-tag type="info">这些学生既没有预约过咨询师，也没有咨询记录</el-tag>
        </div>
        <el-table :data="pendingReferralStudents" v-loading="loading" stripe>
          <el-table-column label="姓名">
            <template #default="{ row }">
              {{ row.user?.name || '未知' }}
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
          <el-table-column prop="notes" label="备注" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="添加时间" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="warning" size="small" @click="showReferralDialog(row)">转介建议</el-button>
              <el-button type="danger" size="small" @click="handleRemove(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="pendingReferralStudents.length === 0" description="暂无待转介学生" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" title="添加重点关注学生" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择学生">
          <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option
              v-for="student in availableStudents"
              :key="student.id"
              :label="`${student.name} (${student.studentNo})`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input v-model="form.notes" type="textarea" :rows="4" placeholder="请说明关注原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 转介建议对话框 -->
    <el-dialog v-model="referralDialogVisible" title="发起转介建议" width="600px">
      <el-form :model="referralForm" label-width="120px">
        <el-form-item label="学生">
          <el-input :value="currentStudent?.user?.name" disabled />
        </el-form-item>

        <el-form-item label="指派咨询师">
          <el-select v-model="referralForm.assignedCounselorId" placeholder="请选择咨询师（可选）" style="width: 100%" clearable>
            <el-option
              v-for="counselor in counselors"
              :key="counselor.id"
              :label="counselor.name"
              :value="counselor.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="转介原因" required>
          <el-input v-model="referralForm.reason" type="textarea" :rows="4" placeholder="请详细说明转介原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="referralDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReferralSubmit" :loading="referralSubmitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { advisorAPI, counselorAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const students = ref([])
const availableStudents = ref([])
const dialogVisible = ref(false)
const referralDialogVisible = ref(false)
const referralSubmitting = ref(false)
const counselors = ref([])
const currentStudent = ref(null)
const activeTab = ref('ALL')

const form = reactive({
  studentId: null,
  notes: ''
})

const referralForm = reactive({
  studentId: null,
  assignedCounselorId: null,
  reason: ''
})

// 筛选出既没有预约记录也没有咨询记录的学生
const pendingReferralStudents = computed(() => {
  return students.value.filter(student => !student.hasAppointments && !student.hasConsultation)
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await advisorAPI.getKeyConcernStudents(userStore.userId)
    students.value = res.data

    // 为每个学生检查是否有预约记录和咨询记录
    for (let student of students.value) {
      const studentId = student.student?.id || student.concern?.studentId

      // 检查预约记录
      const hasAppointments = await checkStudentHasAppointments(studentId)
      student.hasAppointments = hasAppointments

      // 检查咨询记录
      const hasConsultation = await checkStudentHasConsultation(studentId)
      student.hasConsultation = hasConsultation
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 检查学生是否有预约记录
const checkStudentHasAppointments = async (studentId) => {
  try {
    const res = await advisorAPI.getStudentAppointments(studentId)
    return res.data && res.data.length > 0
  } catch (error) {
    console.error('检查学生预约记录失败:', error)
    return false
  }
}

// 检查学生是否有咨询师评估记录（完成过的咨询）
const checkStudentHasConsultation = async (studentId) => {
  try {
    // 查询该学生的咨询记录
    const res = await advisorAPI.getStudentConsultations(studentId)
    return res.data && res.data.length > 0
  } catch (error) {
    console.error('检查学生咨询记录失败:', error)
    return false
  }
}

const loadAvailableStudents = async () => {
  // TODO: 从后端获取可添加的学生列表
  availableStudents.value = []
}

const loadCounselors = async () => {
  try {
    const res = await counselorAPI.getList()
    counselors.value = res.data || []
  } catch (error) {
    console.error('加载咨询师列表失败:', error)
    counselors.value = []
  }
}

const showAddDialog = () => {
  form.studentId = null
  form.notes = ''
  dialogVisible.value = true
  loadAvailableStudents()
}

const handleSubmit = async () => {
  if (!form.studentId) {
    ElMessage.warning('请选择学生')
    return
  }

  submitting.value = true
  try {
    // 先获取辅导员信息得到 advisorId
    const advisorInfo = await advisorAPI.getInfo()
    const data = {
      advisorId: advisorInfo.data.id,
      studentId: form.studentId,
      notes: form.notes
    }

    await advisorAPI.addKeyConcernStudent(data)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleRemove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要移除该学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 获取重点关注记录的ID
    const concernId = row.id || row.concern?.id
    if (!concernId) {
      ElMessage.error('无法获取记录ID')
      return
    }

    await advisorAPI.removeKeyConcernStudent(concernId)
    ElMessage.success('移除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error('移除失败')
    }
  }
}

const showReferralDialog = (row) => {
  currentStudent.value = row
  referralForm.studentId = row.student?.id || row.concern?.studentId
  referralForm.assignedCounselorId = null
  referralForm.reason = ''
  referralDialogVisible.value = true
  loadCounselors()
}

const handleReferralSubmit = async () => {
  if (!referralForm.studentId || !referralForm.reason) {
    ElMessage.warning('请填写转介原因')
    return
  }

  referralSubmitting.value = true
  try {
    // 先获取辅导员信息得到 advisorId
    const advisorInfo = await advisorAPI.getInfo()
    const data = {
      advisorId: advisorInfo.data.id,
      studentId: referralForm.studentId,
      assignedCounselorId: referralForm.assignedCounselorId,
      reason: referralForm.reason
    }

    await advisorAPI.createReferralSuggestion(data)
    ElMessage.success('转介建议已提交')
    referralDialogVisible.value = false
  } catch (error) {
    console.error(error)
    ElMessage.error('提交失败')
  } finally {
    referralSubmitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
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
</style>
