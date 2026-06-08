<template>
  <div class="page-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card-v2">
          <div class="stat-icon-wrapper">
            <el-icon :size="28" class="stat-icon"><User /></el-icon>
          </div>
          <div class="stat-content-wrapper">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="stat-trend">
            <span class="trend-text">活跃用户</span>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card-v2">
          <div class="stat-icon-wrapper pink">
            <el-icon :size="28" class="stat-icon"><UserFilled /></el-icon>
          </div>
          <div class="stat-content-wrapper">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">学生数</div>
          </div>
          <div class="stat-trend">
            <span class="trend-text">在读学生</span>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card-v2">
          <div class="stat-icon-wrapper blue">
            <el-icon :size="28" class="stat-icon"><Calendar /></el-icon>
          </div>
          <div class="stat-content-wrapper">
            <div class="stat-value">{{ stats.totalAppointments }}</div>
            <div class="stat-label">预约总数</div>
          </div>
          <div class="stat-trend">
            <span class="trend-text">本月预约</span>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card-v2">
          <div class="stat-icon-wrapper orange">
            <el-icon :size="28" class="stat-icon"><Warning /></el-icon>
          </div>
          <div class="stat-content-wrapper">
            <div class="stat-value">{{ stats.totalAlerts }}</div>
            <div class="stat-label">预警总数</div>
          </div>
          <div class="stat-trend">
            <span class="trend-text">待处理</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2 class="page-title">用户管理</h2>
            <p class="page-desc">管理系统中所有用户账号</p>
          </div>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterRole" placeholder="角色筛选" clearable @change="loadData" style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="学生" value="STUDENT" />
          <el-option label="咨询师" value="COUNSELOR" />
          <el-option label="辅导员" value="ADVISOR" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>

        <el-select v-model="filterCollege" placeholder="学院筛选" clearable @change="loadData" style="width: 180px; margin-left: 12px" v-loading="collegesLoading">
          <el-option label="全部" value="" />
          <el-option
            v-for="college in colleges"
            :key="college"
            :label="college"
            :value="college"
          />
        </el-select>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名/姓名"
          clearable
          style="width: 200px; margin-left: 12px"
          @clear="loadData"
          @keyup.enter="loadData"
        >
          <template #append>
            <el-button @click="loadData">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>

        <div style="flex: 1"></div>

        <el-button type="success" @click="importDialogVisible = true">
          <el-icon><Download /></el-icon>
          批量导入/导出
        </el-button>
      </div>

      <el-table :data="users" v-loading="loading" stripe style="margin-top: 20px">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" effect="light">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" min-width="150" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                type="primary"
                link
                size="small"
                @click="showEditDialog(row)"
                class="action-btn"
              >
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>

              <el-divider direction="vertical" class="action-divider" />

              <el-dropdown trigger="click" @command="(command) => handleMoreAction(command, row)">
                <el-button link size="small" class="action-btn more-btn">
                  <span>更多</span>
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu class="custom-dropdown">
                    <el-dropdown-item command="toggleStatus">
                      <el-icon><Switch /></el-icon>
                      <span>{{ row.status === 1 ? '禁用' : '启用' }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="resetPassword">
                      <el-icon><Refresh /></el-icon>
                      <span>重置密码</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="changeRole">
                      <el-icon><Edit /></el-icon>
                      <span>角色变更</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>
                      <span class="delete-text">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="isEdit ? '不修改请留空' : ''" />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%" :disabled="isEdit && form.role !== 'STUDENT'">
            <el-option label="学生" value="STUDENT" />
            <el-option label="咨询师" value="COUNSELOR" />
            <el-option label="辅导员" value="ADVISOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="学院">
          <el-input v-model="form.college" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="角色变更" width="400px">
      <el-form :model="roleForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="roleForm.username" disabled />
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag :type="getRoleTagType(roleForm.currentRole)" effect="light">
            {{ getRoleText(roleForm.currentRole) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新角色" required>
          <el-select v-model="roleForm.newRole" placeholder="请选择新角色" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="咨询师" value="COUNSELOR" />
            <el-option label="辅导员" value="ADVISOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmRoleChange" :loading="roleChanging">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入用户" width="600px">
      <el-alert
        title="导入说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>请按照以下格式准备 Excel 文件：</p>
        <ul style="margin: 10px 0; padding-left: 20px">
          <li>第一行为表头：用户名、密码、姓名、角色、学院、手机号、邮箱</li>
          <li>角色可选值：STUDENT、COUNSELOR、ADVISOR、ADMIN</li>
          <li>密码如留空则默认为 123456</li>
        </ul>
        <el-button type="primary" size="small" @click="downloadTemplate" style="margin-top: 10px">
          <el-icon><Download /></el-icon>
          下载模板
        </el-button>
      </el-alert>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">只能上传 xlsx/xls 文件</div>
        </template>
      </el-upload>

      <div v-if="importPreview.length > 0" style="margin-top: 20px">
        <h4>预览数据（前 5 条）</h4>
        <el-table :data="importPreview.slice(0, 5)" border size="small">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)" size="small">
                {{ getRoleText(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="college" label="学院" />
        </el-table>
        <p style="color: #909399; font-size: 13px; margin-top: 10px">
          共 {{ importPreview.length }} 条数据
        </p>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing" :disabled="importPreview.length === 0">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Download, Edit, ArrowDown, Switch, Refresh, Delete, UploadFilled, User, UserFilled, Calendar, Warning } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'
import * as XLSX from 'xlsx'

const loading = ref(false)
const submitting = ref(false)
const users = ref([])
const colleges = ref([])
const collegesLoading = ref(false)
const filterRole = ref('')
const filterCollege = ref('')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const stats = ref({
  totalUsers: 0,
  totalStudents: 0,
  totalAppointments: 0,
  totalAlerts: 0
})


// 角色变更相关
const roleDialogVisible = ref(false)
const roleChanging = ref(false)
const roleForm = reactive({
  userId: null,
  username: '',
  currentRole: '',
  newRole: ''
})

// 导入导出相关
const importDialogVisible = ref(false)
const importing = ref(false)
const uploadRef = ref(null)
const importFile = ref(null)
const importPreview = ref([])

const form = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  role: '',
  phone: '',
  email: '',
  college: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleText = (role) => {
  const texts = {
    'STUDENT': '学生',
    'COUNSELOR': '咨询师',
    'ADVISOR': '辅导员',
    'ADMIN': '管理员'
  }
  return texts[role] || role
}

const getRoleTagType = (role) => {
  const types = {
    'STUDENT': '',
    'COUNSELOR': 'success',
    'ADVISOR': 'warning',
    'ADMIN': 'danger'
  }
  return types[role] || ''
}
const loadStats = async () => {
  try {
    const res = await adminAPI.getSystemStatistics()
    stats.value = res.data
  } catch (error) {
    console.error(error)
  }
}
const loadColleges = async () => {
  collegesLoading.value = true
  try {
    const res = await adminAPI.getCollegeList()
    colleges.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    collegesLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getUserList(filterRole.value)
    let filteredUsers = res.data

    if (filterCollege.value) {
      filteredUsers = filteredUsers.filter(user => user.college === filterCollege.value)
    }

    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filteredUsers = filteredUsers.filter(user =>
        user.username.toLowerCase().includes(keyword) ||
        user.name.toLowerCase().includes(keyword)
      )
    }

    users.value = filteredUsers
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const downloadTemplate = () => {
  const templateData = [
    ['用户名', '密码', '姓名', '角色', '学院', '手机号', '邮箱'],
    ['student11', '123456', '张三', 'STUDENT', '信息工程学院', '13800000001', 'zhangsan@ncu.edu.cn'],
    ['counselor3', '123456', '李四', 'COUNSELOR', '', '13800000002', 'lisi@ncu.edu.cn'],
    ['advisor2', '123456', '王五', 'ADVISOR', '理学院', '13800000003', 'wangwu@ncu.edu.cn']
  ]

  const ws = XLSX.utils.aoa_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '用户导入模板')

  ws['!cols'] = [
    { wch: 15 },
    { wch: 15 },
    { wch: 12 },
    { wch: 12 },
    { wch: 18 },
    { wch: 15 },
    { wch: 25 }
  ]

  XLSX.writeFile(wb, '用户导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

const handleExport = async () => {
  if (users.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  try {
    const exportData = users.value.map(user => ({
      '用户名': user.username,
      '姓名': user.name,
      '角色': user.role,
      '学院': user.college || '',
      '手机号': user.phone || '',
      '邮箱': user.email || '',
      '状态': user.status === 1 ? '启用' : '禁用'
    }))

    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '用户列表')

    ws['!cols'] = [
      { wch: 15 },
      { wch: 12 },
      { wch: 12 },
      { wch: 18 },
      { wch: 15 },
      { wch: 25 },
      { wch: 10 }
    ]

    const fileName = `用户列表_${new Date().getTime()}.xlsx`
    XLSX.writeFile(wb, fileName)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

const handleFileChange = (file) => {
  importFile.value = file.raw
  const reader = new FileReader()

  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[firstSheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet)

      importPreview.value = jsonData.map(row => ({
        username: row['用户名'] || '',
        password: row['密码'] || '123456',
        name: row['姓名'] || '',
        role: row['角色'] || 'STUDENT',
        college: row['学院'] || '',
        phone: row['手机号'] || '',
        email: row['邮箱'] || ''
      }))
    } catch (error) {
      console.error(error)
      ElMessage.error('文件解析失败，请检查文件格式')
      importPreview.value = []
    }
  }

  reader.readAsArrayBuffer(file.raw)
}

const handleImport = async () => {
  if (importPreview.value.length === 0) {
    ElMessage.warning('没有可导入的数据')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要导入 ${importPreview.value.length} 条用户数据吗？`,
      '导入确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    importing.value = true
    let successCount = 0
    let failCount = 0

    for (const user of importPreview.value) {
      try {
        await adminAPI.createUser(user)
        successCount++
      } catch (error) {
        console.error(`导入用户 ${user.username} 失败:`, error)
        failCount++
      }
    }

    ElMessage.success(`导入完成！成功 ${successCount} 条，失败 ${failCount} 条`)
    importDialogVisible.value = false
    importPreview.value = []
    importFile.value = null
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    importing.value = false
  }
}

const handleMoreAction = async (command, row) => {
  switch (command) {
    case 'toggleStatus':
      await handleToggleStatus(row)
      break
    case 'resetPassword':
      await handleResetPassword(row)
      break
    case 'changeRole':
      await handleChangeRole(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${row.username}" 的密码吗？`, '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('密码已重置为默认密码')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleChangeRole = async (row) => {
  roleForm.userId = row.id
  roleForm.username = row.username
  roleForm.currentRole = row.role
  roleForm.newRole = ''
  roleDialogVisible.value = true
}

const handleConfirmRoleChange = async () => {
  if (!roleForm.newRole) {
    ElMessage.warning('请选择新角色')
    return
  }

  if (roleForm.newRole === roleForm.currentRole) {
    ElMessage.warning('新角色与当前角色相同')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将 "${roleForm.username}" 的角色从 "${getRoleText(roleForm.currentRole)}" 变更为 "${getRoleText(roleForm.newRole)}" 吗？`,
      '角色变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    roleChanging.value = true
    const user = users.value.find(u => u.id === roleForm.userId)
    if (user) {
      user.role = roleForm.newRole
      await adminAPI.updateUser(user)
      ElMessage.success('角色变更成功')
      roleDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    roleChanging.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    name: '',
    role: '',
    phone: '',
    email: '',
    college: ''
  })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  form.password = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await adminAPI.updateUser(form)
          ElMessage.success('更新成功')
        } else {
          await adminAPI.createUser(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要${row.status === 1 ? '禁用' : '启用'}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminAPI.toggleUserStatus(row.id)
    ElMessage.success('操作成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminAPI.deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadStats()
  loadColleges()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card-v2 {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8ecf1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card-v2::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card-v2:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #d0d5dd;
}

.stat-card-v2:hover::before {
  opacity: 1;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

.stat-icon-wrapper.pink {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.25);
}

.stat-icon-wrapper.blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.25);
}

.stat-icon-wrapper.orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  box-shadow: 0 4px 12px rgba(250, 112, 154, 0.25);
}

.stat-icon {
  color: #ffffff;
}

.stat-content-wrapper {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
  margin-bottom: 6px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  font-weight: 500;
}

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  color: #8c8c8c;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.main-card {
  border-radius: 12px;
  border: 1px solid #e8ecf1;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 6px 0;
  letter-spacing: -0.3px;
}

.page-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
  font-weight: 400;
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px 0;
  border-bottom: 1px solid #e8ecf1;
}

.action-link {
  padding: 0 4px;
  transition: all 0.2s ease;
}

.action-link:hover {
  color: #409eff;
  text-decoration: none;
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.action-btn {
  padding: 0 !important;
  height: auto !important;
  min-height: auto !important;
  line-height: 1.5 !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  color: #409eff !important;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  transition: all 0.2s ease !important;
}

.action-btn:hover {
  color: #66b1ff !important;
  background: transparent !important;
  border: none !important;
}

.action-btn:active {
  color: #3a8ee6 !important;
}

.action-btn .el-icon {
  margin-right: 4px;
  font-size: 14px;
  vertical-align: middle;
}

.action-btn span {
  vertical-align: middle;
}

.more-btn {
  color: #909399 !important;
}

.more-btn:hover {
  color: #409eff !important;
}

.action-divider {
  height: 16px !important;
  margin: 0 !important;
  background-color: #dcdfe6 !important;
}

.custom-dropdown {
  border-radius: 8px !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12) !important;
  border: 1px solid #e8ecf1 !important;
  padding: 6px !important;
  min-width: 140px !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item) {
  border-radius: 6px !important;
  padding: 10px 16px !important;
  font-size: 14px !important;
  display: flex !important;
  align-items: center !important;
  gap: 10px !important;
  transition: all 0.2s ease !important;
  color: #606266 !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item:hover) {
  background: #ecf5ff !important;
  color: #409eff !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item .el-icon) {
  font-size: 16px !important;
  color: #909399 !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item:hover .el-icon) {
  color: #409eff !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item--divided) {
  margin-top: 6px !important;
  border-top: 1px solid #e8ecf1 !important;
  padding-top: 6px !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item--divided .delete-text) {
  color: #f56c6c !important;
}

:deep(.custom-dropdown .el-dropdown-menu__item:hover .delete-text) {
  color: #f56c6c !important;
}

:deep(.el-dropdown) {
  margin-left: 0;
}

:deep(.el-space) {
  display: flex;
  align-items: center;
}

:deep(.el-button.is-link) {
  font-weight: 500;
}

:deep(.el-table) {
  font-size: 14px;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #fafbfc;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 14px 0;
}

:deep(.el-table td) {
  padding: 14px 0;
  color: #606266;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 24px;
  background: #fafbfc;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}
</style>
