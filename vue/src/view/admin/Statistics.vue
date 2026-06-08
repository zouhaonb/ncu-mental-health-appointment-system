<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">数据决策看板</h2>
      <p class="page-subtitle">全平台数据概览与历史报表</p>
    </div>

    <el-tabs v-model="activeTab" class="data-tabs">
      <el-tab-pane label="实时概览" name="overview">
        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-header">今日预约量</div>
              <div class="stat-value primary">{{ stats.todayAppointments || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-header">待处理预约</div>
              <div class="stat-value warning">{{ stats.pendingAppointments || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-header">已完成预约</div>
              <div class="stat-value success">{{ stats.completedAppointments || 0 }}</div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-header">完成率</div>
              <div class="stat-value rate">{{ completionRate }}%</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-header">学生总数</div>
              <div class="stat-value primary">{{ stats.totalStudents || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-header">咨询师总数</div>
              <div class="stat-value success">{{ stats.totalCounselors || 0 }}</div>
            </div>
          </el-col>
        </el-row>

        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">各学院预约分布</span>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="历史报表" name="history">
        <el-card class="filter-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">查询条件</span>
            </div>
          </template>
          <el-form :inline="true" :model="historyForm" class="filter-form">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="historyForm.startDate"
                type="date"
                placeholder="选择开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="historyForm.endDate"
                type="date"
                placeholder="选择结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadHistoryData" :loading="loading">
                查询
              </el-button>
              <el-button @click="resetHistoryForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <template v-if="historyData">
          <el-row :gutter="20" class="stats-row">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-header">总预约数</div>
                <div class="stat-value primary">{{ historyData.totalAppointments || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-header">完成率</div>
                <div class="stat-value rate">{{ historyData.completionRate }}%</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-header">时间范围</div>
                <div class="stat-value" style="color: #606266; font-size: 18px;">
                  {{ historyForm.startDate }} ~ {{ historyForm.endDate }}
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="chart-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span class="chart-title">预约趋势（按日期）</span>
                  </div>
                </template>
                <div ref="trendChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="chart-card" shadow="hover">
                <template #header>
                  <div class="chart-header">
                    <span class="chart-title">预约状态分布</span>
                  </div>
                </template>
                <div ref="statusChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>

          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <span class="chart-title">各学院预约分布</span>
              </div>
            </template>
            <div ref="collegeChartRef" class="chart-container"></div>
          </el-card>
        </template>

        <el-empty v-else description="请选择日期范围查询历史数据" :image-size="200" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { adminAPI } from '@/api'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const activeTab = ref('overview')
const loading = ref(false)

// 实时概览相关
const chartRef = ref(null)
let chartInstance = null

const stats = ref({
  totalUsers: 0,
  totalStudents: 0,
  totalCounselors: 0,
  totalAdvisors: 0,
  totalAppointments: 0,
  todayAppointments: 0,
  pendingAppointments: 0,
  completedAppointments: 0,
  totalAssessments: 0,
  totalAlerts: 0,
  collegeAppointments: []
})

const completionRate = computed(() => {
  const total = stats.value.totalAppointments || 0
  const completed = stats.value.completedAppointments || 0

  if (total === 0) return '0.00'
  return ((completed / total) * 100).toFixed(2)
})

// 历史报表相关
const historyForm = ref({
  startDate: '',
  endDate: ''
})
const historyData = ref(null)
const trendChartRef = ref(null)
const statusChartRef = ref(null)
const collegeChartRef = ref(null)
let trendChartInstance = null
let statusChartInstance = null
let collegeChartInstance = null

// 加载实时概览数据
const loadData = async () => {
  try {
    const res = await adminAPI.getSystemStatistics()
    stats.value = {
      ...stats.value,
      ...res.data
    }
    initChart()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const initChart = () => {
  if (!chartRef.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: {
        color: '#fff'
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: stats.value.collegeAppointments.map(item => item.college) || [],
      axisTick: {
        alignWithLabel: true
      },
      axisLabel: {
        fontSize: 12,
        color: '#606266',
        interval: 0,
        rotate: stats.value.collegeAppointments.length > 5 ? 30 : 0
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#606266'
      },
      axisLine: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '预约学生人数',
        type: 'bar',
        barWidth: '40%',
        data: stats.value.collegeAppointments.map(item => item.count) || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [8, 8, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f093fb' },
              { offset: 1, color: '#f5576c' }
            ])
          }
        },
        animationDelay: (idx) => idx * 100
      }
    ],
    animationEasing: 'elasticOut'
  }

  chartInstance.setOption(option)
}

// 加载历史报表数据
const loadHistoryData = async () => {
  if (!historyForm.value.startDate || !historyForm.value.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }

  if (new Date(historyForm.value.startDate) > new Date(historyForm.value.endDate)) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return
  }

  loading.value = true
  try {
    const res = await adminAPI.getHistoryStatistics({
      startDate: historyForm.value.startDate,
      endDate: historyForm.value.endDate
    })
    historyData.value = res.data

    await nextTick()

    initHistoryCharts()
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('加载历史数据失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetHistoryForm = () => {
  historyForm.value = {
    startDate: '',
    endDate: ''
  }
  historyData.value = null
  disposeHistoryCharts()
}

const initHistoryCharts = () => {
  if (!historyData.value) return

  initTrendChart()
  initStatusChart()
  initCollegeChart()

  setTimeout(() => {
    trendChartInstance?.resize()
    statusChartInstance?.resize()
    collegeChartInstance?.resize()
  }, 100)
}

const initTrendChart = () => {
  if (!trendChartRef.value) return

  if (trendChartInstance) {
    trendChartInstance.dispose()
  }

  trendChartInstance = echarts.init(trendChartRef.value)

  const dailyData = historyData.value.dailyAppointments || []

  if (dailyData.length === 0) {
    const option = {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 14
        }
      }
    }
    trendChartInstance.setOption(option)
    return
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: {
        color: '#fff'
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dailyData.map(item => item.date),
      axisLabel: {
        fontSize: 12,
        color: '#606266',
        rotate: dailyData.length > 7 ? 30 : 0
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '预约数量',
        type: 'line',
        data: dailyData.map(item => item.count),
        smooth: true,
        lineStyle: {
          color: '#409eff',
          width: 3
        },
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        animationEasing: 'elasticOut'
      }
    ]
  }

  trendChartInstance.setOption(option)
}

const initStatusChart = () => {
  if (!statusChartRef.value) return

  if (statusChartInstance) {
    statusChartInstance.dispose()
  }

  statusChartInstance = echarts.init(statusChartRef.value)

  const statusStats = historyData.value.statusStatistics || {}

  if (Object.keys(statusStats).length === 0) {
    const option = {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 14
        }
      }
    }
    statusChartInstance.setOption(option)
    return
  }

  const statusNames = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  const statusColors = {
    'PENDING': '#e6a23c',
    'CONFIRMED': '#409eff',
    'COMPLETED': '#67c23a',
    'CANCELLED': '#f56c6c'
  }

  const data = Object.entries(statusStats).map(([status, count]) => ({
    name: statusNames[status] || status,
    value: count,
    itemStyle: {
      color: statusColors[status] || '#909399'
    }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: {
        color: '#fff'
      },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        fontSize: 12,
        color: '#606266'
      }
    },
    series: [
      {
        name: '预约状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: data,
        animationEasing: 'elasticOut'
      }
    ]
  }

  statusChartInstance.setOption(option)
}

const initCollegeChart = () => {
  if (!collegeChartRef.value) return

  if (collegeChartInstance) {
    collegeChartInstance.dispose()
  }

  collegeChartInstance = echarts.init(collegeChartRef.value)

  const collegeData = historyData.value.collegeAppointments || []

  if (collegeData.length === 0) {
    const option = {
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 14
        }
      }
    }
    collegeChartInstance.setOption(option)
    return
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: {
        color: '#fff'
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: collegeData.map(item => item.college),
      axisLabel: {
        fontSize: 12,
        color: '#606266',
        interval: 0,
        rotate: collegeData.length > 5 ? 30 : 0
      },
      axisLine: {
        lineStyle: {
          color: '#E4E7ED'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '预约学生人数',
        type: 'bar',
        barWidth: '40%',
        data: collegeData.map(item => item.count),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#85ce61' }
          ]),
          borderRadius: [8, 8, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f093fb' },
              { offset: 1, color: '#f5576c' }
            ])
          }
        },
        animationDelay: (idx) => idx * 100
      }
    ],
    animationEasing: 'elasticOut'
  }

  collegeChartInstance.setOption(option)
}

const disposeHistoryCharts = () => {
  trendChartInstance?.dispose()
  statusChartInstance?.dispose()
  collegeChartInstance?.dispose()
  trendChartInstance = null
  statusChartInstance = null
  collegeChartInstance = null
}

const handleResize = () => {
  chartInstance?.resize()
  trendChartInstance?.resize()
  statusChartInstance?.resize()
  collegeChartInstance?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  disposeHistoryCharts()
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

.data-tabs {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-header {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.rate {
  color: #409eff;
  font-size: 28px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.chart-card {
  margin-top: 20px;
}

.chart-header {
  display: flex;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 400px;
  width: 100%;
}
</style>