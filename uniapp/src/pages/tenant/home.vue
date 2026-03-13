<template>
  <view class="tenant-home">
    <!-- Header -->
    <view class="header">
      <text class="logo">发电机租赁</text>
      <view class="user-info">
        <image 
          class="user-avatar" 
          :src="user.avatar || '/static/default-avatar.png'" 
          mode="aspectFill"
          @click="goToProfile"
        />
        <text class="user-name" v-if="user.username">Hi, {{ user.username }}</text>
        <text class="logout-btn" @click="handleLogout">退出</text>
      </view>
    </view>

    <!-- Filter Section -->
    <view class="filter-section">
      <view class="search-bar">
        <view class="picker-wrap">
             <picker @change="bindPowerChange" :value="powerIndex" :range="powerOptions">
                <view class="picker-text">
                    {{ searchForm.power || '全部功率' }} ▾
                </view>
             </picker>
        </view>
        <input 
            class="search-input" 
            v-model="searchForm.location" 
            placeholder="输入位置" 
            confirm-type="search"
            @confirm="handleSearch"
        />
        <button class="search-btn" size="mini" type="primary" @click="handleSearch">搜索</button>
      </view>
      <view class="filter-row">
          <input class="mini-input" type="number" v-model="searchForm.minRent" placeholder="最低价" />
          <text>-</text>
          <input class="mini-input" type="number" v-model="searchForm.maxRent" placeholder="最高价" />
          <view class="sort-wrap">
              <text :class="['sort-item', searchForm.sortType === 'price_asc' ? 'active' : '']" @click="toggleSort('price')">价格</text>
              <text :class="['sort-item', searchForm.sortType === 'distance' ? 'active' : '']" @click="toggleSort('distance')">距离</text>
          </view>
      </view>
    </view>

    <!-- List Section -->
    <scroll-view scroll-y class="generator-list" @scrolltolower="loadMore">
      <view v-if="generators.length === 0" class="empty-state">
        <text>暂无发电机</text>
      </view>
      
      <view class="generator-card" v-for="gen in generators" :key="gen.id" @click="goToDetail(gen.id)">
        <image :src="getImageUrl(gen.imageUrl ? gen.imageUrl.split(',')[0] : '')" class="card-image" mode="aspectFill" />
        <view class="card-content">
            <view class="card-header">
                <text class="card-title">{{ gen.name }}</text>
                <text class="power-badge">{{ gen.power }}</text>
            </view>
            <view class="card-info">
                <text class="location-text">{{ gen.address || '未知地点' }}</text>
                <text v-if="gen.distance" class="distance-text">{{ formatDistance(gen.distance) }}</text>
            </view>
            <view class="card-footer">
                <view class="price-box">
                    <text class="currency">¥</text>
                    <text class="amount">{{ gen.dailyRent }}</text>
                    <text class="unit">/天</text>
                </view>
                <view class="rating-box" v-if="gen.averageRating">
                    <text class="rating-star">★</text>
                    <text class="rating-score">{{ Number(gen.averageRating).toFixed(1) }}</text>
                </view>
            </view>
        </view>
      </view>
      <view class="loading-more" v-if="loading">加载中...</view>
    </scroll-view>

    <!-- Floating Buttons (optional) -->
    <view class="fab-container">
        <button class="fab-btn" @click="goToCart">🛒</button>
        <button class="fab-btn" @click="goToOrders">📄</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { searchGenerators } from '@/api/generator'
import { getImageUrl } from '@/utils/image'

const user = ref({})
const loading = ref(false)
const generators = ref([])

const powerOptions = ['全部', '50KW', '100KW', '200KW', '500KW']
const powerIndex = ref(0)

const searchForm = reactive({
  power: '',
  location: '',
  minRent: undefined,
  maxRent: undefined,
  sortType: '',
  userLatitude: null,
  userLongitude: null
})

const loadUser = () => {
  const storedUser = uni.getStorageSync('user')
  if (storedUser) {
    try {
        user.value = JSON.parse(storedUser)
    } catch(e) {
        user.value = storedUser
    }
  }
}

const bindPowerChange = (e) => {
    const idx = e.detail.value
    powerIndex.value = idx
    searchForm.power = powerOptions[idx] === '全部' ? '' : powerOptions[idx]
    handleSearch()
}

const toggleSort = (type) => {
    if (type === 'price') {
        searchForm.sortType = searchForm.sortType === 'price_asc' ? 'price_desc' : 'price_asc'
    } else if (type === 'distance') {
        searchForm.sortType = 'distance'
        // Mock location for now, or use uni.getLocation
        searchForm.userLatitude = 39.9042
        searchForm.userLongitude = 116.4074
    }
    handleSearch()
}

const handleSearch = async () => {
  loading.value = true
  try {
    const response = await searchGenerators(searchForm)
    generators.value = response || []
  } catch (error) {
    uni.showToast({ title: '获取列表失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const formatDistance = (km) => {
  if (km < 1) return '< 1km'
  return km.toFixed(1) + 'km'
}

const goToDetail = (id) => {
  uni.navigateTo({ url: `/pages/generator/detail?id=${id}` })
}

const goToProfile = () => {
    uni.navigateTo({ url: '/pages/profile/profile' })
}

const goToCart = () => {
    uni.navigateTo({ url: '/pages/cart/cart' })
}

const goToOrders = () => {
    uni.navigateTo({ url: '/pages/tenant/orders' })
}

const handleLogout = () => {
    uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
            if (res.confirm) {
                uni.removeStorageSync('user')
                uni.removeStorageSync('token')
                uni.reLaunch({ url: '/pages/login/login' })
            }
        }
    })
}

onMounted(() => {
  loadUser()
  handleSearch()
})
</script>

<style lang="scss" scoped>
.tenant-home {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header {
  height: 88rpx;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.logo {
  font-size: 32rpx;
  font-weight: bold;
  color: #409EFF;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  border: 1px solid #eee;
}

.user-name {
    font-size: 24rpx;
    color: #666;
}

.logout-btn {
    font-size: 24rpx;
    color: #999;
}

.filter-section {
    padding: 20rpx;
    background: #fff;
    margin-bottom: 20rpx;
}

.search-bar {
    display: flex;
    align-items: center;
    gap: 20rpx;
    margin-bottom: 20rpx;
}

.picker-wrap {
    font-size: 28rpx;
    color: #333;
    min-width: 120rpx;
}

.search-input {
    flex: 1;
    height: 64rpx;
    background: #f5f7fa;
    border-radius: 32rpx;
    padding: 0 30rpx;
    font-size: 26rpx;
}

.filter-row {
    display: flex;
    align-items: center;
    gap: 20rpx;
    font-size: 26rpx;
}

.mini-input {
    width: 120rpx;
    height: 56rpx;
    background: #f5f7fa;
    border-radius: 8rpx;
    text-align: center;
    font-size: 24rpx;
}

.sort-wrap {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    gap: 30rpx;
}

.sort-item {
    color: #666;
    &.active {
        color: #409EFF;
        font-weight: bold;
    }
}

.generator-list {
    flex: 1;
    overflow: hidden;
    padding: 0 20rpx;
    box-sizing: border-box;
}

.empty-state {
    text-align: center;
    padding: 100rpx 0;
    color: #999;
}

.generator-card {
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 24rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.card-image {
    width: 100%;
    height: 300rpx;
    background: #eee;
}

.card-content {
    padding: 20rpx;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;
}

.card-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    flex: 1;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.power-badge {
    font-size: 20rpx;
    color: #fff;
    background: rgba(0,0,0,0.5);
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
    margin-left: 10rpx;
}

.card-info {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: #999;
    margin-bottom: 16rpx;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.price-box {
    color: #f56c6c;
}

.currency { font-size: 24rpx; }
.amount { font-size: 36rpx; font-weight: bold; }
.unit { font-size: 22rpx; color: #999; }

.rating-box {
    display: flex;
    align-items: center;
    gap: 4rpx;
}

.rating-star { color: #ff9900; font-size: 24rpx; }
.rating-score { color: #ff9900; font-size: 24rpx; font-weight: bold; }

.fab-container {
    position: fixed;
    right: 40rpx;
    bottom: 60rpx;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}

.fab-btn {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #409EFF;
    color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32rpx;
    box-shadow: 0 4rpx 12rpx rgba(64, 158, 255, 0.4);
    padding: 0;
    margin: 0;
}
</style>
