<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">心灵广场</h2>
      <p class="page-subtitle">浏览心理健康文章和学院活动</p>
    </div>

    <el-card class="content-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="article-tabs">
            <el-tab-pane label="全部" name="" />
            <el-tab-pane label="心理文章" name="ARTICLE" />
            <el-tab-pane label="学院活动" name="ACTIVITY" />
          </el-tabs>
          <el-button v-if="userStore.role === 'ADMIN'" type="primary" @click="handlePublish">
            发布
          </el-button>
        </div>
      </template>

      <div v-loading="loading" class="article-list">
        <el-card
          v-for="article in articles"
          :key="article.id"
          class="article-item"
          shadow="hover"
        >
          <div class="article-header">
            <h3 class="article-title">{{ article.title }}</h3>
            <el-tag :type="getTagType(article.type)" class="article-tag">
              {{ getTypeText(article.type) }}
            </el-tag>
          </div>

          <div class="article-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ article.author || '管理员' }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ article.createdAt }}
            </span>
          </div>

          <div class="article-content">
            {{ stripHtml(article.content).substring(0, 150) }}...
          </div>

          <div class="article-footer">
            <el-button type="primary" link @click="viewDetail(article)">
              查看详情 >
            </el-button>
            <div v-if="userStore.role === 'ADMIN'" class="article-actions">
              <el-button type="primary" link @click="handleEdit(article)">
                编辑
              </el-button>
              <el-button type="danger" link @click="handleDelete(article)">
                删除
              </el-button>
            </div>
          </div>
        </el-card>

        <el-empty v-if="articles.length === 0" description="暂无文章" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="文章详情" width="800px">
      <div class="detail-content">
        <h2 class="detail-title">{{ currentArticle.title }}</h2>
        <div class="detail-meta">
          <span>
            <el-icon><User /></el-icon>
            {{ currentArticle.author || '管理员' }}
          </span>
          <span>
            <el-icon><Clock /></el-icon>
            {{ currentArticle.createdAt }}
          </span>
          <el-tag :type="getTagType(currentArticle.type)">
            {{ getTypeText(currentArticle.type) }}
          </el-tag>
        </div>
        <div class="detail-body" v-html="currentArticle.content"></div>
      </div>
    </el-dialog>

    <el-dialog v-model="publishVisible" title="发布文章" width="700px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="publishForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="publishForm.type" placeholder="请选择类型">
            <el-option label="心理文章" value="ARTICLE" />
            <el-option label="学院活动" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布学院">
          <el-input v-model="publishForm.college" placeholder="请输入发布学院（可选）" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Clock } from '@element-plus/icons-vue'
import { articleAPI } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const articles = ref([])
const activeTab = ref('')
const detailVisible = ref(false)
const publishVisible = ref(false)
const currentArticle = ref({})

const publishForm = ref({
  title: '',
  type: 'ARTICLE',
  college: '',
  content: ''
})

const getTypeText = (type) => {
  const map = {
    'ARTICLE': '心理文章',
    'NOTICE': '通知',
    'ACTIVITY': '学院活动'
  }
  return map[type] || type
}

const getTagType = (type) => {
  const map = {
    'ARTICLE': '',
    'NOTICE': 'warning',
    'ACTIVITY': 'success'
  }
  return map[type] || ''
}

const stripHtml = (html) => {
  const div = document.createElement('div')
  div.innerHTML = html
  return div.textContent || div.innerText || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await articleAPI.getArticles(activeTab.value)
    articles.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  loadData()
}

const viewDetail = (row) => {
  currentArticle.value = row
  detailVisible.value = true
}

const handlePublish = () => {
  publishForm.value = {
    title: '',
    type: 'ARTICLE',
    college: '',
    content: ''
  }
  publishVisible.value = true
}

const confirmPublish = async () => {
  if (!publishForm.value.title || !publishForm.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  try {
    await articleAPI.createArticle(publishForm.value)
    ElMessage.success('发布成功')
    publishVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await articleAPI.deleteArticle(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.content-card {
  background: white;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-tabs {
  flex: 1;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-item {
  transition: all 0.3s;
}

.article-item:hover {
  transform: translateY(-2px);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
}

.article-tag {
  margin-left: 12px;
  flex-shrink: 0;
}

.article-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 16px;
  font-size: 14px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #EBEEF5;
}

.article-actions {
  display: flex;
  gap: 12px;
}

.detail-content {
  padding: 20px;
}

.detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.detail-meta {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
  color: #909399;
  font-size: 14px;
}

.detail-body {
  line-height: 2;
  color: #303133;
  font-size: 15px;
}

.detail-body :deep(p) {
  margin-bottom: 16px;
}

.detail-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}
</style>
