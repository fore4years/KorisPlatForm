<template>
  <div class="payment-container">
    <el-card class="payment-card">
      <div class="header">
        <h2>收银台</h2>
      </div>
      
      <div class="amount-section">
        <p class="label">支付金额</p>
        <p class="amount">¥{{ amount }}</p>
      </div>

      <div class="info">
        <p>订单号：{{ orderId }}</p>
        <p>收款方：发电机租赁平台</p>
      </div>

      <div class="payment-methods">
        <el-button 
          type="primary" 
          size="large" 
          class="pay-btn" 
          @click="handleAlipay"
          :loading="loading"
        >
          支付宝支付
        </el-button>
      </div>
      
      <div class="actions">
        <el-button link @click="$router.replace('/tenant/orders')">稍后支付</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getOrderDetail } from '../api/order'

const route = useRoute()
const router = useRouter()
const orderId = route.query.orderId
const amount = route.query.amount
const loading = ref(false)

onMounted(async () => {
  if (!orderId) return
  try {
    const res = await getOrderDetail(orderId)
    // request.js unwraps data, so res is the OrderResponse object directly
    const order = res 
    // If order is not in WAIT_PAY state (and not the special WAIT_CONFIRM pending case), redirect
    const isWaitPay = order.status === 'WAIT_PAY' || 
                     (order.status === 'WAIT_CONFIRM' && order.paymentMethod === 'ONLINE' && order.paymentStatus === 'PENDING')
    
    if (!isWaitPay) {
      ElMessage.success('该订单已支付或无需支付')
      router.replace('/tenant/orders')
    }
  } catch (error) {
    console.error('Check order status failed', error)
  }
})

const handleAlipay = async () => {
  loading.value = true
  try {
    const res = await request.post(`/payment/pay/${orderId}`)
    
    if (res.form) {
      // Render the HTML form from Alipay and submit it
      const div = document.createElement('div')
      div.innerHTML = res.form
      document.body.appendChild(div)
      document.forms[0].submit()
    } else {
      ElMessage.error('获取支付链接失败')
    }
  } catch (error) {
    // Error handled by request.js
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.payment-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.payment-card {
  width: 400px;
  text-align: center;
  padding: 40px 20px;
}
.amount-section {
  margin: 30px 0;
}
.label {
  color: #909399;
  font-size: 14px;
}
.amount {
  font-size: 36px;
  font-weight: bold;
  color: #f56c6c;
  margin-top: 5px;
}
.info {
  color: #606266;
  margin-bottom: 40px;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}
.info p {
  margin: 5px 0;
}
.pay-btn {
  width: 100%;
  margin-bottom: 15px;
  font-size: 16px;
}
</style>
