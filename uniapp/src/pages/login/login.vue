<template>
  <view class="login-container">
    <view class="login-content">
      <view class="brand-section">
        <text class="title">发电机租赁平台</text>
        <text class="subtitle">高效 · 便捷 · 可靠的一站式租赁服务</text>
      </view>
      
      <view class="login-card">
        <view class="card-header">
          <text class="card-title">欢迎登录</text>
          <text class="card-subtitle">请使用您的账号访问平台</text>
        </view>
        
        <view class="login-form">
          <view class="form-item">
            <input 
              class="input"
              v-model="loginForm.username" 
              placeholder="用户名" 
            />
          </view>
          
          <view class="form-item">
            <input 
              class="input"
              v-model="loginForm.password" 
              password 
              placeholder="密码" 
            />
          </view>
          
          <view class="form-item row">
             <label class="checkbox-label" @click="toggleRemember">
                <radio :checked="loginForm.rememberMe" color="#409eff" style="transform:scale(0.7)"/>
                <text>记住我 (7天内免登录)</text>
             </label>
          </view>
          
          <view class="form-item">
            <button type="primary" class="submit-btn" @click="handleLogin" :loading="loading">登 录</button>
          </view>
          
          <view class="form-footer">
            <text>还没有账号？</text>
            <text class="link" @click="goToRegister">立即注册</text>
            <text class="divider">|</text>
            <text class="link" @click="goToForgot">忘记密码</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
// Ensure api/auth exists and exports login
import { login } from '@/api/auth'

const loading = ref(false)
const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false
})

const toggleRemember = () => {
    loginForm.value.rememberMe = !loginForm.value.rememberMe;
}

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
      uni.showToast({ title: '请输入用户名和密码', icon: 'none' });
      return;
  }

  loading.value = true
  try {
    const user = await login(loginForm.value)
    
    uni.setStorageSync('user', JSON.stringify(user))
    if (user.token) {
        uni.setStorageSync('token', user.token)
    }

    uni.showToast({ title: `欢迎回来，${user.name || user.username}`, icon: 'success' })
    
    // Delay slightly to let toast show
    setTimeout(() => {
        if (user.role === 'TENANT') {
          uni.reLaunch({ url: '/pages/tenant/home' })
        } else if (user.role === 'MERCHANT') {
          uni.reLaunch({ url: '/pages/merchant/home' })
        } else if (user.role === 'ADMIN') {
          uni.reLaunch({ url: '/pages/admin/home' })
        } else {
            uni.reLaunch({ url: '/pages/index/index' })
        }
    }, 1000);

  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
    uni.navigateTo({ url: '/pages/register/register' });
}

const goToForgot = () => {
    uni.navigateTo({ url: '/pages/forgot-password/forgot-password' });
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
}

.login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
  width: 90%;
  max-width: 600rpx;
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

.login-card {
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

.login-form {
  margin-top: 10rpx;
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

.row {
    display: flex;
    align-items: center;
}

.checkbox-label {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #606266;
}

.submit-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 32rpx;
    border-radius: 8rpx;
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

.divider {
    color: #dcdfe6;
    margin: 0 10rpx;
}
</style>
