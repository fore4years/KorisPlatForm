<template>
  <div class="user-profile">
    <el-page-header @back="$router.back()" class="page-header" title="返回">
      <template #content>
        <span class="header-title">个人中心</span>
      </template>
    </el-page-header>

    <div class="main-content">
      <el-card class="profile-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="title">基本信息</span>
            <el-button type="primary" @click="saveProfile" :loading="loading">保存修改</el-button>
          </div>
        </template>

        <el-form :model="form" label-width="120px" :rules="rules" ref="formRef" class="profile-form">
          <!-- Avatar Section -->
          <div class="avatar-section">
            <UploadImage v-model="form.avatar" type="avatar" :limit="2" />
          </div>

          <el-divider />

          <!-- Merchant Upgrade Section -->
          <div v-if="userRole === 'TENANT'" class="upgrade-section">
            <el-alert
              title="想要发布发电机赚取收益？"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <div class="upgrade-content">
                  <span>成为平台出租商户，发布闲置资源。</span>
                  <el-button type="primary" link @click="$router.push('/merchant/apply')">立即申请</el-button>
                </div>
              </template>
            </el-alert>
            <el-divider />
          </div>

          <!-- Tenant Fields -->
          <template v-if="userRole === 'TENANT'">
            <el-form-item label="用户类型">
                <el-radio-group v-model="tenantType">
                    <el-radio label="personal">个人用户</el-radio>
                    <el-radio label="enterprise">企业用户</el-radio>
                </el-radio-group>
            </el-form-item>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="昵称/姓名" prop="name">
                    <el-input v-model="form.name" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone" disabled>
                        <template #append>
                            <el-button @click="handleChangePhone">更换</el-button>
                        </template>
                    </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <template v-if="tenantType === 'personal'">
                <el-form-item label="身份证号" prop="identityCard">
                    <el-input v-model="form.identityCard" placeholder="修改需审核" />
                </el-form-item>
            </template>

            <el-form-item label="收货地址" prop="address">
                <el-input v-model="form.address" type="textarea" :rows="3" />
            </el-form-item>

            <template v-if="tenantType === 'enterprise'">
                <el-divider content-position="left">企业信息</el-divider>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="企业名称" prop="companyName">
                        <el-input v-model="form.companyName" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="联系人" prop="contactPerson">
                        <el-input v-model="form.contactPerson" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="营业执照" prop="businessLicense">
                    <el-input v-model="form.businessLicense" placeholder="营业执照编号 (修改需审核)" />
                </el-form-item>
            </template>
          </template>

          <!-- Merchant Fields -->
          <template v-if="userRole === 'MERCHANT'">
             <el-form-item label="店铺名称" prop="name">
                <el-input v-model="form.name" />
            </el-form-item>
             <el-form-item label="经营范围" prop="businessScope">
                <el-input v-model="form.businessScope" type="textarea" :rows="3" />
            </el-form-item>
             <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" disabled>
                    <template #append>
                         <el-button @click="handleChangePhone">更换</el-button>
                    </template>
                </el-input>
            </el-form-item>
             <el-form-item label="配送范围" prop="deliveryRange">
                <el-input v-model="form.deliveryRange" />
            </el-form-item>
             <el-form-item label="营业执照" prop="businessLicense">
                <el-input v-model="form.businessLicense" disabled placeholder="联系管理员修改" />
            </el-form-item>
          </template>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile, updateProfile } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import UploadImage from '../components/UploadImage.vue'

const router = useRouter()
const loading = ref(false)
const userRole = ref('')
const tenantType = ref('personal')
const formRef = ref(null)

const form = reactive({
  name: '',
  phone: '',
  avatar: '',
  address: '',
  identityCard: '',
  companyName: '',
  contactPerson: '',
  businessLicense: '',
  businessScope: '',
  deliveryRange: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await getProfile()
    const data = res
    userRole.value = data.role
    
    // Determine tenant type
    if (data.companyName) {
        tenantType.value = 'enterprise'
    }

    Object.assign(form, data)
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

const saveProfile = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateProfile(form)
        ElMessage.success('信息修改成功')
        
        // Update localStorage so header avatar updates immediately
        const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
        const updatedUser = { ...currentUser, ...form }
        localStorage.setItem('user', JSON.stringify(updatedUser))
        
      } catch (error) {
        ElMessage.error('修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleChangePhone = () => {
    ElMessageBox.prompt('请输入验证码 (模拟: 1234)', '更换手机号', {
        confirmButtonText: '验证',
        cancelButtonText: '取消',
    }).then(({ value }) => {
        if (value === '1234') {
            ElMessage.success('验证成功，请联系管理员更改手机号 (Demo)')
        } else {
            ElMessage.error('验证码错误')
        }
    }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-profile {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.page-header {
  margin-bottom: 24px;
}
.header-title {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}
.main-content {
  max-width: 800px;
  margin: 0 auto;
}
.profile-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.profile-form {
  padding: 0 20px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.upgrade-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
</style>
