<template>
  <view class="register-container">
    <view class="register-content">
      <view class="brand-section">
        <text class="title">加入我们</text>
        <text class="subtitle">开启您的租赁之旅</text>
      </view>

      <view class="register-card">
        <view class="card-header">
          <text class="card-title">创建账号</text>
          <text class="card-subtitle">请填写以下信息完成注册</text>
        </view>
        
        <view class="register-form">
          <view class="form-item">
            <text class="label">用户名</text>
            <input class="input" v-model="registerForm.username" placeholder="设置用户名" />
          </view>
          
          <view class="form-item">
            <text class="label">手机号</text>
            <input class="input" v-model="registerForm.phone" placeholder="输入手机号" type="number" />
          </view>
          
          <view class="form-item">
            <text class="label">密码</text>
            <input class="input" v-model="registerForm.password" password placeholder="设置密码 (8-20位，含字母数字)" @input="checkStrength" />
            <view class="strength-bar" v-if="registerForm.password">
              <view :class="['bar', strengthLevel >= 1 ? 'active' : '']"></view>
              <view :class="['bar', strengthLevel >= 2 ? 'active' : '']"></view>
              <view :class="['bar', strengthLevel >= 3 ? 'active' : '']"></view>
            </view>
            <text class="strength-text" v-if="registerForm.password">{{ strengthText }}</text>
          </view>
          
          <view class="form-item">
            <text class="label">角色</text>
            <radio-group @change="handleRoleChange" class="role-group">
                <label class="radio-label">
                    <radio value="TENANT" :checked="registerForm.role === 'TENANT'" color="#409eff" />
                    <text>我是租户</text>
                </label>
                <label class="radio-label">
                    <radio value="MERCHANT" :checked="registerForm.role === 'MERCHANT'" color="#409eff" />
                    <text>我是商家</text>
                </label>
            </radio-group>
          </view>
          
          <view class="form-item">
            <text class="label">姓名/企业名称</text>
            <input class="input" v-model="registerForm.name" placeholder="真实姓名或企业名称" />
          </view>
          
          <view class="form-item" v-if="registerForm.role === 'TENANT'">
            <text class="label">身份证号</text>
            <input class="input" v-model="registerForm.identityCard" placeholder="请输入身份证号" />
          </view>
          
          <view class="form-item" v-if="registerForm.role === 'MERCHANT'">
            <text class="label">营业执照</text>
            <input class="input" v-model="registerForm.businessLicense" placeholder="请输入营业执照号" />
          </view>

          <view class="form-item">
             <button type="primary" class="submit-btn" @click="handleRegister" :loading="loading">立即注册</button>
          </view>
          
          <view class="form-footer">
            <text>已有账号？</text>
            <text class="link" @click="goToLogin">去登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { register } from '@/api/auth'

const loading = ref(false)
const strengthLevel = ref(0)

const registerForm = ref({
  username: '',
  password: '',
  phone: '',
  role: 'TENANT',
  name: '',
  identityCard: '',
  businessLicense: ''
})

const handleRoleChange = (e) => {
    registerForm.value.role = e.detail.value
}

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
  if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++ // This might be hard on mobile, maybe relax rules?
  // Let's stick to original logic but be mindful of mobile input
  if (/[0-9]/.test(val) && /[^A-Za-z0-9]/.test(val)) score++
  
  // Simplify for mobile if needed, but keeping consistent for now
  if (!(/[A-Za-z]/.test(val) && /[0-9]/.test(val))) {
      score = Math.min(score, 1)
  }
  strengthLevel.value = Math.min(score, 3)
}

const handleRegister = async () => {
  // Basic validation
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.phone || !registerForm.value.name) {
      uni.showToast({ title: '请填写必填项', icon: 'none' })
      return
  }
  
  loading.value = true
  try {
    await register(registerForm.value)
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
        uni.navigateBack()
    }, 1500)
  } catch (error) {
    // Error handled in request.js interceptor
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
    uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
  padding: 40rpx 0;
}

.register-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
  width: 90%;
}

.brand-section {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  font-size: 40rpx;
  color: #2c3e50;
  margin-bottom: 10rpx;
  font-weight: 700;
}

.subtitle {
  font-size: 24rpx;
  color: #606266;
}

.register-card {
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-title {
  font-size: 32rpx;
  color: #303133;
  margin-bottom: 10rpx;
  font-weight: bold;
}

.card-subtitle {
  color: #909399;
  font-size: 24rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
    font-size: 28rpx;
    color: #606266;
    margin-bottom: 10rpx;
    display: block;
}

.input {
    height: 80rpx;
    background-color: #f5f7fa;
    border-radius: 8rpx;
    padding: 0 20rpx;
    font-size: 28rpx;
    border: 1px solid #dcdfe6;
}

.role-group {
    display: flex;
    gap: 40rpx;
}

.radio-label {
    display: flex;
    align-items: center;
    font-size: 28rpx;
    color: #606266;
    gap: 10rpx;
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
    margin-top: 20rpx;
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10rpx;
  font-size: 24rpx;
  color: #606266;
  margin-top: 20rpx;
}

.link {
    color: #409eff;
}
</style>
