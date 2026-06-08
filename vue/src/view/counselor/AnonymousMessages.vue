<template>
  <div class="page-container">
    <div class="page-header">
      <h2>匿名留言</h2>
      <p class="page-description">查看和回复学生的匿名留言</p>
    </div>

    <div class="messages-list" v-loading="loading">
      <!-- 留言卡片 -->
      <el-card
        v-for="message in messages"
        :key="message.id"
        class="message-card"
        :class="{ 'replied': message.status === 'REPLIED', 'claimed': message.status === 'CLAIMED' }"
      >
        <!-- 留言头部：状态和时间 -->
        <div class="message-header">
          <el-tag
            :type="getStatusType(message.status)"
            size="small"
          >
            {{ getStatusText(message.status) }}
          </el-tag>
          <span class="message-time">{{ formatDateTime(message.createdAt) }}</span>
        </div>

        <!-- 留言内容 -->
        <div class="message-content">
          {{ message.content }}
        </div>

        <!-- 已回复的留言显示回复内容 -->
        <div v-if="message.status === 'REPLIED'" class="reply-section">
          <el-divider />
          <!-- 如果是本人回复 -->
          <div v-if="message.counselorId === currentCounselorId" class="my-reply">
            <div class="reply-label">我的回复：</div>
            <div class="reply-content">{{ message.reply }}</div>
          </div>
          <!-- 如果是其他咨询师回复 -->
          <div v-else class="other-reply">
            <el-icon><InfoFilled /></el-icon>
            <span>已被其他咨询师回复</span>
          </div>
        </div>

        <!-- 已被其他咨询师处理的提示 -->
        <div v-if="message.status === 'CLAIMED'" class="claimed-notice">
          <el-icon><InfoFilled /></el-icon>
          <span>已被其他咨询师处理中</span>
        </div>

        <!-- 操作按钮 -->
        <div class="message-actions">
          <el-button
            v-if="message.status === 'PENDING'"
            type="primary"
            size="small"
            @click="showReplyDialog(message)"
          >
            认领并回复
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty v-if="!loading && messages.length === 0" description="暂无留言" />
    </div>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复留言" width="600px">
      <el-form :model="replyForm" label-width="100px">
        <el-form-item label="留言内容">
          <div class="original-message">{{ replyForm.content }}</div>
        </el-form-item>

        <el-form-item label="回复内容" required>
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReply" :loading="submitting">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { counselorAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const messages = ref([])
const replyDialogVisible = ref(false)
const currentCounselorId = ref(null)

const replyForm = reactive({
  id: null,
  content: '',
  reply: ''
})

// 加载留言数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await counselorAPI.getAnonymousMessages()
    messages.value = res.data || []
  } catch (error) {
    console.error('加载留言失败:', error)
    ElMessage.error('加载留言失败')
  } finally {
    loading.value = false
  }
}

// 获取当前咨询师信息
const loadCounselorInfo = async () => {
  try {
    const res = await counselorAPI.getInfo()
    if (res && res.data && res.data.id) {
      currentCounselorId.value = res.data.id
      console.log('当前咨询师ID:', currentCounselorId.value)
    }
  } catch (error) {
    console.error('获取咨询师信息失败:', error)
  }
}

// 显示回复对话框
const showReplyDialog = (message) => {
  replyForm.id = message.id
  replyForm.content = message.content
  replyForm.reply = ''
  replyDialogVisible.value = true
}

// 确认回复
const confirmReply = async () => {
  if (!replyForm.reply || !replyForm.reply.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }

  submitting.value = true
  try {
    await counselorAPI.replyAnonymousMessage(replyForm.id, { reply: replyForm.reply })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败: ' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'REPLIED': 'success',
    'CLAIMED': 'info'
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待回复',
    'REPLIED': '已回复',
    'CLAIMED': '已被其他咨询师处理'
  }
  return textMap[status] || status
}

// 格式化时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
}

onMounted(() => {
  loadCounselorInfo()
  loadData()
})

</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.page-description {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.message-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.message-card.replied {
  background-color: #fafafa;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.message-time {
  font-size: 13px;
  color: #909399;
}

.message-content {
  font-size: 15px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 12px;
  white-space: pre-wrap;
}

.reply-section {
  margin-top: 16px;
  padding-top: 16px;
}

.my-reply {
  margin-top: 16px;
}

.reply-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 8px;
}

.reply-content {
  font-size: 14px;
  line-height: 1.6;
  color: #409EFF;
  background-color: #ecf5ff;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.other-reply {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
  font-size: 14px;
}

.claimed-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
  font-size: 14px;
}

.message-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.original-message {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}
</style>
