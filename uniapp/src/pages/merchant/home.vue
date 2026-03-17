<template>
  <view class="merchant-home">
    <view class="header">
        <text class="title">商家中心</text>
        <text class="subtitle" v-if="user.name">{{ user.name }}</text>
    </view>
    
    <view class="tabs">
        <view :class="['tab-item', activeTab === 'devices' ? 'active' : '']" @click="activeTab = 'devices'">
            设备管理
        </view>
        <view :class="['tab-item', activeTab === 'orders' ? 'active' : '']" @click="activeTab = 'orders'">
            订单管理
        </view>
        <view :class="['tab-item', activeTab === 'customers' ? 'active' : '']" @click="activeTab = 'customers'">
            客户管理
        </view>
    </view>

    <scroll-view scroll-y class="content-area">
        <!-- Devices Tab -->
        <view v-if="activeTab === 'devices'">
            <view class="action-bar">
                <button class="add-btn" type="primary" size="mini" @click="goToEdit(null)">+ 发布新设备</button>
            </view>
            
            <view class="list-item" v-for="item in devices" :key="item.id">
                <image :src="getImageUrl(item.imageUrl ? item.imageUrl.split(',')[0] : '')" class="item-img" mode="aspectFill" />
                <view class="item-info">
                    <view class="item-row">
                        <text class="item-name">{{ item.name }}</text>
                        <text class="item-status" :class="item.stockStatus">{{ formatStockStatus(item.stockStatus) }}</text>
                    </view>
                    <view class="item-row">
                        <text class="item-detail">{{ item.power }} | ¥{{ item.dailyRent }}/天</text>
                    </view>
                    <view class="item-actions">
                        <button size="mini" class="action-btn edit" @click="goToEdit(item.id)">编辑</button>
                        <button size="mini" class="action-btn delete" @click="handleDelete(item.id)">下架</button>
                    </view>
                </view>
            </view>
            <view v-if="devices.length === 0" class="empty">暂无设备</view>
        </view>

        <!-- Orders Tab -->
        <view v-if="activeTab === 'orders'">
            <view class="list-item" v-for="order in orders" :key="order.id" @click="goToOrder(order.id)">
                <view class="order-info">
                    <view class="item-row">
                        <text class="order-id">订单 #{{ order.id }}</text>
                        <text class="order-status">{{ formatStatus(order.status) }}</text>
                    </view>
                    <view class="item-row">
                        <text class="item-name">{{ order.generatorName }}</text>
                    </view>
                    <view class="item-row">
                        <text class="item-detail">¥{{ order.totalAmount }}</text>
                        <text class="item-date">{{ formatDate(order.createdAt) }}</text>
                    </view>
                </view>
            </view>
            <view v-if="orders.length === 0" class="empty">暂无订单</view>
        </view>

        <!-- Customers Tab -->
        <view v-if="activeTab === 'customers'">
            <view class="list-item customer-item" v-for="customer in customers" :key="customer.id" @click="goToCustomerHistory(customer)">
                <image :src="getImageUrl(customer.avatar)" class="avatar" mode="aspectFill" />
                <view class="customer-info">
                    <view class="item-row">
                        <text class="customer-name">{{ customer.name }}</text>
                        <text class="credit-tag" :class="customer.creditStatus === '良好' ? 'good' : 'bad'">{{ customer.creditStatus }}</text>
                    </view>
                    <view class="item-row">
                        <text class="item-detail">累计订单: {{ customer.totalOrders }} | 累计金额: ¥{{ customer.totalAmount }}</text>
                    </view>
                </view>
            </view>
            <view v-if="customers.length === 0" class="empty">暂无客户</view>
        </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getMyGenerators, deleteGenerator } from '@/api/generator'
import { getMerchantOrders, getMerchantCustomers } from '@/api/order'
import { getImageUrl } from '@/utils/image'

const user = ref({})
const activeTab = ref('devices')
const devices = ref([])
const orders = ref([])
const customers = ref([])

watch(activeTab, (newTab) => {
    if (newTab === 'devices') loadDevices()
    else if (newTab === 'orders') loadOrders()
    else if (newTab === 'customers') loadCustomers()
})

const loadUser = () => {
    const u = uni.getStorageSync('user')
    if (u) {
        try { user.value = JSON.parse(u) } catch(e) { user.value = u }
    }
}

const loadDevices = async () => {
    if (!user.value.userid) return
    try {
        const res = await getMyGenerators(user.value.userid)
        devices.value = res || []
    } catch (error) {
        console.error(error)
    }
}

const loadOrders = async () => {
    if (!user.value.userid) return
    try {
        const res = await getMerchantOrders(user.value.userid)
        orders.value = res || []
    } catch (error) {
        console.error(error)
    }
}

const loadCustomers = async () => {
    if (!user.value.userid) return
    try {
        const res = await getMerchantCustomers(user.value.userid)
        customers.value = res || []
    } catch (error) {
        console.error(error)
    }
}

const goToEdit = (id) => {
    uni.navigateTo({
        url: `/pages/merchant/generator-edit?id=${id || ''}`
    })
}

const goToCustomerHistory = (customer) => {
    uni.navigateTo({
        url: `/pages/merchant/customer-history?id=${customer.id}&name=${customer.name}`
    })
}

const handleDelete = (id) => {
    uni.showModal({
        title: '确认',
        content: '确定要下架该设备吗？',
        success: async (res) => {
            if (res.confirm) {
                try {
                    await deleteGenerator(id)
                    uni.showToast({ title: '已下架' })
                    loadDevices()
                } catch (e) {
                    uni.showToast({ title: '操作失败', icon: 'none' })
                }
            }
        }
    })
}

const goToOrder = (id) => {
    uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

const formatStockStatus = (s) => {
    const map = { 'AVAILABLE': '可租', 'RENTED': '已租', 'MAINTENANCE': '维护中' }
    return map[s] || s
}

const formatStatus = (s) => {
    const map = {
        'WAIT_CONFIRM': '待确认', 'CONFIRMED': '已确认', 'DELIVERED': '交付中',
        'RENTING': '租赁中', 'WAIT_RETURN': '待归还', 'COMPLETED': '已完成', 'CANCELLED': '已取消'
    }
    return map[s] || s
}

const formatDate = (d) => {
    if (!d) return ''
    return new Date(d).toLocaleDateString()
}

onMounted(() => {
    loadUser()
    // Load data when tab changes or initially
    loadDevices()
    loadOrders()
})
</script>

<style lang="scss" scoped>
.merchant-home {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f5f7fa;
}
.header {
    background: #fff;
    padding: 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.title { font-size: 32rpx; font-weight: bold; }
.subtitle { font-size: 24rpx; color: #666; }

.tabs {
    display: flex;
    background: #fff;
    border-bottom: 1px solid #eee;
}
.tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    font-size: 28rpx;
    color: #666;
    border-bottom: 2px solid transparent;
    &.active {
        color: #409EFF;
        border-bottom-color: #409EFF;
    }
}

.content-area {
    flex: 1;
    padding: 20rpx;
    box-sizing: border-box;
}

.action-bar {
    margin-bottom: 20rpx;
    display: flex;
    justify-content: flex-end;
}

.list-item {
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    display: flex;
    gap: 20rpx;
}

.item-img {
    width: 120rpx;
    height: 120rpx;
    border-radius: 8rpx;
    background: #eee;
}

.item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}
.order-info {
    flex: 1;
}

.item-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8rpx;
}

.item-name { font-size: 28rpx; font-weight: bold; }
.item-status { 
    font-size: 22rpx; 
    padding: 2rpx 8rpx; 
    border-radius: 4rpx;
    &.AVAILABLE { background: #f0f9eb; color: #67c23a; }
    &.RENTED { background: #fdf6ec; color: #e6a23c; }
    &.MAINTENANCE { background: #fef0f0; color: #f56c6c; }
}

.item-detail { font-size: 24rpx; color: #666; }
.item-date { font-size: 22rpx; color: #999; }

.item-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10rpx;
}

.action-btn {
    margin: 0;
    font-size: 22rpx;
    &.edit { background: #ecf5ff; color: #409EFF; }
    &.delete { background: #fef0f0; color: #f56c6c; }
}

.empty {
    text-align: center;
    padding: 50rpx;
    color: #999;
}

.order-id { font-size: 26rpx; color: #333; }
.order-status { font-size: 26rpx; color: #409EFF; }
</style>
