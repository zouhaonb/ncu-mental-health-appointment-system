<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>心灵广场管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            发布内容
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="selectedRows.length > 0"
        :title="`已选择 ${selectedRows.length} 篇文章`"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        <template #default>
          <el-button type="danger" size="small" @click="handleBatchDelete">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </template>
      </el-alert>

      <el-table
        :data="articles"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="showDetailDialog(row)" :underline="false">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="college" label="发布学院" width="150" />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetailDialog(row)">
              预览
            </el-button>
            <el-button type="primary" size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑文章' : '发布文章'" width="800px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="类型" required>
          <el-radio-group v-model="form.type">
            <el-radio label="ARTICLE">文章</el-radio>
            <el-radio label="NOTICE">通知</el-radio>
            <el-radio label="ACTIVITY">活动</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="发布学院">
          <el-input v-model="form.college" placeholder="请输入发布学院，例如：信息工程学院" />
        </el-form-item>

        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="请输入文章内容" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="文章详情" width="900px" class="article-detail-dialog">
      <div class="article-header">
        <h1 class="article-title">{{ currentArticle.title }}</h1>
        <div class="article-meta">
          <el-tag :type="getTypeTag(currentArticle.type)" size="large">
            {{ getTypeText(currentArticle.type) }}
          </el-tag>
          <span v-if="currentArticle.college" class="meta-item">
            <el-icon><Location /></el-icon>
            {{ currentArticle.college }}
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon>
            {{ formatDate(currentArticle.createdAt) }}
          </span>
        </div>
      </div>

      <el-divider />

      <div class="article-content">
        <div v-html="formatContent(currentArticle.content)"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { adminAPI } from '@/api'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const articles = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const selectedRows = ref([])
const currentArticle = ref({})

const form = reactive({
  id: null,
  title: '',
  content: '',
  type: 'ARTICLE',
  college: '',
  authorId: null
})

const getTypeTag = (type) => {
  const tags = { ARTICLE: '', NOTICE: 'warning', ACTIVITY: 'success' }
  return tags[type] || ''
}

const getTypeText = (type) => {
  const texts = { ARTICLE: '文章', NOTICE: '通知', ACTIVITY: '活动' }
  return texts[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatContent = (content) => {
  if (!content) return ''
  // 将换行符转换为 HTML 换行标签
  return content.replace(/\n/g, '<br/>')
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getArticleList()
    articles.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    title: '',
    content: '',
    type: 'ARTICLE',
    college: '',
    authorId: userStore.userId
  })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const showDetailDialog = (row) => {
  currentArticle.value = row
  detailDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await adminAPI.updateArticle(form)
      ElMessage.success('更新成功')
    } else {
      await adminAPI.createArticle(form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminAPI.deleteArticle(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 篇文章吗？此操作不可恢复！`,
      '批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const deletePromises = selectedRows.value.map(row => adminAPI.deleteArticle(row.id))
    await Promise.all(deletePromises)

    ElMessage.success(`成功删除 ${selectedRows.value.length} 篇文章`)
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    loading.value = false
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

/* 文章详情弹窗样式 */
:deep(.article-detail-dialog) {
  .el-dialog__header {
    padding-bottom: 0;
  }

  .el-dialog__body {
    padding: 20px 30px 30px;
  }
}

.article-header {
  margin-bottom: 20px;
}

.article-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 14px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item .el-icon {
  font-size: 16px;
}

.article-content {
  font-size: 16px;
  line-height: 2;
  color: #303133;
  min-height: 200px;
}

.article-content :deep(p) {
  margin: 16px 0;
  text-align: justify;
}

.article-content :deep(br) {
  line-height: 1.8;
}

.article-content :deep(strong) {
  font-weight: 600;
  color: #409eff;
}

.article-content :deep(em) {
  font-style: italic;
  color: #606266;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.article-content :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

.article-content :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 20px;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
  color: #606266;
}
</style>
