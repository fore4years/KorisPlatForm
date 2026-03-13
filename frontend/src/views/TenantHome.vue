<template>
  <div class="tenant-home">
    <el-container>
      <el-header class="header">
        <div class="logo">发电机租赁平台</div>
        <div class="user-info">
          <el-avatar 
            :size="32" 
            :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
            class="user-avatar"
            @click="$router.push('/profile')"
          />
          <span>欢迎，{{ user.username }}</span>
          <div class="nav-links">
            <ThemeToggle />
            <el-button v-if="user.role === 'MERCHANT'" type="primary" size="small" @click="$router.push('/merchant')">切换商家版</el-button>
            <el-button type="text" @click="$router.push('/favorites')">收藏夹</el-button>
            <el-button type="text" @click="$router.push('/cart')">购物车</el-button>
            <el-button type="text" @click="$router.push('/tenant/orders')">我的订单</el-button>
            <el-button type="text" @click="showHelp = true">帮助中心</el-button>
            <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
            <el-divider direction="vertical" />
            <el-button type="text" @click="logout" class="logout-btn">退出</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <!-- Search & Filter Section -->
        <div class="filter-section">
          <el-card class="filter-card" shadow="never">
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="功率">
                <el-select v-model="searchForm.power" placeholder="全部功率" clearable style="width: 140px">
                  <el-option label="50KW" value="50KW" />
                  <el-option label="100KW" value="100KW" />
                  <el-option label="200KW" value="200KW" />
                  <el-option label="500KW" value="500KW" />
                </el-select>
              </el-form-item>
              <el-form-item label="位置">
                <el-input v-model="searchForm.location" placeholder="输入城市或地点" clearable :prefix-icon="Location" style="width: 200px" />
              </el-form-item>
              <el-form-item label="日租金">
                <div class="rent-range">
                  <el-input-number v-model="searchForm.minRent" :min="0" placeholder="最低" controls-position="right" style="width: 110px" />
                  <span class="separator">-</span>
                  <el-input-number v-model="searchForm.maxRent" :min="0" placeholder="最高" controls-position="right" style="width: 110px" />
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- Sorting -->
            <div class="sort-bar">
              <span class="label">排序：</span>
              <el-radio-group v-model="searchForm.sortType" @change="handleSearch" size="small">
                <el-radio-button label="">默认</el-radio-button>
                <el-radio-button label="price_asc">租金↑</el-radio-button>
                <el-radio-button label="price_desc">租金↓</el-radio-button>
                <el-radio-button label="rating">好评</el-radio-button>
                <el-radio-button label="distance">距离</el-radio-button>
              </el-radio-group>
              
              <div v-if="searchForm.sortType === 'distance'" class="location-tip">
                <el-icon><Position /></el-icon>
                <span>需获取位置</span>
                <el-button type="primary" link size="small" @click="mockLocation">模拟定位(北京)</el-button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- List Section -->
        <div class="generator-list" v-loading="loading">
          <el-empty v-if="generators.length === 0" description="暂无符合条件的发电机" />
          
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="gen in generators" :key="gen.id" style="margin-bottom: 24px">
              <el-card :body-style="{ padding: '0px' }" class="generator-card" shadow="hover" @click="goToDetail(gen.id)">
                <div class="image-wrapper">
                  <img :src="getImageUrl(gen.imageUrl ? gen.imageUrl.split(',')[0] : '')" class="image" loading="lazy" />
                  <div class="power-tag">{{ gen.power }}</div>
                </div>
                <div class="card-content">
                  <div class="card-title" :title="gen.name">{{ gen.name }}</div>
                  <div class="card-price">
                    <span class="currency">¥</span>
                    <span class="amount">{{ gen.dailyRent }}</span>
                    <span class="unit">/天</span>
                  </div>
                  <div class="card-details">
                    <div class="detail-item">
                      <el-icon><Location /></el-icon>
                      <span class="truncate">{{ gen.address || '未知地点' }}</span>
                      <span v-if="gen.distance" class="distance">({{ formatDistance(gen.distance) }})</span>
                    </div>
                    <div class="detail-item rating">
                      <el-rate v-model="gen.averageRating" disabled text-color="#ff9900" size="small" />
                      <span class="rating-text" v-if="gen.averageRating !== undefined">{{ Number(gen.averageRating).toFixed(1) }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>

    <!-- Help Dialog -->
    <el-dialog v-model="showHelp" title="帮助中心 - 新手引导" width="500px">
      <div class="help-content">
        <h3>如何预订发电机？</h3>
        <p>1. 搜索：在上方搜索栏输入功率、位置或租金范围。</p>
        <p>2. 选择：点击感兴趣的发电机查看详情。</p>
        <p>3. 预订：点击"立即预订"，填写日期和地址，提交订单。</p>
        <el-divider />
        <h3>常见问题</h3>
        <p><strong>Q: 支付失败怎么办？</strong><br>A: 请检查余额或网络，如果持续失败请联系客服。</p>
        <p><strong>Q: 如何联系商家？</strong><br>A: 订单详情页有商家联系电话。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { searchGenerators } from '../api/generator'
import { ElMessage } from 'element-plus'
import { Location, Search, Position } from '@element-plus/icons-vue'
import ThemeToggle from '../components/ThemeToggle.vue'
import { getImageUrl } from '../utils/image'

const router = useRouter()
const user = ref({})
const loading = ref(false)
const showHelp = ref(false)
const generators = ref([])

const loadUser = () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }
}

const searchForm = reactive({
  power: '',
  location: '',
  minRent: undefined,
  maxRent: undefined,
  sortType: '',
  userLatitude: null,
  userLongitude: null
})

const handleSearch = async () => {
  loading.value = true
  try {
    const response = await searchGenerators(searchForm)
    generators.value = response || []
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.power = ''
  searchForm.location = ''
  searchForm.minRent = undefined
  searchForm.maxRent = undefined
  searchForm.sortType = ''
  handleSearch()
}

const mockLocation = () => {
  searchForm.userLatitude = 39.9042
  searchForm.userLongitude = 116.4074
  ElMessage.success('已模拟当前位置：北京')
  if (searchForm.sortType === 'distance') {
    handleSearch()
  }
}

const formatDistance = (km) => {
  if (km < 1) return '< 1km'
  return km.toFixed(1) + 'km'
}

const goToDetail = (id) => {
  router.push(`/generator/${id}`)
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadUser()
  handleSearch()
})
</script>

<style scoped>
.tenant-home {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 22px;
  font-weight: 700;
  color: #409EFF;
  letter-spacing: 0.5px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.user-avatar {
  cursor: pointer;
  border: 2px solid #e6f7ff; /* 浅蓝描边 */
  border-radius: 8px; /* 圆角矩形 */
  box-sizing: border-box;
  transition: all 0.3s;
}

.user-avatar:hover {
  border-color: #409EFF; /* 悬停时加深描边 */
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-links .el-button {
  color: #606266;
  font-weight: 500;
}

.nav-links .el-button:hover {
  color: #409EFF;
}

.logout-btn {
  color: #909399 !important;
}

.logout-btn:hover {
  color: #f56c6c !important;
}

.main-content {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.rent-range {
  display: flex;
  align-items: center;
}

.separator {
  margin: 0 8px;
  color: #909399;
}

.sort-bar {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-bar .label {
  font-size: 14px;
  color: #606266;
}

.location-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 16px;
  font-size: 13px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.generator-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  background: #ffffff;
}

.generator-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08) !important;
}

.image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.generator-card:hover .image {
  transform: scale(1.05);
}

.power-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.card-content {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-price {
  color: #f56c6c;
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 14px;
  font-weight: 600;
}

.amount {
  font-size: 20px;
  font-weight: 700;
  margin: 0 2px;
}

.unit {
  font-size: 12px;
  color: #909399;
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.distance {
  color: #909399;
  font-size: 12px;
}

.rating {
  margin-top: 4px;
}

.rating-text {
  margin-left: 4px;
  font-weight: 600;
  color: #ff9900;
}
</style>
