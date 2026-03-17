<template>
  <view class="customer-history">
    <view class="header">
      <text class="title">{{ customerName }} 的租赁历史</text>
    </view>

    <scroll-view scroll-y class="content-area">
      <view class="list-item" v-for="order in orders" :key="order.id">
        <view class="order-info">
          <view class="item-row">
            <text class="order-id">订单 #{{ order.id }}</text>
            <text class="order-status">{{ formatStatus(order.status) }}</text>
          </view>
          <view class="item-row">
            <text class="item-name">{{ order.generatorName }}</text>
          </view>
          <view class="item-row">
            <text class="item-detail">金额: ¥{{ order.totalAmount }}</text>
            <text class="item-date">{{ formatDate(order.createdAt) }}</text>
          </view>
        </view>
      </view>
      <view v-if="orders.length === 0" class="empty">暂无租赁记录</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCustomerOrderHistory } from '@/api/order'

const tenantId = ref(null)
const customerName = ref('')
const orders = ref([])
const user = ref({})

const loadOrders = async () => {
  const u = uni.getStorageSync('user')
  if (u) {
    try { user.value = JSON.parse(u) } catch(e) { user.value = u }
  }

  if (!user.value.userid || !tenantId.value) return
  
  try {
    const res = await getCustomerOrderHistory(user.value.userid, tenantId.value)
    orders.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const formatStatus = (status) => {
  const map = {
    'WAIT_PAY': '待支付',
    'WAIT_CONFIRM': '待确认',
    'CONFIRMED': '已确认',
    'DELIVERED': '已交付',
    'RENTING': '租赁中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}

onMounted(() => {
  const pages = getCurrentPages()
  const options = pages[pages.length - 1].options
  tenantId.value = options.id
  customerName.value = options.name
  loadOrders()
})
</script>

<style scoped>
.customer-history {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}
.header {
  padding: 40rpx 30rpx;
  background-color: #ffffff;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.content-area {
  flex: 1;
  padding: 20rpx;
}
.list-item {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
}
.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}
.order-id {
  font-size: 28rpx;
  color: #999;
}
.order-status {
  font-size: 26rpx;
  color: #2979ff;
}
.item-name {
  font-size: 32rpx;
  font-weight: bold;
}
.item-detail {
  font-size: 26rpx;
  color: #666;
}
.item-date {
  font-size: 24rpx;
  color: #999;
}
.empty {
  text-align: center;
  padding: 100rpx;
  color: #999;
}
</style>
