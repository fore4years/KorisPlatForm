<template>
  <div class="cart-page">
    <el-page-header @back="$router.back()" class="page-header" title="返回">
      <template #content>
        <span class="header-title">购物车</span>
      </template>
    </el-page-header>

    <div class="main-content">
      <el-card class="cart-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>全部商品 ({{ cartItems.length }})</span>
            <el-button type="danger" link :disabled="selectedItems.length === 0" @click="handleBatchDelete">删除选中</el-button>
          </div>
        </template>

        <el-table :data="cartItems" v-loading="loading" @selection-change="handleSelectionChange" class="cart-table">
          <el-table-column type="selection" width="55" align="center" />
          
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <div class="product-info">
                <img :src="getImageUrl(scope.row.generator.imageUrl ? scope.row.generator.imageUrl.split(',')[0] : '')" class="product-img" />
                <div class="product-details">
                  <div class="product-name">{{ scope.row.generator.name }}</div>
                  <div class="product-merchant">
                    <el-icon><Shop /></el-icon> {{ scope.row.generator.merchant?.name || 'Unknown' }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="规格" width="200" align="center">
              <template #default="scope">
                  <div class="spec-tags">
                    <el-tag size="small" type="info">{{ scope.row.leaseDuration }}天</el-tag>
                    <el-tag size="small" type="info" style="margin-left: 5px">{{ scope.row.deliveryType === 'DELIVERY' ? '商家配送' : '自提' }}</el-tag>
                  </div>
              </template>
          </el-table-column>

          <el-table-column label="租金估算" width="150" align="right">
              <template #default="scope">
                  <span class="price">¥{{ calculatePrice(scope.row) }}</span>
              </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div class="cart-footer" v-if="cartItems.length > 0">
        <div class="footer-left">
          <span class="selected-count">已选 {{ selectedItems.length }} 件</span>
        </div>
        <div class="footer-right">
          <div class="total-info">
            <span class="label">合计：</span>
            <span class="total-price">¥{{ totalPrice }}</span>
          </div>
          <el-button type="primary" size="large" class="checkout-btn" :disabled="selectedItems.length === 0" @click="handleCheckout">去结算</el-button>
        </div>
      </div>
    </div>

    <!-- Checkout Modal -->
    <el-dialog v-model="checkoutVisible" title="确认订单" width="500px" center>
        <el-form :model="checkoutForm" label-width="100px" class="checkout-form">
            <el-form-item label="租赁开始" required>
                <el-date-picker v-model="checkoutForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
            </el-form-item>
            
            <el-form-item label="交付地点" required>
                <el-input v-model="checkoutForm.deliveryAddress" placeholder="请输入详细交付地址" />
            </el-form-item>

            <el-form-item label="联系人" required>
                <el-input v-model="checkoutForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>

            <el-form-item label="联系电话" required>
                <el-input v-model="checkoutForm.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>

            <el-form-item label="支付方式" required>
                <el-radio-group v-model="checkoutForm.paymentMethod">
                  <el-radio label="ONLINE" border>在线支付</el-radio>
                  <el-radio label="OFFLINE" border>线下支付</el-radio>
                </el-radio-group>
            </el-form-item>

            <el-alert title="注意：所有商品将使用相同的开始时间，并按各自的租赁时长计算结束时间。" type="warning" :closable="false" show-icon style="margin-top: 10px" />
        </el-form>
        <template #footer>
            <el-button @click="checkoutVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmCheckout" :loading="submitting">确认下单</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCart, deleteCartItem, batchDeleteCartItems } from '../api/cart'
import { createOrder } from '../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Shop } from '@element-plus/icons-vue'
import { getImageUrl } from '../utils/image'

const router = useRouter()
const loading = ref(false)
const cartItems = ref([])
const selectedItems = ref([])
const checkoutVisible = ref(false)
const submitting = ref(false)

const checkoutForm = ref({
    startTime: null,
    deliveryAddress: '',
    contactName: '',
    contactPhone: '',
    paymentMethod: 'ONLINE'
})

const loadCart = async () => {
  loading.value = true
  try {
    const res = await getCart()
    cartItems.value = res
  } catch (error) {
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

const calculatePrice = (item) => {
    let total = item.generator.dailyRent * item.leaseDuration
    if (item.deliveryType === 'DELIVERY') {
        total += (item.generator.deliveryFee || 0)
    }
    return total
}

const handleSelectionChange = (val) => {
  selectedItems.value = val
}

const totalPrice = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + calculatePrice(item), 0)
})

const handleDelete = async (id) => {
    try {
        await deleteCartItem(id)
        ElMessage.success('删除成功')
        loadCart()
    } catch (error) {
        ElMessage.error('删除失败')
    }
}

const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm('确认删除选中商品?', '提示', { type: 'warning' })
        const ids = selectedItems.value.map(item => item.id)
        await batchDeleteCartItems(ids)
        ElMessage.success('删除成功')
        loadCart()
    } catch (error) {
        if (error !== 'cancel') ElMessage.error('删除失败')
    }
}

const handleCheckout = () => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    checkoutForm.value = {
        startTime: null,
        deliveryAddress: user.address || '',
        contactName: user.name || '',
        contactPhone: user.phone || '',
        paymentMethod: 'ONLINE'
    }
    checkoutVisible.value = true
}

const confirmCheckout = async () => {
    if (!checkoutForm.value.startTime || !checkoutForm.value.deliveryAddress || !checkoutForm.value.contactName || !checkoutForm.value.contactPhone) {
        ElMessage.warning('请填写完整订单信息')
        return
    }

    submitting.value = true
    try {
        const createdOrders = []
        for (const item of selectedItems.value) {
            const end = new Date(checkoutForm.value.startTime)
            end.setDate(end.getDate() + item.leaseDuration)
            
            const res = await createOrder({
                generatorId: item.generator.id,
                startTime: checkoutForm.value.startTime,
                endTime: end,
                deliveryType: item.deliveryType,
                contactName: checkoutForm.value.contactName, 
                contactPhone: checkoutForm.value.contactPhone,
                deliveryAddress: checkoutForm.value.deliveryAddress,
                paymentMethod: checkoutForm.value.paymentMethod
            })
            createdOrders.push(res)
            await deleteCartItem(item.id)
        }
        
        ElMessage.success('下单成功')
        checkoutVisible.value = false
        
        if (checkoutForm.value.paymentMethod === 'ONLINE') {
            if (createdOrders.length === 1) {
                // Single order, go to payment directly
                router.push({
                    path: '/payment',
                    query: {
                        orderId: createdOrders[0].id,
                        amount: createdOrders[0].totalAmount
                    }
                })
            } else {
                // Multiple orders, go to order list with hint
                ElMessage.info('请在订单列表中完成支付')
                router.push({ path: '/tenant/orders', query: { tab: 'WAIT_PAY' } })
            }
        } else {
            router.push('/tenant/orders')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('下单失败')
    } finally {
        submitting.value = false
    }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-page {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 80px; /* Space for footer */
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
  max-width: 1000px;
  margin: 0 auto;
}
.cart-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-info {
  display: flex;
  align-items: center;
}
.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 16px;
  border-radius: 6px;
  border: 1px solid #f0f2f5;
}
.product-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.product-name {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}
.product-merchant {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
.price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 16px;
}
.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.total-info {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.total-info .label {
  font-size: 14px;
  color: #606266;
}
.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}
.checkout-btn {
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
}
.selected-count {
  color: #606266;
  font-size: 14px;
}
</style>
