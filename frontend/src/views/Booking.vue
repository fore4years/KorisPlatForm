<template>
  <div class="booking-container" v-loading="loading">
    <el-page-header @back="$router.back()" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="text-large font-600 mr-3"> 租赁预订 </span>
          <ThemeToggle />
        </div>
      </template>
    </el-page-header>

    <div class="content-wrapper" v-if="generator">
      <el-row :gutter="30">
        <!-- Left: Booking Form -->
        <el-col :md="14">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>预订信息</span>
              </div>
            </template>
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
              <el-form-item label="租赁设备">
                <div class="generator-summary">
                  <img :src="generator.imageUrl ? generator.imageUrl.split(',')[0] : 'https://via.placeholder.com/100'" class="thumb" />
                  <div>
                    <div class="gen-name">{{ generator.name }}</div>
                    <div class="gen-price">¥{{ generator.dailyRent }}/天</div>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="租赁时间" prop="dateRange">
                <el-date-picker
                  v-model="dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  :disabled-date="disabledDate"
                  @change="handleDateChange"
                />
              </el-form-item>

              <el-form-item label="交付地点" prop="deliveryAddress">
                <el-input v-model="form.deliveryAddress" placeholder="请输入详细交付地址" />
              </el-form-item>

              <el-form-item label="联系人" prop="contactName">
                <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
              </el-form-item>

              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>

              <el-form-item label="支付方式" prop="paymentMethod">
                <el-radio-group v-model="form.paymentMethod">
                  <el-radio label="ONLINE" border>在线支付 (支付宝/微信)</el-radio>
                  <el-radio label="OFFLINE" border>线下支付 (对公转账/现金)</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- Right: Price Summary -->
        <el-col :md="10">
          <el-card class="price-card">
            <template #header>
              <div class="card-header">
                <span>费用明细</span>
              </div>
            </template>
            
            <div v-if="priceDetails">
              <div class="price-row">
                <span>租金 ({{ priceDetails.days }}天)</span>
                <span>¥{{ priceDetails.rentAmount }}</span>
              </div>
              <div class="price-row">
                <span>押金</span>
                <span>¥{{ priceDetails.depositAmount }}</span>
              </div>
              <div class="price-row">
                <span>平台服务费 (5%)</span>
                <span>¥{{ priceDetails.serviceFee }}</span>
              </div>
              <el-divider />
              <div class="total-row">
                <span>总计</span>
                <span class="total-amount">¥{{ priceDetails.totalAmount }}</span>
              </div>
              
              <div class="payment-note" v-if="form.paymentMethod === 'ONLINE'">
                * 点击提交将跳转至支付页面
              </div>
              <div class="payment-note" v-else>
                * 提交后请等待商家确认并联系线下付款
              </div>
            </div>
            <div v-else class="empty-price">
              请选择租赁时间以计算费用
            </div>

            <el-button 
              type="primary" 
              size="large" 
              style="width: 100%; margin-top: 20px" 
              @click="submitOrder"
              :disabled="!priceDetails"
            >
              提交订单
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGeneratorDetail } from '../api/generator'
import { calculatePrice, createOrder } from '../api/order'
import { ElMessage } from 'element-plus'

import ThemeToggle from '../components/ThemeToggle.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const generator = ref(null)
const dateRange = ref([])
const priceDetails = ref(null)

const form = reactive({
  generatorId: route.params.id,
  deliveryAddress: '',
  contactName: '',
  contactPhone: '',
  paymentMethod: 'ONLINE'
})

const formRef = ref(null)
const rules = {
  dateRange: [{ required: true, message: '请选择租赁时间', trigger: 'change' }],
  deliveryAddress: [{ required: true, message: '请输入详细交付地址', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

// Disable past dates
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // Disable dates before today
}

// Load Generator Info
const loadGenerator = async () => {
  loading.value = true
  try {
    const res = await getGeneratorDetail(route.params.id)
    generator.value = res
    // Pre-fill user info if available
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user) {
      form.contactName = user.name
      form.contactPhone = user.phone
    }
  } catch (error) {
    ElMessage.error('加载设备信息失败')
  } finally {
    loading.value = false
  }
}

const handleDateChange = async () => {
  // Sync to form for validation
  form.dateRange = dateRange.value
  
  // Clear validation error if any
  if (formRef.value && dateRange.value) {
      formRef.value.clearValidate('dateRange')
  }

  if (!dateRange.value || dateRange.value.length !== 2) {
    priceDetails.value = null
    return
  }
  
  try {
    const res = await calculatePrice({
      generatorId: form.generatorId,
      startTime: dateRange.value[0].toISOString(),
      endTime: dateRange.value[1].toISOString()
    })
    priceDetails.value = res
  } catch (error) {
    ElMessage.error('计算价格失败')
  }
}

const submitOrder = async () => {
  if (!formRef.value) return
  
  // Custom check for dateRange since it's separate from form model
  // Note: element-plus form validation will also trigger based on 'prop'
  // so we sync dateRange to form model or rely on validation
  
  // Actually, 'dateRange' is not in 'form' reactive object but is used in template prop="dateRange"
  // Element Plus validation looks for model[prop]. Since dateRange is ref and not in form, validation might fail or be weird.
  // Best practice: put dateRange inside form
  form.dateRange = dateRange.value

  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择租赁时间')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (valid) {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      if (!user.id) {
        ElMessage.error('请先登录')
        router.push('/login')
        return
      }

      loading.value = true
      try {
        const orderData = {
          ...form,
          startTime: dateRange.value[0].toISOString(),
          endTime: dateRange.value[1].toISOString()
        }
        
        const res = await createOrder(orderData, user.id)
        const order = res
        
        if (form.paymentMethod === 'ONLINE') {
          router.push({
            path: '/payment',
            query: {
              orderId: order.id,
              amount: order.totalAmount
            }
          })
        } else {
          ElMessage.success('订单提交成功，请等待商家确认')
          router.push('/tenant') // Or Order List
        }
      } catch (error) {
        ElMessage.error('提交订单失败')
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请填写必填项')
    }
  })
}

onMounted(() => {
  loadGenerator()
})
</script>

<style scoped>
.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}
.booking-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}
.generator-summary {
  display: flex;
  align-items: center;
  background-color: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
}
.thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  margin-right: 15px;
  border-radius: 4px;
}
.gen-name {
  font-weight: bold;
}
.gen-price {
  color: #f56c6c;
}
.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
}
.total-row {
  display: flex;
  justify-content: space-between;
  font-size: 18px;
  font-weight: bold;
  margin-top: 15px;
}
.total-amount {
  color: #f56c6c;
}
.empty-price {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}
.payment-note {
  margin-top: 15px;
  font-size: 12px;
  color: #909399;
}
</style>
