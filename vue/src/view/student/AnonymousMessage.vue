<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>匿名留言</h2>
      <p class="page-description">
        向咨询师匿名留言，获得专业心理支持。您的身份信息将受到保护。
      </p>
    </div>

    <!-- 主卡片 -->
    <el-card class="main-card">
      <!-- Tabs 切换 -->
      <el-tabs v-model="activeTab" class="message-tabs">
        <!-- 提交留言 -->
        <el-tab-pane label="提交留言" name="submit">
          <!-- 隐私提示 -->
          <el-alert
            title="您的留言将以匿名方式发送给咨询师，咨询师无法看到您的真实身份信息。"
            type="info"
            :closable="false"
            show-icon
            class="privacy-alert"
          />

          <!-- 留言表单 -->
          <el-form :model="form" class="message-form">
            <el-form-item label="留言内容" required>
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="8"
                placeholder="请在此输入您想对咨询师说的话......"
                maxlength="1000"
                show-word-limit
                resize="vertical"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
                <el-icon><Promotion /></el-icon>
                提交留言
              </el-button>
              <el-button @click="handleClear" size="large">
                清空
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 查看留言 -->
        <el-tab-pane label="查看留言" name="view">
          <el-table
            :data="messages"
            v-loading="loading"
            border
            stripe
            empty-text="暂无留言记录"
          >
            <el-table-column prop="content" label="留言内容" min-width="300" show-overflow-tooltip />
            <el-table-column prop="reply" label="回复内容" min-width="300" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.reply || '暂无回复' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'REPLIED' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'REPLIED' ? '已回复' : '待回复' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="留言时间" width="180" align="center">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const messages = ref([])
const activeTab = ref('submit')

const form = reactive({
  content: ''
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return ''
  }
}

const loadData = async () => {
  loading.value = true
  try {
    if (!userStore.userId) {
      ElMessage.error('用户信息不存在，请重新登录')
      return
    }
    const res = await studentAPI.getMyAnonymousMessages(userStore.userId)
    messages.value = res.data || []
  } catch (error) {
    console.error('加载留言失败:', error)
    ElMessage.error('加载留言失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.content || !form.content.trim()) {
    ElMessage.warning('请填写留言内容')
    return
  }

  submitting.value = true
  try {
    // 获取学生信息，得到正确的 studentId
    const studentInfo = await studentAPI.getInfo()
    const correctStudentId = studentInfo.data.id

    const data = {
      studentId: correctStudentId,
      content: form.content.trim()
    }

    await studentAPI.sendAnonymousMessage(data)
    ElMessage.success('留言成功，咨询师将尽快回复您')
    form.content = ''
    // 切换到查看留言标签页
    activeTab.value = 'view'
    loadData()
  } catch (error) {
    console.error('提交留言失败:', error)
    ElMessage.error('提交留言失败')
  } finally {
    submitting.value = false
  }
}

const handleClear = () => {
  form.content = ''
  ElMessage.info('已清空')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.main-card {
  border-radius: 8px;
}

.message-tabs {
  margin-top: 10px;
}

.message-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.privacy-alert {
  margin-bottom: 20px;
  border-radius: 4px;
}

.message-form {
  max-width: 800px;
}

.message-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.message-form :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
}

.message-form :deep(.el-button) {
  margin-right: 10px;
}

:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}
</style>
