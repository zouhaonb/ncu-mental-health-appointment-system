<template>
  <div class="assessment-container">
    <el-card>
      <template #header>
        <span>心理测评</span>
      </template>

      <el-alert
        title="请选择测评类型"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="assessment-card" @click="startAssessment('DEPRESSION')">
            <div class="assessment-type">
              <el-icon :size="60" color="#667eea"><Document /></el-icon>
              <h3>抑郁自评量表 (SDS)</h3>
              <p>用于评估抑郁症状的严重程度（10题精简版）</p>
              <el-button type="primary" size="large">开始测评</el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover" class="assessment-card" @click="startAssessment('ANXIETY')">
            <div class="assessment-type">
              <el-icon :size="60" color="#f093fb"><Document /></el-icon>
              <h3>焦虑自评量表 (SAS)</h3>
              <p>用于评估焦虑症状的严重程度（10题精简版）</p>
              <el-button type="success" size="large">开始测评</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="currentType === 'DEPRESSION' ? '抑郁自评量表 (SDS)' : '焦虑自评量表 (SAS)'"
      width="900px"
      :close-on-click-modal="false"
      class="assessment-dialog"
    >
      <div class="guide-text">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
        >
          <template #title>
            <strong>作答指导语：</strong>请根据您过去一周的实际情况，选择最符合您的选项。
          </template>
        </el-alert>
      </div>

      <el-form :model="assessmentForm" label-width="100%" class="questions-form">
        <div v-for="(question, index) in questions" :key="index" class="question-item">
          <p class="question-text">
            {{ index + 1 }}. {{ question.text }}
            <el-tag v-if="question.reverse" type="warning" size="small" style="margin-left: 8px">
              反向题
            </el-tag>
          </p>
          <el-radio-group v-model="assessmentForm.answers[index]" class="options-group">
            <el-radio
              v-for="option in question.options"
              :key="option.value"
              :label="option.value"
              class="option-item"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssessment" :loading="submitting">
          提交测评
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="resultVisible"
      title="测评结果"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="result-content">
        <div class="result-header">
          <h3>{{ currentType === 'DEPRESSION' ? '抑郁自评量表 (SDS)' : '焦虑自评量表 (SAS)' }}</h3>
        </div>

        <div class="result-score">
          <div class="score-item">
            <span class="label">原始总分：</span>
            <span class="value">{{ resultData.rawScore }} 分</span>
          </div>
          <div class="score-item">
            <span class="label">标准分：</span>
            <span class="value highlight">{{ resultData.standardScore }} 分</span>
          </div>
          <div class="score-item">
            <span class="label">风险等级：</span>
            <el-tag :type="getRiskTagType(resultData.riskLevel)" size="large">
              {{ getRiskText(resultData.riskLevel) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="result-interpretation">
          <h4>结果解读：</h4>
          <p>{{ resultData.interpretation }}</p>
        </div>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 20px"
        >
          <template #default>
            <strong>温馨提示：</strong>本量表仅为情绪状态的初步筛查，不能替代专业诊断。如果分数偏高或情绪困扰持续，建议联系心理咨询师或专业医生进一步评估。
          </template>
        </el-alert>
      </div>

      <template #footer>
        <el-button @click="resultVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToCounselor">预约咨询师</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { studentAPI } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const dialogVisible = ref(false)
const resultVisible = ref(false)
const currentType = ref('')
const submitting = ref(false)
const resultData = reactive({
  rawScore: 0,
  standardScore: 0,
  riskLevel: '',
  interpretation: ''
})

const assessmentForm = reactive({ answers: [] })

// 使用默认阈值，后续由后端根据配置动态计算
// phq9_threshold: 15 (抑郁高风险阈值)
// gad7_threshold: 10 (焦虑高风险阈值)

// SDS 抑郁自评量表（10题精简版）
const sdsQuestions = [
  {
    text: '我觉得闷闷不乐，情绪低沉',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得一天之中早晨最好',
    reverse: true,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我一阵阵哭出来或觉得想哭',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我晚上睡眠不好',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我吃得跟平常一样多',
    reverse: true,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我无缘无故地感到疲乏',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我对将来抱有希望',
    reverse: true,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得不安而平静不下来',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得自己是个有用的人，有人需要我',
    reverse: true,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我认为如果我死了别人会生活得好些',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  }
]

// SAS 焦虑自评量表（10题精简版）
const sasQuestions = [
  {
    text: '我觉得比平时容易紧张或着急',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我无缘无故地感到害怕',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我容易心里烦乱或觉得惊恐',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得我可能将要发疯',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得一切都很好，也不会发生什么不幸',
    reverse: true,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我手脚发抖打颤',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我感觉容易衰弱和疲乏',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得心跳得很快',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我觉得心平气和，并且容易安静坐着',
    reverse: true,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  },
  {
    text: '我做恶梦',
    reverse: false,
    options: [
      { label: '没有或很少时间', value: 1 },
      { label: '小部分时间', value: 2 },
      { label: '相当多时间', value: 3 },
      { label: '绝大部分或全部时间', value: 4 }
    ]
  }
]

const questions = ref([])

const startAssessment = (type) => {
  currentType.value = type
  questions.value = type === 'DEPRESSION' ? sdsQuestions : sasQuestions
  assessmentForm.answers = new Array(questions.value.length).fill(null)
  dialogVisible.value = true
}

// 计算分数（包含反向计分）
const calculateScore = () => {
  let rawScore = 0

  questions.value.forEach((question, index) => {
    const answer = assessmentForm.answers[index]
    if (answer !== null) {
      if (question.reverse) {
        // 反向题：1→4, 2→3, 3→2, 4→1
        rawScore += (5 - answer)
      } else {
        // 正向题：直接计分
        rawScore += answer
      }
    }
  })

  // 转换为标准分：原始总分 × 1.25，取整数部分
  const standardScore = Math.round(rawScore * 1.25)

  return { rawScore, standardScore }
}

const getRiskLevel = (standardScore) => {
  if (currentType.value === 'DEPRESSION') {
    // SDS 抑郁量表风险等级（标准分）
    if (standardScore < 33) return 'NORMAL'
    if (standardScore < 38) return 'LOW'
    if (standardScore < 42) return 'MEDIUM'
    return 'HIGH'
  } else {
    // SAS 焦虑量表风险等级（标准分）
    if (standardScore < 31) return 'NORMAL'
    if (standardScore < 36) return 'LOW'
    if (standardScore < 40) return 'MEDIUM'
    return 'HIGH'
  }
}

const getRiskText = (riskLevel) => {
  const map = {
    'NORMAL': '正常',
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险'
  }
  return map[riskLevel] || riskLevel
}

const getRiskTagType = (riskLevel) => {
  const map = {
    'NORMAL': 'success',
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return map[riskLevel] || ''
}

const getInterpretation = (standardScore, riskLevel) => {
  if (currentType.value === 'DEPRESSION') {
    // SDS 抑郁量表解读
    if (riskLevel === 'NORMAL') {
      return '心态积极稳定，情绪波动小，能够正常应对各类事务。'
    } else if (riskLevel === 'LOW') {
      return '偶尔情绪低落，兴致下降，调整心态后可逐步恢复状态。'
    } else if (riskLevel === 'MEDIUM') {
      return '低落情绪时常出现，行动力减退，做事提不起精神，影响生活状态。'
    } else {
      return '负面情绪难以排解，自我认可度低，生活状态受极大影响，务必咨询专业心理人员。'
    }
  } else {
    // SAS 焦虑量表解读
    if (riskLevel === 'NORMAL') {
      return '情绪状态平稳，无明显焦虑症状，日常学习生活不受影响。'
    } else if (riskLevel === 'LOW') {
      return '偶尔紧张担忧，存在轻微躯体不适，自身基本可以调节。'
    } else if (riskLevel === 'MEDIUM') {
      return '负面情绪频发，持续时间久，对作息与人际相处产生一定影响。'
    } else {
      return '焦虑症状突出，易出现惶恐不适感，严重干扰正常生活，建议及时寻求专业疏导。'
    }
  }
}

const submitAssessment = async () => {
  if (assessmentForm.answers.some(a => a === null)) {
    ElMessage.warning('请完成所有题目')
    return
  }

  try {
    await ElMessageBox.confirm('确定要提交测评吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    submitting.value = true

    const { rawScore, standardScore } = calculateScore()
    const riskLevel = getRiskLevel(standardScore)
    const interpretation = getInterpretation(standardScore, riskLevel)

    // 获取学生信息，得到正确的 studentId
    const studentInfo = await studentAPI.getInfo()
    const correctStudentId = studentInfo.data.id

    const data = {
      studentId: correctStudentId,
      scaleType: currentType.value,
      totalScore: standardScore,
      riskLevel,
      resultJson: JSON.stringify(assessmentForm.answers)
    }

    await studentAPI.submitAssessment(data)

    // 显示结果
    resultData.rawScore = rawScore
    resultData.standardScore = standardScore
    resultData.riskLevel = riskLevel
    resultData.interpretation = interpretation

    dialogVisible.value = false
    resultVisible.value = true

  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    submitting.value = false
  }
}

const goToCounselor = () => {
  resultVisible.value = false
  router.push('/student/counselors')
}
</script>

<style scoped>
.assessment-container {
  padding: 20px;
}

.assessment-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.assessment-card:hover {
  transform: translateY(-5px);
}

.assessment-type {
  text-align: center;
  padding: 20px;
}

.assessment-type h3 {
  margin: 20px 0 10px;
  color: #303133;
}

.assessment-type p {
  color: #909399;
  margin-bottom: 20px;
}

.assessment-dialog :deep(.el-dialog__body) {
  max-height: 600px;
  overflow-y: auto;
}

.guide-text {
  margin-bottom: 20px;
}

.questions-form {
  padding: 0 20px;
}

.question-item {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EBEEF5;
}

.question-text {
  font-size: 16px;
  margin-bottom: 12px;
  color: #303133;
  font-weight: 500;
}

.options-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.option-item {
  margin-right: 0;
}

.result-content {
  padding: 10px 0;
}

.result-header h3 {
  margin: 0 0 20px 0;
  color: #303133;
  text-align: center;
}

.result-score {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.score-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.score-item:last-child {
  margin-bottom: 0;
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

.result-interpretation {
  margin-top: 20px;
}

.result-interpretation h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.result-interpretation p {
  line-height: 1.8;
  color: #606266;
  margin: 0;
}
</style>
