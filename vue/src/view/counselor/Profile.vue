<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">个人信息</h2>
      <p class="page-subtitle">管理您的个人资料和联系方式</p>
    </div>

    <div class="content-wrapper">
      <div class="avatar-section">
        <div class="avatar-wrapper" @click="triggerUpload">
          <div class="avatar-placeholder" v-if="!form.avatar && !previewImage">
            <span>{{ form.name ? form.name.charAt(0) : '咨' }}</span>
          </div>
          <img
            v-else
            :src="previewImage || form.avatar"
            class="avatar-image"
          />
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </div>
        </div>
        <div class="avatar-info">
          <h3 class="counselor-name">{{ form.name || '咨询师' }}</h3>
          <p class="counselor-title">{{ form.title || '暂无职称' }}</p>
        </div>
      </div>

      <el-divider />

      <el-form :model="form" label-width="100px" class="profile-form">
        <div class="form-section">
          <h4 class="section-title">
            <el-icon><User /></el-icon>
            基本信息
          </h4>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="姓名">
                <el-input v-model="form.name" placeholder="请输入姓名" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职称">
                <el-input v-model="form.title" placeholder="请输入职称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="擅长领域">
            <el-input
              v-model="form.specialization"
              placeholder="多个领域用逗号分隔，如：情绪管理,学业压力,人际关系"
            />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请简要介绍您的专业背景和咨询经验"
            />
          </el-form-item>
        </div>

        <el-divider />

        <div class="form-section">
          <h4 class="section-title">
            <el-icon><Phone /></el-icon>
            联系方式
          </h4>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="loading"
            :disabled="!isFormModified"
            class="save-btn"
          >
            <el-icon><Check /></el-icon>
            保存修改
          </el-button>
        </div>
      </el-form>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      @change="handleFileSelect"
      style="display: none;"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Phone, Camera, Check } from '@element-plus/icons-vue'
import { counselorAPI } from '@/api'

const loading = ref(false)
const uploadRef = ref(null)
const fileInputRef = ref(null)
const previewImage = ref('')
const originalForm = reactive({})

const form = reactive({
  name: '',
  title: '',
  specialization: '',
  description: '',
  avatar: '',
  phone: '',
  email: ''
})

const isFormModified = computed(() => {
  return form.title !== originalForm.title ||
         form.specialization !== originalForm.specialization ||
         form.description !== originalForm.description ||
         form.phone !== originalForm.phone ||
         form.email !== originalForm.email ||
         form.avatar !== originalForm.avatar
})

const triggerUpload = () => {
  fileInputRef.value.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过2MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(file)

  uploadAvatar(file)
}

const uploadAvatar = async (file) => {
  try {
    loading.value = true
    const formData = new FormData()
    formData.append('file', file)

    const res = await counselorAPI.uploadAvatar(formData)
    form.avatar = res.data.url
    previewImage.value = ''
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('头像上传失败')
  } finally {
    loading.value = false
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const loadData = async () => {
  try {
    const res = await counselorAPI.getInfo()
    Object.assign(form, res.data)
    Object.assign(originalForm, res.data)
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!isFormModified.value) {
    ElMessage.info('没有需要保存的修改')
    return
  }

  loading.value = true
  try {
    await counselorAPI.updateInfo(form)
    ElMessage.success('保存成功')
    Object.assign(originalForm, form)
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  previewImage.value = ''
  Object.assign(form, originalForm)
  ElMessage.info('已重置')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 32px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.content-wrapper {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid #e5e7eb;
}

.avatar-wrapper:hover {
  border-color: #6366f1;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 600;
  color: #ffffff;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: #ffffff;
  font-size: 24px;
}

.avatar-overlay span {
  font-size: 12px;
  font-weight: 500;
}

.avatar-info {
  flex: 1;
}

.counselor-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.counselor-title {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.profile-form {
  margin-top: 24px;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.section-title .el-icon {
  font-size: 18px;
  color: #6366f1;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4b5563;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.save-btn {
  min-width: 120px;
}

:deep(.el-divider) {
  margin: 32px 0;
}
</style>
