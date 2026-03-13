<template>
  <div class="tenant-orders">
    <el-page-header @back="$router.back()" class="page-header" title="返回">
      <template #content>
        <span class="header-title">我的订单</span>
      </template>
    </el-page-header>

    <div class="main-content">
      <el-card class="order-card" shadow="never">
        <el-tabs v-model="activeTab" class="order-tabs">
            <el-tab-pane label="全部" name="ALL"></el-tab-pane>
            <el-tab-pane label="待支付" name="WAIT_PAY"></el-tab-pane>
            <el-tab-pane label="待确认" name="WAIT_CONFIRM"></el-tab-pane>
            <el-tab-pane label="待收货" name="DELIVERED"></el-tab-pane>
            <el-tab-pane label="租赁中" name="RENTING"></el-tab-pane>
            <el-tab-pane label="已完成" name="COMPLETED"></el-tab-pane>
            <el-tab-pane label="已取消" name="CANCELLED"></el-tab-pane>
        </el-tabs>

        <el-table :data="filteredOrders" v-loading="loading" style="width: 100%" class="order-table">
          <el-table-column prop="id" label="订单号" width="100" align="center" />
          <el-table-column prop="generatorName" label="租赁设备" min-width="150">
             <template #default="scope">
               <div class="product-name">{{ scope.row.generatorName }}</div>
             </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
             <template #default="scope">
               <span class="price">¥{{ scope.row.totalAmount }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status, scope.row)" effect="light" size="small">{{ formatStatus(scope.row.status, scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="下单时间" width="180" align="center">
             <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="viewOrder(scope.row.id)">详情</el-button>
              <el-button 
                v-if="scope.row.status === 'WAIT_PAY' || (scope.row.status === 'WAIT_CONFIRM' && scope.row.paymentMethod === 'ONLINE' && scope.row.paymentStatus === 'PENDING')"
                link
                type="danger"
                size="small"
                @click="goToPay(scope.row)"
              >去支付</el-button>
              <el-button 
                v-if="scope.row.status === 'DELIVERED'" 
                link 
                type="success" 
                size="small"
                @click="handleConfirmReceipt(scope.row.id)"
              >确认收货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getTenantOrders, confirmReceipt } from '../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const orders = ref([])
const loading = ref(false)
const activeTab = ref('ALL')

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getTenantOrders(user.userid)
    orders.value = res
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
    if (activeTab.value === 'ALL') return orders.value
    if (activeTab.value === 'WAIT_PAY') {
        return orders.value.filter(o => o.status === 'WAIT_PAY' || (o.status === 'WAIT_CONFIRM' && o.paymentMethod === 'ONLINE' && o.paymentStatus === 'PENDING'))
    }
    return orders.value.filter(o => o.status === activeTab.value)
})

const handleConfirmReceipt = (id) => {
    ElMessageBox.confirm('确认已收到设备?', '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
    }).then(async () => {
        try {
            await confirmReceipt(id)
            ElMessage.success('已确认收货')
            loadOrders()
        } catch (error) {
            ElMessage.error('操作失败')
        }
    }).catch(() => {})
}

const viewOrder = (orderId) => {
  router.push(`/order/${orderId}`)
}

const goToPay = (order) => {
  router.push({
    path: '/payment',
    query: {
      orderId: order.id,
      amount: order.totalAmount
    }
  })
}

const getStatusType = (status, order) => {
  if (status === 'WAIT_CONFIRM' && order?.paymentMethod === 'ONLINE' && order?.paymentStatus === 'PENDING') {
      return 'danger'
  }
  switch (status) {
    case 'WAIT_PAY': return 'danger'
    case 'WAIT_CONFIRM': return 'warning'
    case 'CONFIRMED': return 'primary'
    case 'DELIVERED': return 'info'
    case 'RENTING': return 'success'
    case 'COMPLETED': return 'success'
    default: return 'info'
  }
}

const formatStatus = (status, order) => {
  if (status === 'WAIT_CONFIRM' && order?.paymentMethod === 'ONLINE' && order?.paymentStatus === 'PENDING') {
      return '待支付'
  }
  const map = {
    'WAIT_PAY': '待支付',
    'WAIT_CONFIRM': '待确认',
    'CONFIRMED': '已确认',
    'DELIVERED': '交付中',
    'RENTING': '租赁中',
    'WAIT_RETURN': '待归还',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.tenant-orders {
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
.order-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.order-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f2f5;
}
.order-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  height: 48px;
  line-height: 48px;
}
.product-name {
  font-weight: 500;
  color: #303133;
}
.price {
  font-weight: 600;
  color: #303133;
}
</style>
