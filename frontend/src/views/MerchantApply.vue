<template>
  <div class="merchant-apply-page">
    <div class="apply-container">
      <div class="header">
        <h1>成为出租商户</h1>
        <p>提交资质信息，审核通过后即可发布设备、承接订单</p>
      </div>

      <el-card class="apply-card">
        <!-- Status View -->
        <div v-if="application && application.status" class="status-view">
          <el-result
            :icon="statusIcon"
            :title="statusTitle"
            :sub-title="statusSubtitle"
          >
            <template #extra>
              <div v-if="application.status === 'REJECTED'" class="rejection-reason">
                <el-alert
                  title="驳回原因"
                  type="error"
                  :description="application.rejectionReason"
                  show-icon
                  :closable="false"
                />
                <el-button type="primary" @click="resetApplication" style="margin-top: 20px;">
                  重新申请
                </el-button>
              </div>
              <el-button v-else-if="application.status === 'APPROVED'" type="success" @click="$router.push('/merchant')">
                进入商户后台
              </el-button>
              <el-button v-else type="primary" @click="$router.push('/profile')">
                返回个人中心
              </el-button>
            </template>
          </el-result>
        </div>

        <!-- Application Form -->
        <el-form 
          v-else 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-width="120px" 
          class="apply-form"
        >
          <!-- 1. Basic Info -->
          <div class="section-title">基础信息</div>
          <el-form-item label="商户类型" prop="merchantType">
            <el-radio-group v-model="form.merchantType">
              <el-radio label="PERSONAL">个人商户</el-radio>
              <el-radio label="ENTERPRISE">企业商户</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="商户名称" prop="merchantName">
            <el-input v-model="form.merchantName" :placeholder="form.merchantType === 'PERSONAL' ? '请输入真实姓名' : '请输入企业名称'" />
          </el-form-item>

          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>

          <el-form-item label="经营地址" prop="address">
            <el-input v-model="form.address" placeholder="详细到街道，用于核验服务范围" />
          </el-form-item>

          <el-form-item label="服务类型" prop="serviceType">
            <el-checkbox-group v-model="form.serviceType">
              <el-checkbox label="短期租赁" />
              <el-checkbox label="长期租赁" />
              <el-checkbox label="柴油发电机" />
              <el-checkbox label="静音发电机" />
            </el-checkbox-group>
          </el-form-item>

          <!-- 2. Credentials -->
          <div class="section-title">核心凭证</div>
          <el-alert
            title="请上传清晰、无遮挡的证件照片，单张大小不超过5MB"
            type="info"
            show-icon
            :closable="false"
            style="margin-bottom: 20px;"
          />

          <!-- Personal Merchant -->
          <template v-if="form.merchantType === 'PERSONAL'">
            <el-form-item label="身份证正面" prop="idCardFront">
              <UploadImage v-model="form.idCardFront" type="cert" />
            </el-form-item>
            <el-form-item label="身份证反面" prop="idCardBack">
              <UploadImage v-model="form.idCardBack" type="cert" />
            </el-form-item>
          </template>

          <!-- Enterprise Merchant -->
          <template v-else>
            <el-form-item label="营业执照" prop="businessLicense">
              <UploadImage v-model="form.businessLicense" type="cert" />
            </el-form-item>
            <el-form-item label="法人身份证正面" prop="legalPersonIdFront">
              <UploadImage v-model="form.legalPersonIdFront" type="cert" />
            </el-form-item>
            <el-form-item label="法人身份证反面" prop="legalPersonIdBack">
              <UploadImage v-model="form.legalPersonIdBack" type="cert" />
            </el-form-item>
          </template>

          <!-- Common Generator Proof -->
          <el-form-item label="发电机权属证明" prop="generatorOwnerCert">
            <UploadImage v-model="form.generatorOwnerCert" type="cert" tip="购机发票或设备登记证" />
          </el-form-item>

          <el-form-item label="发电机实拍图" prop="generatorPhoto">
            <UploadImage v-model="form.generatorPhoto" type="product" tip="含设备铭牌、外观、运行状态" />
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="form.agreement">
              我已阅读并同意 <el-link type="primary">《平台商户入驻协议》</el-link> 和 <el-link type="primary">《设备租赁安全承诺书》</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting" size="large" style="width: 200px;">
              提交申请
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { submitMerchantApplication, getMyMerchantApplication } from '../api/user'
import UploadImage from '../components/UploadImage.vue'

const formRef = ref(null)
const submitting = ref(false)
const application = ref(null)

const form = reactive({
  merchantType: 'PERSONAL',
  merchantName: '',
  contactPhone: '',
  address: '',
  serviceType: [],
  idCardFront: '',
  idCardBack: '',
  businessLicense: '',
  legalPersonIdFront: '',
  legalPersonIdBack: '',
  generatorOwnerCert: '',
  generatorPhoto: '',
  agreement: false
})


const rules = {
  merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入经营地址', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  agreement: [{
    validator: (rule, value, callback) => {
      if (!value) callback(new Error('请阅读并同意协议'))
      else callback()
    },
    trigger: 'change'
  }],
  // Add other required fields validation
}

const statusIcon = computed(() => {
  if (!application.value) return 'info'
  switch (application.value.status) {
    case 'PENDING': return 'info'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'warning'
    default: return 'info'
  }
})

const statusTitle = computed(() => {
  if (!application.value) return ''
  switch (application.value.status) {
    case 'PENDING': return '申请审核中'
    case 'APPROVED': return '审核通过'
    case 'REJECTED': return '审核驳回'
    default: return ''
  }
})

const statusSubtitle = computed(() => {
  if (!application.value) return ''
  switch (application.value.status) {
    case 'PENDING': return '管理员正在核验您的资料，请耐心等待（1-3个工作日）'
    case 'APPROVED': return '恭喜您已成为出租商户，快去发布设备吧！'
    case 'REJECTED': return '抱歉，您的申请未通过，请修改后重新提交。'
    default: return ''
  }
})

onMounted(async () => {
  await loadApplication()
})

const loadApplication = async () => {
  try {
    const res = await getMyMerchantApplication()
    if (res) {
      application.value = res
      
      // If approved, refresh user profile to ensure local storage has correct role
      if (res.status === 'APPROVED') {
        const profileRes = await getProfile()
        localStorage.setItem('user', JSON.stringify(profileRes))
      }
    }
  } catch (e) {
    console.error('Failed to load application', e)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = {
          ...form,
          serviceType: form.serviceType.join(',')
        }
        await submitMerchantApplication(payload)
        ElMessage.success('申请提交成功')
        await loadApplication()
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '提交失败')
      } finally {
        submitting.value = false
      }
    } else {
      ElMessage.warning('请填写必填项')
    }
  })
}

const resetApplication = () => {
  application.value = null
  // Pre-fill form with rejected data if needed
}
</script>

<style scoped>
.merchant-apply-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 20px;
}

.apply-container {
  max-width: 800px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  margin-bottom: 10px;
  color: #303133;
}

.header p {
  color: #909399;
}

.apply-card {
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  text-align: center;
  line-height: 148px;
  border: 1px dashed #c0ccda;
}

.avatar {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.status-view {
  padding: 40px;
  text-align: center;
}

.rejection-reason {
  margin-bottom: 20px;
  text-align: left;
}
</style>
