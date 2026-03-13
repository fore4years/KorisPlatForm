<template>
  <div class="register-container">
    <div class="theme-toggle-fixed">
      <ThemeToggle />
    </div>
    <div class="register-content">
      <div class="brand-section">
        <h1>加入我们</h1>
        <p>开启您的租赁之旅</p>
      </div>

      <el-card class="register-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>创建账号</h2>
            <p class="subtitle">请填写以下信息完成注册</p>
          </div>
        </template>
        
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form" size="large" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="registerForm.username" placeholder="设置用户名" :prefix-icon="User" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="registerForm.phone" placeholder="输入手机号" :prefix-icon="Iphone" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="设置密码 (8-20位，含字母数字)" 
              :prefix-icon="Lock" 
              show-password 
              @input="checkStrength"
            />
            <div class="strength-bar" v-if="registerForm.password">
              <div :class="['bar', strengthLevel >= 1 ? 'active' : '']"></div>
              <div :class="['bar', strengthLevel >= 2 ? 'active' : '']"></div>
              <div :class="['bar', strengthLevel >= 3 ? 'active' : '']"></div>
            </div>
            <div class="strength-text" v-if="registerForm.password">{{ strengthText }}</div>
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="registerForm.role" class="role-group">
              <el-radio-button label="TENANT">我是租户</el-radio-button>
              <el-radio-button label="MERCHANT">我是商家</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="姓名/企业名称" prop="name">
            <el-input v-model="registerForm.name" placeholder="真实姓名或企业名称" />
          </el-form-item>

          <el-form-item v-if="registerForm.role === 'TENANT'" label="身份证号" prop="identityCard">
            <el-input v-model="registerForm.identityCard" placeholder="请输入身份证号" />
          </el-form-item>

          <el-form-item v-if="registerForm.role === 'MERCHANT'" label="营业执照" prop="businessLicense">
            <el-input v-model="registerForm.businessLicense" placeholder="请输入营业执照号" />
          </el-form-item>

          <el-form-item>
             <div id="captcha-box-reg" style="width: 100%;"></div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading" :disabled="!captchaVerified">立即注册</el-button>
          </el-form-item>

          <div class="form-footer">
            <span>已有账号？</span>
            <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import ThemeToggle from '../components/ThemeToggle.vue'

const router = useRouter()
const loading = ref(false)
const captchaVerified = ref(false)
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

const registerFormRef = ref(null)
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '长度在 8 到 20 个字符', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!/[A-Za-z]/.test(value) || !/[0-9]/.test(value)) {
          callback(new Error('密码必须包含字母和数字'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名或企业名称', trigger: 'blur' }],
  identityCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请输入营业执照号', trigger: 'blur' }]
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
  // Ensure basic requirement doesn't give high score if only length matches
  if (!(/[A-Za-z]/.test(val) && /[0-9]/.test(val))) {
      score = Math.min(score, 1)
  }
  strengthLevel.value = Math.min(score, 3)
}

onMounted(() => {
    initCaptcha()
})

const initCaptcha = () => {
     // Mock implementation
     const box = document.getElementById('captcha-box-reg')
     if (box) {
         box.innerHTML = `
             <div style="background: #f5f7fa; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; text-align: center; cursor: pointer;" onclick="this.innerHTML='✓ 验证通过'; this.style.color='#67c23a'; this.style.borderColor='#67c23a'; window.dispatchEvent(new CustomEvent('captchaSuccessReg'))">
                 点击进行人机验证
             </div>
         `
         window.addEventListener('captchaSuccessReg', () => {
             captchaVerified.value = true
         })
     }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register(registerForm.value)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '注册失败')
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
  background-image: url('https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
  padding: 40px 0;
}

.register-container::before {
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

.register-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
  max-width: 500px;
  padding: 20px;
}

.brand-section {
  text-align: center;
}

.brand-section h1 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 5px;
  font-weight: 700;
}

.brand-section p {
  font-size: 14px;
  color: #606266;
}

.register-card {
  width: 100%;
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08) !important;
  background: rgba(255, 255, 255, 0.95);
}

.card-header {
  text-align: center;
  padding: 5px 0;
}

.card-header h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 5px;
}

.subtitle {
  color: #909399;
  font-size: 13px;
}

.role-group {
  width: 100%;
}
.role-group :deep(.el-radio-button__inner) {
  width: 100%;
}
.role-group :deep(.el-radio-button) {
  width: 50%;
}

.submit-btn {
  width: 100%;
  font-weight: 600;
  height: 44px;
  margin-top: 10px;
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
