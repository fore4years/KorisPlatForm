<template>
  <view class="forgot-container">
    <view class="forgot-content">
      <view class="header">
        <text class="title">重置密码</text>
      </view>

      <view class="step-card">
        <!-- Steps Indicator -->
        <view class="steps">
          <view :class="['step-item', activeStep >= 0 ? 'active' : '']">
            <text class="step-num">1</text>
            <text class="step-text">验证身份</text>
          </view>
          <view class="step-line"></view>
          <view :class="['step-item', activeStep >= 1 ? 'active' : '']">
            <text class="step-num">2</text>
            <text class="step-text">设置新密码</text>
          </view>
        </view>

        <!-- Step 1: Verify Phone -->
        <view v-if="activeStep === 0" class="form-content">
          <view class="form-item">
            <input class="input" v-model="step1Form.phone" placeholder="请输入手机号" type="number" />
          </view>
          <view class="form-item code-row">
            <input class="input code-input" v-model="step1Form.code" placeholder="验证码" type="number" />
            <button class="code-btn" type="primary" size="mini" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </view>
          <button type="primary" class="submit-btn" @click="handleNext">下一步</button>
        </view>

        <!-- Step 2: Set Password -->
        <view v-else-if="activeStep === 1" class="form-content">
          <view class="form-item">
            <input class="input" v-model="step2Form.password" password placeholder="新密码 (8-20位，含字母数字)" @input="checkStrength" />
            <view class="strength-bar" v-if="step2Form.password">
              <view :class="['bar', strengthLevel >= 1 ? 'active' : '']"></view>
              <view :class="['bar', strengthLevel >= 2 ? 'active' : '']"></view>
              <view :class="['bar', strengthLevel >= 3 ? 'active' : '']"></view>
            </view>
            <text class="strength-text" v-if="step2Form.password">{{ strengthText }}</text>
          </view>
          <view class="form-item">
            <input class="input" v-model="step2Form.confirmPassword" password placeholder="确认新密码" />
          </view>
          <button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading">确认重置</button>
        </view>

        <!-- Step 3: Success -->
        <view v-else class="success-view">
          <icon type="success" size="64" color="#67C23A"/>
          <text class="success-title">重置成功</text>
          <text class="success-desc">您的密码已更新，请重新登录</text>
          <button type="primary" class="submit-btn" @click="goToLogin">去登录</button>
        </view>

        <view class="footer-link">
           <text class="link" @click="goToLogin">返回登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { sendForgotPasswordCode, resetPassword } from '@/api/auth'

const activeStep = ref(0)
const countdown = ref(0)
const loading = ref(false)
const strengthLevel = ref(0)

const step1Form = reactive({
  phone: '',
  code: ''
})

const step2Form = reactive({
  password: '',
  confirmPassword: ''
})

const strengthText = computed(() => {
  switch (strengthLevel.value) {
    case 1: return '弱'
    case 2: return '中'
    case 3: return '强'
    default: return ''
  }
})

const checkStrength = (e) => {
  const val = e.detail.value
  let score = 0
  if (val.length >= 8) score++
  if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++
  if (/[0-9]/.test(val) && /[^A-Za-z0-9]/.test(val)) score++
  if (!(/[A-Za-z]/.test(val) && /[0-9]/.test(val))) {
      score = Math.min(score, 1)
  }
  strengthLevel.value = Math.min(score, 3)
}

const sendCode = async () => {
  if (!step1Form.phone || !/^1[3-9]\d{9}$/.test(step1Form.phone)) {
      uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
  }

  try {
    await sendForgotPasswordCode(step1Form.phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
     // Error handled by interceptor
  }
}

const handleNext = () => {
  if (!step1Form.phone || !step1Form.code) {
      uni.showToast({ title: '请填写手机号和验证码', icon: 'none' })
      return
  }
  // In a real app, you might verify the code with backend here.
  activeStep.value = 1
}

const handleSubmit = async () => {
  if (!step2Form.password || !step2Form.confirmPassword) {
      uni.showToast({ title: '请填写密码', icon: 'none' })
      return
  }
  if (step2Form.password !== step2Form.confirmPassword) {
      uni.showToast({ title: '两次密码不一致', icon: 'none' })
      return
  }

  loading.value = true
  try {
    await resetPassword({
      phone: step1Form.phone,
      code: step1Form.code,
      newPassword: step2Form.password
    })
    activeStep.value = 2
  } catch (error) {
    // Error handled
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
    uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<style lang="scss" scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40rpx 0;
}

.forgot-content {
  width: 90%;
  max-width: 600rpx;
}

.header {
    text-align: center;
    margin-bottom: 40rpx;
}
.title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
}

.step-card {
    background: #fff;
    padding: 40rpx;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.steps {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 60rpx;
}
.step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #ccc;
    &.active {
        color: #409EFF;
        .step-num {
            background: #409EFF;
            color: #fff;
            border-color: #409EFF;
        }
    }
}
.step-num {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    border: 2rpx solid #ccc;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 28rpx;
    margin-bottom: 10rpx;
}
.step-text {
    font-size: 24rpx;
}
.step-line {
    width: 80rpx;
    height: 2rpx;
    background: #eee;
    margin: 0 20rpx 30rpx 20rpx;
}

.form-content {
    margin-top: 20rpx;
}

.form-item {
    margin-bottom: 30rpx;
}

.input {
    height: 80rpx;
    background-color: #f5f7fa;
    border-radius: 8rpx;
    padding: 0 20rpx;
    font-size: 28rpx;
    border: 1px solid #dcdfe6;
}

.code-row {
    display: flex;
    gap: 20rpx;
}
.code-input {
    flex: 1;
}
.code-btn {
    width: 200rpx;
    height: 80rpx;
    line-height: 80rpx;
    font-size: 24rpx;
    padding: 0;
}

.strength-bar {
  display: flex;
  gap: 5rpx;
  margin-top: 10rpx;
  height: 6rpx;
}
.bar {
  flex: 1;
  background: #ebeef5;
  border-radius: 3rpx;
  transition: all 0.3s;
}
.bar.active {
  background: #67c23a;
}
.strength-text {
  font-size: 24rpx;
  color: #909399;
  text-align: right;
  margin-top: 4rpx;
  display: block;
}

.submit-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 32rpx;
    border-radius: 8rpx;
    margin-top: 40rpx;
}

.success-view {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40rpx 0;
}
.success-title {
    font-size: 32rpx;
    font-weight: bold;
    margin-top: 20rpx;
    color: #333;
}
.success-desc {
    font-size: 26rpx;
    color: #999;
    margin-top: 10rpx;
    margin-bottom: 40rpx;
}

.footer-link {
    text-align: center;
    margin-top: 30rpx;
}
.link {
    color: #606266;
    font-size: 26rpx;
}
</style>
