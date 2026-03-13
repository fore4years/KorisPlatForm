<template>
  <div class="payment-success-container">
    <el-card class="success-card">
      <div class="icon-wrapper">
        <el-icon class="success-icon" :size="80" color="#67C23A"><CircleCheckFilled /></el-icon>
      </div>
      <h2>支付成功</h2>
      <p class="description">您的订单已支付完成，请等待商家发货。</p>
      <p class="redirect-tip">{{ countdown }}秒后自动返回订单列表...</p>
      
      <div class="info-item" v-if="amount">
        <span>支付金额：</span>
        <span class="amount">¥{{ amount }}</span>
      </div>
      <div class="info-item" v-if="tradeNo">
        <span>交易单号：</span>
        <span class="trade-no">{{ tradeNo }}</span>
      </div>

      <div class="actions">
        <el-button type="primary" @click="$router.replace('/tenant')">返回首页</el-button>
        <el-button @click="viewOrder">查看订单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const amount = ref('')
const tradeNo = ref('')
const orderId = ref('')
const countdown = ref(3)
let timer = null

onMounted(async () => {
  // Alipay returns parameters in query
  amount.value = route.query.total_amount
  tradeNo.value = route.query.trade_no
  orderId.value = route.query.out_trade_no
  
  if (orderId.value && tradeNo.value) {
    try {
      await request.post(`/payment/success/${orderId.value}`, {
        tradeNo: tradeNo.value
      })
    } catch (error) {
      console.error('Failed to update payment status', error)
      ElMessage.error('更新支付状态失败，请联系客服')
    }
  }

  // Start countdown
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      router.replace('/tenant/orders')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const viewOrder = () => {
  if (orderId.value) {
    router.replace(`/order/${orderId.value}`)
  } else {
    router.replace('/tenant/orders')
  }
}
</script>

<style scoped>
.redirect-tip {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}
.payment-success-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.success-card {
  width: 100%;
  max-width: 500px;
  text-align: center;
  padding: 40px 20px;
}

.icon-wrapper {
  margin-bottom: 20px;
}

h2 {
  color: #303133;
  margin-bottom: 10px;
}

.description {
  color: #606266;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.trade-no {
  color: #303133;
}

.actions {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
