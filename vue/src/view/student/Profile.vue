<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button
            v-if="!isEditing"
            type="primary"
            @click="startEdit"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <div v-else>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="姓名">
          <el-input v-model="form.name" disabled />
        </el-form-item>

        <el-form-item label="学号">
          <el-input v-model="form.studentNo" disabled />
        </el-form-item>

        <el-form-item label="学院">
          <el-input v-model="form.college" disabled />
        </el-form-item>

        <el-form-item label="班级">
          <el-input v-model="form.className" disabled />
        </el-form-item>

        <el-form-item label="年级">
          <el-input v-model="form.grade" disabled />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" :disabled="!isEditing" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" :disabled="!isEditing" />
        </el-form-item>

        <el-form-item label="密码">
          <div class="password-field">
            <el-input
              v-model="passwordDisplay"
              type="password"
              disabled
              placeholder="••••••••"
            />
            <el-button
              v-if="!isEditing"
              type="primary"
              link
              @click="showPasswordDialog = true"
            >
              修改密码
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="showPasswordDialog" title="修改密码" width="500px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码" required>
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordChange" :loading="passwordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const isEditing = ref(false)
const showPasswordDialog = ref(false)
const passwordLoading = ref(false)
const passwordDisplay = ref('••••••••')

const form = reactive({
  id: null,
  username: '',
  name: '',
  studentNo: '',
  college: '',
  className: '',
  grade: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  loadData()
}

const loadData = async () => {
  try {
    const res = await studentAPI.getInfo()
    Object.assign(form, res.data)
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!form.phone && !form.email) {
    ElMessage.warning('请至少填写手机号或邮箱')
    return
  }

  loading.value = true
  try {
    await studentAPI.updateInfo({
      id: form.id,
      phone: form.phone,
      email: form.email
    })
    ElMessage.success('保存成功')
    isEditing.value = false
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePasswordChange = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }

  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  passwordLoading.value = true
  try {
    await studentAPI.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error(error)
  } finally {
    passwordLoading.value = false
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

.password-field {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.password-field .el-input {
  flex: 1;
}
</style>
