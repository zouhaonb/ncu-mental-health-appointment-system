<template>
  <div class="page-container">
    <!-- 趋势图区域 -->
    <el-card class="trend-card" v-if="hasAssessmentData">
      <template #header>
        <div class="card-header">
          <span>测评趋势</span>
          <el-radio-group v-model="chartType" size="small">
            <el-radio-button label="DEPRESSION">抑郁测评趋势</el-radio-button>
            <el-radio-button label="ANXIETY">焦虑测评趋势</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="trend-chart"></div>
    </el-card>

    <!-- 测评历史表格 -->
    <el-card>
      <template #header>
        <span>测评历史</span>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        empty-text="暂无测评记录，请先进行心理测评"
      >
        <el-table-column prop="scaleType" label="量表类型" width="150">
          <template #default="{ row }">
            <el-tag :type="row.scaleType === 'DEPRESSION' ? '' : 'success'">
              {{ row.scaleType === 'DEPRESSION' ? '抑郁自评 (SDS)' : '焦虑自评 (SAS)' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="测评日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="得分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-text">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)" effect="dark">
              {{ getRiskText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interpretation" label="结果解读" min-width="250">
          <template #default="{ row }">
            <span class="interpretation-text">{{ getInterpretationText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewReport(row)">
              查看报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 结果报告弹窗 -->
    <el-dialog
      v-model="reportVisible"
      title="测评结果报告"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="report-content" v-if="currentReport">
        <!-- 量表信息 -->
        <div class="report-header">
          <h3>{{ currentReport.scaleType === 'DEPRESSION' ? '抑郁自评量表 (SDS)' : '焦虑自评量表 (SAS)' }}</h3>
          <p class="report-time">测评时间：{{ formatDateTime(currentReport.createdAt) }}</p>
        </div>

        <el-divider />

        <!-- 得分情况 -->
        <div class="report-score">
          <h4>得分情况</h4>
          <div class="score-info">
            <div class="score-item">
              <span class="label">标准分：</span>
              <span class="value highlight">{{ currentReport.totalScore }} 分</span>
            </div>
            <div class="score-item">
              <span class="label">风险等级：</span>
              <el-tag :type="getRiskType(currentReport.riskLevel)" effect="dark" size="large">
                {{ getRiskText(currentReport.riskLevel) }}
              </el-tag>
            </div>
          </div>

          <!-- 标准分说明 -->
          <div class="score-description">
            <el-alert
              :title="getScoreDescription(currentReport)"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </div>

        <el-divider />

        <!-- 情绪表现参考 -->
        <div class="report-emotion">
          <h4>情绪表现参考</h4>
          <p class="emotion-text">{{ getEmotionReference(currentReport) }}</p>
        </div>

        <el-divider />

        <!-- 建议 -->
        <div class="report-suggestion">
          <h4>建议</h4>
          <el-alert
            :title="getSuggestion(currentReport)"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>

        <el-divider />

        <!-- 免责声明 -->
        <div class="report-disclaimer">
          <el-alert
            title="免责声明"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>本结果仅为初步筛查，不能替代专业诊断。如需进一步评估，请联系学校心理咨询师或专业医生。</p>
            </template>
          </el-alert>
        </div>
      </div>

      <template #footer>
        <el-button @click="reportVisible = false">关闭</el-button>
        <el-button type="primary" @click="bookCounselor">
          预约咨询师
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const reportVisible = ref(false)
const currentReport = ref(null)
const chartRef = ref(null)
const chartType = ref('DEPRESSION')
let chartInstance = null

const hasAssessmentData = ref(false)

const getRiskType = (level) => {
  const types = {
    'NORMAL': 'success',
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return types[level] || ''
}

const getRiskText = (level) => {
  const texts = {
    'NORMAL': '正常',
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险'
  }
  return texts[level] || level
}

const getInterpretationText = (row) => {
  if (row.scaleType === 'DEPRESSION') {
    // SDS 抑郁量表解读
    if (row.riskLevel === 'NORMAL') {
      return '心态积极稳定，情绪波动小，能够正常应对各类事务'
    } else if (row.riskLevel === 'LOW') {
      return '偶尔情绪低落，兴致下降，调整心态后可逐步恢复状态'
    } else if (row.riskLevel === 'MEDIUM') {
      return '低落情绪时常出现，行动力减退，做事提不起精神，影响生活状态'
    } else {
      return '负面情绪难以排解，自我认可度低，生活状态受极大影响，务必咨询专业心理人员'
    }
  } else {
    // SAS 焦虑量表解读
    if (row.riskLevel === 'NORMAL') {
      return '情绪状态平稳，无明显焦虑症状，日常学习生活不受影响'
    } else if (row.riskLevel === 'LOW') {
      return '偶尔紧张担忧，存在轻微躯体不适，自身基本可以调节'
    } else if (row.riskLevel === 'MEDIUM') {
      return '负面情绪频发，持续时间久，对作息与人际相处产生一定影响'
    } else {
      return '焦虑症状突出，易出现惶恐不适感，严重干扰正常生活，建议及时寻求专业疏导'
    }
  }
}

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

const formatDate = (dateTime) => {
  if (!dateTime) return ''
  try {
    const date = new Date(dateTime)
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return ''
  }
}

const viewReport = (row) => {
  currentReport.value = row
  reportVisible.value = true
}

const getScoreDescription = (report) => {
  const score = report.totalScore
  const type = report.scaleType === 'DEPRESSION' ? 'SDS' : 'SAS'

  if (type === 'SDS') {
    if (score < 31) {
      return `您的 SDS 标准分为 ${score} 分，处于正常范围内（< 31 分），说明您目前的情绪状态良好。`
    } else if (score < 40) {
      return `您的 SDS 标准分为 ${score} 分，处于轻度抑郁临界值（31-39 分），建议关注自己的情绪变化。`
    } else {
      return `您的 SDS 标准分为 ${score} 分，处于中重度抑郁范围（≥ 40 分），建议尽快寻求专业帮助。`
    }
  } else {
    if (score < 33) {
      return `您的 SAS 标准分为 ${score} 分，处于正常范围内（< 33 分），说明您目前的焦虑水平正常。`
    } else if (score < 42) {
      return `您的 SAS 标准分为 ${score} 分，处于中度焦虑临界值（33-41 分），建议关注自己的焦虑情绪。`
    } else {
      return `您的 SAS 标准分为 ${score} 分，处于重度焦虑范围（≥ 42 分），建议尽快寻求专业帮助。`
    }
  }
}

const getEmotionReference = (report) => {
  const riskLevel = report.riskLevel
  const type = report.scaleType === 'DEPRESSION' ? '抑郁' : '焦虑'

  if (riskLevel === 'LOW') {
    if (type === '抑郁') {
      return '情绪稳定，能积极面对生活和学习中的挑战，保持良好的心理状态。偶尔会有情绪波动，但能较快恢复。'
    } else {
      return '心态平和，能从容应对日常压力和挑战，偶尔会感到紧张或担心，但不影响正常的学习和生活。'
    }
  } else if (riskLevel === 'MEDIUM') {
    if (type === '抑郁') {
      return '偶尔感到情绪低落、兴趣减退，可能伴有睡眠质量下降、注意力不集中等情况，对日常活动有一定影响，但尚能维持基本的学习和生活。'
    } else {
      return '偶尔感到紧张、担心，可能伴有轻微的心跳加速、出汗等生理反应，偶尔会影响睡眠和学习效率，但基本能维持正常的日常活动。'
    }
  } else {
    if (type === '抑郁') {
      return '持续感到情绪低落、对事物失去兴趣，可能伴有严重的睡眠障碍、食欲改变、精力减退等症状，明显影响学习、社交和日常生活功能。'
    } else {
      return '经常感到紧张不安、担心恐惧，可能伴有心慌、出汗、颤抖、失眠等明显的生理和心理症状，严重影响学习效率、社交活动和日常生活。'
    }
  }
}

const getSuggestion = (report) => {
  const riskLevel = report.riskLevel

  if (riskLevel === 'LOW') {
    return '保持良好的生活习惯，规律作息，适度运动，保持社交活动，继续维持当前的健康状态。'
  } else if (riskLevel === 'MEDIUM') {
    return '建议关注情绪变化，学习一些放松技巧（如深呼吸、冥想、运动等），与信任的人交流感受，必要时联系学校心理咨询师进行专业评估。'
  } else {
    return '强烈建议您尽快预约学校心理咨询师进行专业评估和干预，同时告知辅导员或信任的老师，获得更多支持和帮助。'
  }
}

const bookCounselor = () => {
  reportVisible.value = false
  router.push('/student/counselors')
}

const initChart = () => {
  if (!chartRef.value) return

  try {
    chartInstance = echarts.init(chartRef.value)
    updateChart()
  } catch (error) {
    console.error('图表初始化失败:', error)
  }
}

const updateChart = () => {
  if (!chartInstance) {
    console.error('图表实例不存在')
    return
  }

  try {
    console.log('当前 chartType:', chartType.value)
    console.log('所有数据:', tableData.value)

    const filteredData = tableData.value
      .filter(item => {
        if (chartType.value === 'DEPRESSION') {
          return item.scaleType === 'DEPRESSION'
        } else {
          return item.scaleType === 'ANXIETY'
        }
      })
      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))

    console.log('筛选后的数据:', filteredData)

    const isDepression = chartType.value === 'DEPRESSION'
    const chartTitle = isDepression ? '抑郁自评量表 (SDS) 得分趋势' : '焦虑自评量表 (SAS) 得分趋势'
    const lineColor = isDepression ? '#667eea' : '#f093fb'
    const thresholdLine = isDepression ? 31 : 33

    // 如果没有数据，显示空状态
    if (filteredData.length === 0) {
      console.log('无数据，显示空状态')
      const option = {
        title: {
          text: chartTitle,
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 600,
            color: '#303133'
          }
        },
        graphic: [{
          type: 'text',
          left: 'center',
          top: 'middle',
          style: {
            text: '暂无' + (isDepression ? '抑郁' : '焦虑') + '测评数据',
            fontSize: 16,
            fill: '#909399'
          }
        }]
      }
      chartInstance.clear()
      chartInstance.setOption(option)
      return
    }

    const dates = filteredData.map(item => formatDate(item.createdAt))
    const scores = filteredData.map(item => item.totalScore)
    const riskLevels = filteredData.map(item => item.riskLevel)

    console.log('图表数据 - dates:', dates, 'scores:', scores)

    const option = {
      title: {
        text: chartTitle,
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 600,
          color: '#303133'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const data = params[0]
          const index = data.dataIndex
          const riskLevel = riskLevels[index]
          const riskText = getRiskText(riskLevel)
          return `${data.name}<br/>得分：<b>${data.value}</b> 分<br/>风险等级：<b>${riskText}</b>`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: {
          fontSize: 12,
          color: '#606266'
        }
      },
      yAxis: {
        type: 'value',
        name: '标准分',
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
          name: '标准分',
          type: 'line',
          data: scores,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            color: lineColor,
            width: 3
          },
          itemStyle: {
            color: lineColor
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: lineColor + '40' },
              { offset: 1, color: lineColor + '05' }
            ])
          },
          markLine: {
            data: [
              {
                yAxis: thresholdLine,
                lineStyle: {
                  color: '#E6A23C',
                  type: 'dashed',
                  width: 2
                },
                label: {
                  formatter: '临界值 ' + thresholdLine,
                  position: 'insideEndTop',
                  fontSize: 12
                }
              }
            ],
            symbol: 'none'
          }
        }
      ]
    }

    chartInstance.clear()
    chartInstance.setOption(option, true)
    console.log('图表已更新')
  } catch (error) {
    console.error('图表更新失败:', error)
  }
}

watch(chartType, (newVal) => {
  console.log('chartType 改变为:', newVal)
  nextTick(() => {
    updateChart()
  })
})

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

const loadData = async () => {
  loading.value = true
  try {
    if (!userStore.userId) {
      ElMessage.error('用户信息不存在，请重新登录')
      return
    }

    const res = await studentAPI.getAssessmentHistory(userStore.userId)
    tableData.value = res.data || []

    console.log('测评数据:', tableData.value)

    if (tableData.value.length > 0) {
      hasAssessmentData.value = true

      // 根据数据类型设置默认的 chartType
      const hasAnxiety = tableData.value.some(item => item.scaleType === 'ANXIETY')
      const hasDepression = tableData.value.some(item => item.scaleType === 'DEPRESSION')

      if (hasAnxiety && !hasDepression) {
        chartType.value = 'ANXIETY'
      } else if (hasDepression && !hasAnxiety) {
        chartType.value = 'DEPRESSION'
      }

      await nextTick()
      initChart()
    } else {
      hasAssessmentData.value = false
    }
  } catch (error) {
    console.error('加载测评历史失败:', error)
    ElMessage.error('加载测评历史失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.trend-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-chart {
  width: 100%;
  height: 400px;
}

.score-text {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

.interpretation-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.report-content {
  padding: 10px 0;
}

.report-header {
  text-align: center;
  margin-bottom: 10px;
}

.report-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.report-time {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.report-score h4,
.report-emotion h4,
.report-suggestion h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.score-info {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-item .label {
  font-size: 16px;
  color: #606266;
}

.score-item .value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.score-item .value.highlight {
  color: #409EFF;
  font-size: 24px;
}

.score-description {
  margin-top: 12px;
}

.emotion-text {
  line-height: 1.8;
  color: #606266;
  margin: 0;
  padding: 12px;
  background: #fafafa;
  border-left: 3px solid #409EFF;
  border-radius: 4px;
}

.report-disclaimer {
  margin-top: 10px;
}

.report-disclaimer p {
  margin: 0;
  line-height: 1.6;
}
</style>
