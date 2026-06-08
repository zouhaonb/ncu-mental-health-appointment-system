<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>咨询师管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增咨询师
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" class="statistics-row">
        <el-col :span="4">
          <el-statistic title="总计" :value="statistics.total || 0" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="待审核" :value="statistics.pending || 0" value-style="color: #E6A23C" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="已通过" :value="statistics.approved || 0" value-style="color: #67C23A" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="已拒绝" :value="statistics.rejected || 0" value-style="color: #F56C6C" />
        </el-col>
        <el-col :span="4">
          <el-statistic title="已禁用" :value="statistics.disabled || 0" value-style="color: #909399" />
        </el-col>
      </el-row>

      <el-row :gutter="20" class="filter-row">
        <el-col :span="6">
          <el-select v-model="filterStatus" placeholder="按状态筛选" clearable @change="loadData">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="0" />
            <el-option label="已通过" value="1" />
            <el-option label="已拒绝" value="2" />
            <el-option label="已禁用" value="-1" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input v-model="filterKeyword" placeholder="搜索职称/擅长领域" clearable @clear="loadData" @keyup.enter="loadData">
            <template #append>
              <el-button @click="loadData">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
      </el-row>

      <el-table :data="counselors" v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="title" label="职称" width="180" />
        <el-table-column prop="specialization" label="擅长领域" width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.isActive)">
              {{ getStatusText(row.isActive) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleViewDetail(row)">
                详情
              </el-button>
              <template v-if="row.isActive === 0">
                <el-button type="success" size="small" @click="handleApprove(row.id)">
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">
                  拒绝
                </el-button>
              </template>
              <el-button
                v-else
                :type="row.isActive === -1 ? 'success' : 'warning'"
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.isActive === -1 ? '启用' : '禁用' }}
              </el-button>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="咨询师详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ currentCounselor.name }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ currentCounselor.title }}</el-descriptions-item>
        <el-descriptions-item label="擅长领域" :span="2">{{ currentCounselor.specialization }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentCounselor.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentCounselor.email }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentCounselor.college }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCounselor.isActive)">
            {{ getStatusText(currentCounselor.isActive) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ currentCounselor.description }}</el-descriptions-item>
        <el-descriptions-item v-if="currentCounselor.rejectReason" label="拒绝理由" :span="2">
          {{ currentCounselor.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑咨询师信息" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" disabled />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="editForm.specialization" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝理由">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejecting">确定拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createDialogVisible" title="新增咨询师" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="createForm.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="createForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="createForm.college" />
        </el-form-item>
        <el-form-item label="职称" required>
          <el-input v-model="createForm.title" />
        </el-form-item>
        <el-form-item label="擅长领域" required>
          <el-input v-model="createForm.specialization" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateCounselor" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'

const loading = ref(false)
const counselors = ref([])
const statistics = ref({})
const filterStatus = ref('')
const filterKeyword = ref('')

const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const createDialogVisible = ref(false)

const currentCounselor = ref({})
const saving = ref(false)
const rejecting = ref(false)
const creating = ref(false)

const editForm = reactive({
  id: null,
  name: '',
  title: '',
  specialization: '',
  description: ''
})

const rejectForm = reactive({
  id: null,
  reason: ''
})

const createForm = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  email: '',
  college: '',
  title: '',
  specialization: '',
  description: ''
})

const getStatusType = (status) => {
  const types = { 1: 'success', 0: 'warning', 2: 'danger', '-1': 'info' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 1: '已通过', 0: '待审核', 2: '已拒绝', '-1': '已禁用' }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const [counselorRes, statRes] = await Promise.all([
      adminAPI.getCounselorList({ status: filterStatus.value, keyword: filterKeyword.value }),
      adminAPI.getCounselorStatistics()
    ])
    counselors.value = counselorRes.data
    statistics.value = statRes.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row) => {
  currentCounselor.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要通过该咨询师的审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminAPI.approveCounselor(id)
    ElMessage.success('审核通过')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请填写拒绝理由')
    return
  }

  rejecting.value = true
  try {
    await adminAPI.rejectCounselor(rejectForm.id, { reason: rejectForm.reason })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    rejecting.value = false
  }
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.name = row.name
  editForm.title = row.title
  editForm.specialization = row.specialization
  editForm.description = row.description
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  saving.value = true
  try {
    await adminAPI.updateCounselor({
      id: editForm.id,
      title: editForm.title,
      specialization: editForm.specialization,
      description: editForm.description
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (row) => {
  const action = row.isActive === -1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该咨询师吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminAPI.toggleCounselorStatus(row.id)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCreate = () => {
  Object.assign(createForm, {
    username: '',
    password: '',
    name: '',
    phone: '',
    email: '',
    college: '',
    title: '',
    specialization: '',
    description: ''
  })
  createDialogVisible.value = true
}

const handleCreateCounselor = async () => {
  if (!createForm.username || !createForm.password || !createForm.name || !createForm.title || !createForm.specialization) {
    ElMessage.warning('请填写必填项')
    return
  }

  creating.value = true
  try {
    await adminAPI.createCounselor(createForm)
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    creating.value = false
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

.statistics-row {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.filter-row {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.el-button {
  margin: 0;
}
</style>
