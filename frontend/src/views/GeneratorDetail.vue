<template>
  <div class="generator-detail" v-loading="loading">
    <el-page-header @back="goBack" class="page-header" title="返回">
      <template #content>
        <span class="header-title">设备详情</span>
      </template>
    </el-page-header>

    <div class="main-content" v-if="generator">
      <el-row :gutter="24">
        <!-- Left: Image & Basic Info -->
        <el-col :xs="24" :md="16">
          <div class="detail-card">
            <div class="image-section">
              <el-carousel
                v-if="generator.imageUrl && generator.imageUrl.includes(',')"
                trigger="click"
                height="400px"
                :autoplay="true"
                :interval="3000"
              >
                <el-carousel-item v-for="(img, index) in generator.imageUrl.split(',')" :key="index">
                  <el-image
                    :src="img"
                    class="detail-image"
                    fit="cover"
                    :preview-src-list="generator.imageUrl.split(',')"
                    :initial-index="index"
                    preview-teleported
                  />
                </el-carousel-item>
              </el-carousel>
              <el-image
                v-else
                :src="getImageUrl(generator.imageUrl)"
                class="detail-image"
                fit="cover"
                :preview-src-list="generator.imageUrl ? [getImageUrl(generator.imageUrl)] : []"
                preview-teleported
              />

              <div class="status-badge" :class="generator.stockStatus === 'AVAILABLE' ? 'available' : 'unavailable'">
                {{ generator.stockStatus === 'AVAILABLE' ? '可租赁' : '不可租' }}
              </div>
            </div>

            <div class="info-section">
              <div class="title-row">
                <h1 class="product-title">{{ generator.name }}</h1>
                <div class="action-buttons">
                  <el-button
                    :type="isFavorited ? 'danger' : 'default'"
                    :icon="isFavorited ? StarFilled : Star"
                    circle
                    @click="toggleFavoriteHandler"
                    class="fav-btn"
                  />
                </div>
              </div>

              <div class="price-grid">
                <div class="price-box daily">
                  <div class="label">日租金</div>
                  <div class="amount">¥{{ generator.dailyRent }}</div>
                </div>
                <div class="price-box weekly" v-if="generator.weeklyRent">
                  <div class="label">周租金</div>
                  <div class="amount">¥{{ generator.weeklyRent }}</div>
                </div>
                <div class="price-box monthly" v-if="generator.monthlyRent">
                  <div class="label">月租金</div>
                  <div class="amount">¥{{ generator.monthlyRent }}</div>
                </div>
              </div>

              <div class="deposit-tag">
                <el-icon><Money /></el-icon>
                押金：¥{{ generator.deposit }}
              </div>
            </div>
          </div>

          <!-- Parameters -->
          <div class="detail-card mt-4">
            <h3 class="section-title">设备参数</h3>
            <div class="params-grid">
              <div class="param-item">
                <span class="label">功率</span>
                <span class="value">{{ generator.power }}</span>
              </div>
              <div class="param-item">
                <span class="label">油耗</span>
                <span class="value">{{ generator.fuelConsumption }}</span>
              </div>
              <div class="param-item">
                <span class="label">重量</span>
                <span class="value">{{ generator.weight }}</span>
              </div>
              <div class="param-item">
                <span class="label">尺寸</span>
                <span class="value">{{ generator.size }}</span>
              </div>
              <div class="param-item">
                <span class="label">配送方式</span>
                <span class="value">{{ generator.deliveryMethod }}</span>
              </div>
              <div class="param-item">
                <span class="label">交付地点</span>
                <span class="value">{{ generator.address }}</span>
              </div>
            </div>
          </div>

          <!-- Reviews -->
          <div class="detail-card mt-4">
            <div class="review-header">
              <h3 class="section-title">用户评价 ({{ generator.reviews ? generator.reviews.length : 0 }})</h3>
              <el-rate v-model="generator.averageRating" disabled show-score text-color="#ff9900" />
            </div>

            <div class="review-list" v-if="generator.reviews && generator.reviews.length > 0">
              <div v-for="(review, index) in generator.reviews" :key="index" class="review-item">
                <div class="review-user">
                  <el-avatar :size="32" :src="review.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <div class="user-meta">
                    <div class="username">{{ review.username }}</div>
                    <el-rate v-model="review.rating" disabled size="small" />
                  </div>
                  <div class="review-date">{{ review.createdAt }}</div>
                </div>
                <div class="review-text">{{ review.comment }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无评价" :image-size="100" />
          </div>
        </el-col>

        <!-- Right: Action & Merchant -->
        <el-col :xs="24" :md="8">
          <div class="sticky-sidebar">
            <!-- Merchant Info -->
            <div class="detail-card merchant-card">
              <div class="merchant-profile">
                <el-avatar :size="50" shape="square" src="https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png" />
                <div class="merchant-text">
                  <div class="merchant-name">{{ generator.merchant?.name }}</div>
                  <div class="merchant-badges">
                    <el-tag size="small" type="success" effect="plain">认证商家</el-tag>
                  </div>
                </div>
              </div>
              <div class="contact-info">
                <el-button type="primary" plain class="w-full" @click="contactMerchant" :icon="Phone">联系商家</el-button>
              </div>
            </div>

            <!-- Booking Action -->
            <div class="detail-card action-card mt-4">
              <div class="action-form">
                <div class="form-group">
                  <div class="label">租赁天数</div>
                  <el-input-number v-model="leaseDuration" :min="1" :max="365" class="w-full" />
                </div>
                <div class="form-group">
                  <div class="label">配送方式</div>
                  <el-radio-group v-model="deliveryType" class="w-full">
                    <el-radio-button label="DELIVERY">商家配送</el-radio-button>
                    <el-radio-button label="PICKUP">自提</el-radio-button>
                  </el-radio-group>
                </div>
              </div>

              <div class="action-buttons-group">
                <el-button type="warning" size="large" class="flex-1" :disabled="generator.stockStatus !== 'AVAILABLE'" @click="addToCartHandler" :icon="ShoppingCart">
                  {{ cartCount > 0 ? `加入购物车 (已有${cartCount})` : '加入购物车' }}
                </el-button>
                <el-button type="danger" size="large" class="flex-1" :disabled="generator.stockStatus !== 'AVAILABLE'" @click="goToBooking">
                  立即预订
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGeneratorDetail } from '../api/generator'
import { addToCart, getCart } from '../api/cart'
import { toggleFavorite, getFavorites } from '../api/favorite'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, Phone, ShoppingCart, Money } from '@element-plus/icons-vue'
import {getImageUrl} from "@/utils/image.js";

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const generator = ref(null)
const leaseDuration = ref(1)
const deliveryType = ref('DELIVERY')
const isFavorited = ref(false)
const cartCount = ref(0)

const goBack = () => {
  router.back()
}

const loadDetail = async () => {
  loading.value = true
  try {
    const response = await getGeneratorDetail(route.params.id)
    generator.value = response
    checkFavorite()
    loadCartCount()
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const loadCartCount = async () => {
    try {
        const res = await getCart()
        if (generator.value) {
            const items = res.filter(item => item.generator.id === generator.value.id)
            cartCount.value = items.length
        }
    } catch (e) {
        // 静默失败，不影响主流程
    }
}

const checkFavorite = async () => {
    try {
        const res = await getFavorites()
        isFavorited.value = res.some(f => f.generator.id === generator.value.id)
    } catch (e) {}
}

const contactMerchant = () => {
  ElMessage.info(`商家电话：${generator.value.merchant?.phone || '暂无'}`)
}

const goToBooking = () => {
  router.push(`/booking/${generator.value.id}`)
}

const addToCartHandler = async () => {
    try {
        await addToCart({
            generatorId: generator.value.id,
            leaseDuration: leaseDuration.value,
            deliveryType: deliveryType.value
        })
        ElMessage.success('已加入购物车')
        loadCartCount()
    } catch (error) {
        ElMessage.error('加入购物车失败')
    }
}

const toggleFavoriteHandler = async () => {
    try {
        await toggleFavorite(generator.value.id)
        isFavorited.value = !isFavorited.value
        ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.generator-detail {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 24px;
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
  max-width: 1200px;
  margin: 0 auto;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 24px;
}

.mt-4 {
  margin-top: 24px;
}

.image-section {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
}

.detail-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  display: block;
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  backdrop-filter: blur(4px);
}

.status-badge.available {
  background: rgba(103, 194, 58, 0.9);
  color: #fff;
}

.status-badge.unavailable {
  background: rgba(144, 147, 153, 0.9);
  color: #fff;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}

.price-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.price-box {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  border: 1px solid #ebeef5;
}

.price-box.daily {
  background: #ecf5ff;
  border-color: #d9ecff;
}

.price-box .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.price-box .amount {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.price-box.daily .amount {
  color: #409EFF;
}

.deposit-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  background: #fdf6ec;
  padding: 8px 12px;
  border-radius: 4px;
  width: fit-content;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-left: 12px;
  border-left: 4px solid #409EFF;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.param-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.param-item .label {
  color: #909399;
}

.param-item .value {
  color: #303133;
  font-weight: 500;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f2f5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-meta {
  flex: 1;
}

.username {
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 2px;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.review-text {
  color: #606266;
  line-height: 1.6;
  padding-left: 44px;
}

.sticky-sidebar {
  position: sticky;
  top: 100px;
}

.merchant-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.merchant-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 4px;
}

.w-full {
  width: 100%;
}

.form-group {
  margin-bottom: 16px;
}

.form-group .label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.action-buttons-group {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.flex-1 {
  flex: 1;
}

@media (max-width: 768px) {
  .price-grid {
    grid-template-columns: 1fr;
  }
  .params-grid {
    grid-template-columns: 1fr;
  }
}
</style>
