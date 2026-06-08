<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>系统配置</span>
      </template>

      <el-table :data="configs" v-loading="loading">
        <el-table-column prop="configKey" label="配置键" />
        <el-table-column prop="configValue" label="配置值" />
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑配置" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置键">
          <el-input v-model="form.configKey" disabled />
        </el-form-item>

        <el-form-item label="配置值">
          <el-input v-model="form.configValue" />
        </el-form-item>

        <el-form-item label="说明">
          <el-input v-model="form.description" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const configs = ref([])
const dialogVisible = ref(false)

const form = reactive({
  configKey: '',
  configValue: '',
  description: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getConfigList()
    configs.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const showEditDialog = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    await adminAPI.updateConfig(form)
    ElMessage.success('更新成功')
    dialogVisible.value = false
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
</style>
