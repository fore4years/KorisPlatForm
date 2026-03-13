<template>
  <div class="forgot-password-container">
    <div class="theme-toggle-fixed">
      <ThemeToggle />
    </div>
    <div class="content-box">
      <h2>重置密码</h2>
      <el-steps :active="activeStep" finish-status="success" align-center class="steps">
        <el-step title="验证身份" />
        <el-step title="设置新密码" />
        <el-step title="完成" />
      </el-steps>

      <el-form 
        v-if="activeStep === 0" 
        :model="step1Form" 
        :rules="step1Rules" 
        ref="step1Ref" 
        class="form-content"
      >
        <el-form-item prop="phone">
          <el-input v-model="step1Form.phone" placeholder="请输入手机号" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input">
            <el-input v-model="step1Form.code" placeholder="验证码" prefix-icon="Key" />
            <el-button type="primary" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-button type="primary" class="full-btn" @click="handleNext">下一步</el-button>
      </el-form>

      <el-form 
        v-else-if="activeStep === 1" 
        :model="step2Form" 
        :rules="step2Rules" 
        ref="step2Ref" 
        class="form-content"
      >
        <el-form-item prop="password">
          <el-input 
            v-model="step2Form.password" 
            type="password" 
            placeholder="新密码 (8-20位，含字母数字)" 
            prefix-icon="Lock" 
            show-password 
            @input="checkStrength"
          />
          <div class="strength-bar" v-if="step2Form.password">
            <div :class="['bar', strengthLevel >= 1 ? 'active' : '']"></div>
            <div :class="['bar', strengthLevel >= 2 ? 'active' : '']"></div>
            <div :class="['bar', strengthLevel >= 3 ? 'active' : '']"></div>
          </div>
          <div class="strength-text" v-if="step2Form.password">{{ strengthText }}</div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="step2Form.confirmPassword" type="password" placeholder="确认新密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="full-btn" @click="handleSubmit" :loading="loading">确认重置</el-button>
      </el-form>

      <div v-else class="success-view">
        <el-result icon="success" title="重置成功" sub-title="您的密码已更新，请重新登录">
          <template #extra>
            <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
          </template>
        </el-result>
      </div>

      <div class="footer-link">
        <el-link @click="$router.push('/login')">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { sendForgotPasswordCode, resetPassword } from '../api/auth'
import ThemeToggle from '../components/ThemeToggle.vue'

const activeStep = ref(0)
const countdown = ref(0)
const loading = ref(false)
const strengthLevel = ref(0)

const step1Ref = ref(null)
const step2Ref = ref(null)

const step1Form = reactive({
  phone: '',
  code: ''
})

const step2Form = reactive({
  password: '',
  confirmPassword: ''
})

const step1Rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const step2Rules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== step2Form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const strengthText = computed(() => {
  switch (strengthLevel.value) {
    case 1: return '弱'
    case 2: return '中'
    case 3: return '强'
    default: return ''
  }
})

const checkStrength = (val) => {
  let score = 0
  if (val.length >= 8) score++
  if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++
  if (/[0-9]/.test(val) && /[^A-Za-z0-9]/.test(val)) score++
  strengthLevel.value = Math.min(score, 3)
}

const sendCode = async () => {
  // Validate phone first
  try {
    await step1Ref.value.validateField('phone')
  } catch (e) {
    return
  }

  try {
    await sendForgotPasswordCode(step1Form.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  }
}

const handleNext = async () => {
  if (!step1Ref.value) return
  await step1Ref.value.validate((valid) => {
    if (valid) {
      // In real scenario, verify code with backend here or carry it to next step
      // Since our backend resets password in one go with code, we just move to next step UI
      // and verify everything at final submit. 
      // OR we can add a 'verifyCode' endpoint.
      // For simplicity here, assume code is correct format and proceed.
      activeStep.value = 1
    }
  })
}

const handleSubmit = async () => {
  if (!step2Ref.value) return
  await step2Ref.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await resetPassword({
          phone: step1Form.phone,
          code: step1Form.code,
          newPassword: step2Form.password
        })
        activeStep.value = 2
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '重置失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
}
.theme-toggle-fixed {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}
.content-box {
  width: 400px;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
.steps {
  margin-bottom: 30px;
}
.form-content {
  margin-top: 20px;
}
.code-input {
  display: flex;
  gap: 10px;
}
.full-btn {
  width: 100%;
  margin-top: 10px;
}
.footer-link {
  text-align: center;
  margin-top: 20px;
}
.strength-bar {
  display: flex;
  gap: 5px;
  margin-top: 5px;
  height: 4px;
}
.bar {
  flex: 1;
  background: #ebeef5;
  border-radius: 2px;
  transition: all 0.3s;
}
.bar.active {
  background: #67c23a;
}
.strength-text {
  font-size: 12px;
  color: #909399;
  text-align: right;
  margin-top: 2px;
}
</style>
