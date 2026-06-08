<template>
  <div class="counselor-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h2 class="page-title">咨询师预约</h2>
      <p class="page-subtitle">查看可预约的心理咨询师，选择适合您的咨询师进行预约</p>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索咨询师姓名、简介"
            clearable
            style="width: 240px"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-select
            v-model="searchForm.specialization"
            placeholder="请选择擅长领域"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in specializationOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 咨询师卡片列表 -->
    <div v-loading="loading" class="counselor-grid">
      <el-card
        v-for="counselor in filteredCounselors"
        :key="counselor.id"
        class="counselor-card"
        shadow="hover"
      >
        <div class="card-content">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <el-avatar :size="80" :src="counselor.avatar" icon="UserFilled" class="counselor-avatar" />
          </div>

          <!-- 基本信息 -->
          <div class="info-section">
            <h3 class="counselor-name">{{ counselor.name || '未命名咨询师' }}</h3>
            <p class="counselor-title">{{ counselor.title || '心理咨询师' }}</p>

            <!-- 擅长领域标签 -->
            <div class="specialization-tags">
              <el-tag
                v-for="(spec, index) in getSpecializations(counselor.specialization)"
                :key="index"
                size="small"
                effect="plain"
                class="spec-tag"
              >
                {{ spec }}
              </el-tag>
            </div>

            <!-- 简介描述 -->
            <p class="counselor-description">{{ counselor.description || '暂无简介' }}</p>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section">
            <el-button
              type="primary"
              @click="bookAppointment(counselor.id)"
              class="book-btn"
            >
              立即预约
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态提示 -->
    <el-empty
      v-if="!loading && filteredCounselors.length === 0"
      description="暂无符合条件的咨询师"
    />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="filteredCounselors.length > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="filteredCounselors.length"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { studentAPI } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const counselorList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)

const searchForm = ref({
  keyword: '',
  specialization: ''
})

const specializationOptions = ref([
  '情绪管理',
  '学业压力',
  '人际关系',
  '恋爱心理',
  '职业规划',
  '自我成长',
  '家庭关系',
  '社交焦虑',
  '新生适应',
  '危机干预',
  '创伤辅导',
  '正念减压'
])

const filteredCounselors = computed(() => {
  let result = [...counselorList.value]

  if (searchForm.value.keyword) {
    const keyword = searchForm.value.keyword.toLowerCase()
    result = result.filter(c =>
      (c.name && c.name.toLowerCase().includes(keyword)) ||
      (c.description && c.description.toLowerCase().includes(keyword))
    )
  }

  if (searchForm.value.specialization) {
    result = result.filter(c =>
      c.specialization && c.specialization.includes(searchForm.value.specialization)
    )
  }

  return result
})

const getSpecializations = (specialization) => {
  if (!specialization) return []
  return specialization.split(/[,，]/).map(s => s.trim()).filter(s => s)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await studentAPI.getCounselors()
    counselorList.value = res.data || []
    console.log('咨询师数据:', counselorList.value)
  } catch (error) {
    console.error('加载咨询师列表失败:', error)
    ElMessage.error('加载咨询师列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    specialization: ''
  }
  currentPage.value = 1
}

const bookAppointment = (counselorId) => {
  router.push(`/student/appointment/${counselorId}`)
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.counselor-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
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

.search-card {
  margin-bottom: 24px;
  border-radius: 8px;
}

.search-form {
  margin: 0;
}

.counselor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.counselor-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.counselor-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.avatar-section {
  margin-bottom: 16px;
}

.counselor-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.info-section {
  text-align: center;
  flex: 1;
  width: 100%;
}

.counselor-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.counselor-title {
  font-size: 14px;
  color: #409EFF;
  margin: 0 0 12px 0;
}

.specialization-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
}

.spec-tag {
  font-size: 12px;
  border-radius: 12px;
}

.counselor-description {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  text-align: justify;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 83px;
}

.action-section {
  margin-top: 16px;
  width: 100%;
}

.book-btn {
  width: 100%;
  height: 36px;
  border-radius: 18px;
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .counselor-grid {
    grid-template-columns: 1fr;
  }

  .search-form {
    flex-direction: column;
  }
}
</style>
