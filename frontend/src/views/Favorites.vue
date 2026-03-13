<template>
  <div class="favorites-page">
    <el-page-header @back="$router.back()" class="page-header" title="返回">
      <template #content>
        <span class="header-title">我的收藏</span>
      </template>
    </el-page-header>

    <div class="main-content">
      <el-card class="favorites-container" shadow="never">
        <el-empty v-if="favorites.length === 0" description="暂无收藏商品" :image-size="120" />

        <div class="favorites-grid" v-else>
          <el-row :gutter="24">
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="fav in favorites" :key="fav.id" style="margin-bottom: 24px">
                <el-card :body-style="{ padding: '0px' }" class="fav-card" shadow="hover" @click="goToDetail(fav.generator.id)">
                  <div class="image-wrapper">
                    <img :src="fav.generator.imageUrl || 'https://via.placeholder.com/300x200'" class="image" loading="lazy" />
                    <div class="hover-overlay">
                      <el-button type="primary" size="small" @click.stop="goToDetail(fav.generator.id)">查看详情</el-button>
                    </div>
                  </div>
                  <div class="card-content">
                    <div class="card-title" :title="fav.generator.name">{{ fav.generator.name }}</div>
                    <div class="card-meta">
                      <div class="price">
                        <span class="currency">¥</span>
                        <span class="amount">{{ fav.generator.dailyRent }}</span>
                        <span class="unit">/天</span>
                      </div>
                      <div class="date">{{ formatDate(fav.createdAt) }}</div>
                    </div>
                    <div class="card-actions">
                      <el-button type="danger" link size="small" @click.stop="handleRemove(fav.generator.id)" icon="Delete">取消收藏</el-button>
                    </div>
                  </div>
                </el-card>
              </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFavorites, toggleFavorite } from '../api/favorite'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const favorites = ref([])

const loadFavorites = async () => {
  try {
    const res = await getFavorites()
    favorites.value = res
  } catch (error) {
    ElMessage.error('加载收藏失败')
  }
}

const handleRemove = async (generatorId) => {
    try {
        await toggleFavorite(generatorId)
        ElMessage.success('已取消收藏')
        loadFavorites()
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

const goToDetail = (id) => {
    router.push(`/generator/${id}`)
}

const formatDate = (date) => {
    return new Date(date).toLocaleDateString()
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.header-title {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}
.main-content {
  max-width: 1200px;
  margin: 24px auto;
}
.favorites-container {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  min-height: 400px;
}
.fav-card {
  border-radius: 12px;
  overflow: hidden;
  border: none;
  background: #fff;
  transition: all 0.3s;
}
.fav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08) !important;
}
.image-wrapper {
  position: relative;
  height: 180px;
  overflow: hidden;
}
.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}
.fav-card:hover .image {
  transform: scale(1.05);
}
.hover-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.fav-card:hover .hover-overlay {
  opacity: 1;
}
.card-content {
  padding: 16px;
}
.card-title {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;
}
.price {
  color: #f56c6c;
}
.currency {
  font-size: 12px;
  font-weight: 600;
}
.amount {
  font-size: 18px;
  font-weight: 700;
}
.unit {
  font-size: 12px;
  color: #909399;
}
.date {
  font-size: 12px;
  color: #c0c4cc;
}
.card-actions {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f5f7fa;
  padding-top: 12px;
}
</style>
