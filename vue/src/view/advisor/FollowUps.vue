<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>回访任务</span>
          <el-button type="primary" @click="showAddDialog">创建任务</el-button>
        </div>
      </template>

      <el-table :data="tasks" v-loading="loading">
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
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.task.status === 'COMPLETED' ? 'success' : 'warning'">
              {{ row.task.status === 'COMPLETED' ? '已完成' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feedback" label="回访反馈" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.task.feedback || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              v-if="row.task.status !== 'COMPLETED'"
              type="primary"
              size="small"
              @click="showCompleteDialog(row)"
            >
              完成回访
            </el-button>
            <el-button
              v-else
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

    <el-dialog v-model="addDialogVisible" title="创建回访任务" width="600px">
      <el-form :model="addForm" label-width="120px">
        <el-form-item label="选择学生">
          <el-select v-model="addForm.studentId" placeholder="请选择学生" style="width: 100%">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="`${student.name} (${student.studentNo})`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTask" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成回访" width="600px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="回访反馈">
          <el-input v-model="completeForm.feedback" type="textarea" :rows="4" placeholder="请填写回访情况" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompleteTask" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { advisorAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const tasks = ref([])
const students = ref([])
const addDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const currentTask = ref(null)

const addForm = reactive({
  studentId: null
})

const completeForm = reactive({
  feedback: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await advisorAPI.getFollowUpTasks(userStore.userId)
    tasks.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadStudents = async () => {
  // TODO: 从后端获取学生列表
  students.value = []
}

const showAddDialog = () => {
  addForm.studentId = null
  addDialogVisible.value = true
  loadStudents()
}

const handleCreateTask = async () => {
  if (!addForm.studentId) {
    ElMessage.warning('请选择学生')
    return
  }

  submitting.value = true
  try {
    const data = {
      advisorId: userStore.userId,
      studentId: addForm.studentId
    }

    await advisorAPI.createFollowUpTask(data)
    ElMessage.success('任务已创建')
    addDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const showCompleteDialog = (row) => {
  currentTask.value = row
  completeForm.feedback = ''
  completeDialogVisible.value = true
}

const handleCompleteTask = async () => {
  if (!completeForm.feedback) {
    ElMessage.warning('请填写回访反馈')
    return
  }

  submitting.value = true
  try {
    await advisorAPI.completeFollowUpTask(currentTask.value.task.id, completeForm.feedback)
    ElMessage.success('任务已完成')
    completeDialogVisible.value = false
    loadData()
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
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
