<template>
  <div class="login-container">
    <div class="theme-toggle-fixed">
      <ThemeToggle />
    </div>
    <div class="login-content">
      <div class="brand-section">
        <h1>发电机租赁平台</h1>
        <p>高效 · 便捷 · 可靠的一站式租赁服务</p>
      </div>
      
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>欢迎登录</h2>
            <p class="subtitle">请使用您的账号访问平台</p>
          </div>
        </template>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form" size="large">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin" 
            />
          </el-form-item>
          
          <el-form-item>
             <el-checkbox v-model="loginForm.rememberMe">记住我 (7天内免登录)</el-checkbox>
          </el-form-item>
          
          <el-form-item>
             <div id="captcha-box" style="width: 100%;"></div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading" :disabled="!captchaVerified">登 录</el-button>
          </el-form-item>
          
          <div class="form-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
            <el-divider direction="vertical" />
            <el-link type="info" @click="$router.push('/forgot-password')">忘记密码</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import ThemeToggle from '../components/ThemeToggle.vue'

const router = useRouter()
const loading = ref(false)
const captchaVerified = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})

const loginFormRef = ref(null)
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  // Simulate captcha init
  // In real world, this would load Aliyun Captcha script
  initCaptcha()
})

const initCaptcha = () => {
    // Mock implementation of Aliyun Captcha
    const box = document.getElementById('captcha-box')
    if (box) {
        box.innerHTML = `
            <div style="background: #f5f7fa; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; text-align: center; cursor: pointer;" onclick="this.innerHTML='✓ 验证通过'; this.style.color='#67c23a'; this.style.borderColor='#67c23a'; window.dispatchEvent(new CustomEvent('captchaSuccess'))">
                点击进行人机验证
            </div>
        `
        window.addEventListener('captchaSuccess', () => {
            captchaVerified.value = true
        })
    }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const user = await login(loginForm.value)
        
        const storage = loginForm.value.rememberMe ? localStorage : sessionStorage
        storage.setItem('user', JSON.stringify(user))
        if (user.token) {
            storage.setItem('token', user.token)
        }

        ElMessage.success(`欢迎回来，${user.name || user.username}`)
        
        if (user.role === 'TENANT') {
          router.replace('/tenant')
        } else if (user.role === 'MERCHANT') {
          router.replace('/merchant')
        } else if (user.role === 'ADMIN') {
          router.replace('/admin')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名或密码')
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请填写必填项')
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
  background-image: url('https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(5px);
}

.theme-toggle-fixed {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.login-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.brand-section {
  text-align: center;
}

.brand-section h1 {
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-section p {
  font-size: 16px;
  color: #606266;
}

.login-card {
  width: 100%;
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08) !important;
  background: rgba(255, 255, 255, 0.95);
}

.card-header {
  text-align: center;
  padding: 10px 0;
}

.card-header h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-top: 10px;
}

.submit-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 1px;
  height: 44px;
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  margin-top: 10px;
}
</style>
