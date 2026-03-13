<template>
  <div class="order-detail" v-loading="loading">
    <el-page-header @back="$router.back()" class="page-header">
      <template #content>
        <span class="text-large font-600 mr-3"> 订单详情 </span>
      </template>
    </el-page-header>

    <div class="main-content" v-if="order">
      <!-- Status Steps -->
      <el-card class="status-card">
        <el-steps :active="activeStep" align-center finish-status="success">
          <el-step title="提交订单" description="待支付" />
          <el-step title="商家确认" description="等待接单" />
          <el-step title="签署合同" description="双方签署" />
          <el-step title="设备交付" description="物流/自提" />
          <el-step title="租赁中" description="使用设备" />
          <el-step title="完成" description="归还设备" />
        </el-steps>
      </el-card>

      <el-row :gutter="20" style="margin-top: 20px">
        <!-- Order Info -->
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>订单信息 ({{ order.id }})</span>
                <div>
                   <el-button v-if="order.status === 'WAIT_PAY' || (order.status === 'WAIT_CONFIRM' && order.paymentMethod === 'ONLINE' && order.paymentStatus === 'PENDING')" type="danger" size="small" @click="goToPay" style="margin-right: 10px">去支付</el-button>
                   <el-tag>{{ formatStatus(order.status) }}</el-tag>
                </div>
              </div>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="发电机">{{ order.generatorName }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ order.paymentMethod }}</el-descriptions-item>
              <el-descriptions-item label="支付状态">{{ order.paymentStatus }}</el-descriptions-item>
              <el-descriptions-item label="交易单号" v-if="order.transactionId">{{ order.transactionId }}</el-descriptions-item>
              <el-descriptions-item label="总金额">¥{{ order.totalAmount }}</el-descriptions-item>
              <el-descriptions-item label="交付地址">{{ order.deliveryAddress }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ order.contactName }} ({{ order.contactPhone }})</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- Delivery Info -->
          <el-card style="margin-top: 20px" v-if="order.status === 'DELIVERED' || order.status === 'RENTING'">
            <template #header>
              <div class="card-header">
                <span>交付信息</span>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="交付方式">{{ order.deliveryType === 'DELIVERY' ? '商家配送' : '自提' }}</el-descriptions-item>
              <el-descriptions-item label="验收凭证">
                <div v-if="order.deliveryEvidenceUrl">
                  <img :src="order.deliveryEvidenceUrl" style="max-width: 200px; max-height: 200px" />
                </div>
                <div v-else>暂无凭证</div>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          <!-- Repair / After Sales (Visible to All) -->
          <el-card style="margin-top: 20px" v-if="['RENTING', 'WAIT_RETURN', 'COMPLETED'].includes(order.status)">
             <template #header>
              <div class="card-header">
                <span>售后 / 报修</span>
                <el-button v-if="isTenant && order.status === 'RENTING'" type="warning" size="small" @click="showRepairModal = true">发起报修</el-button>
              </div>
            </template>
            <div v-if="repairList.length === 0">暂无记录</div>
            <div v-else>
               <el-collapse>
                  <el-collapse-item v-for="repair in repairList" :key="repair.id" :title="`报修 #${repair.id} - ${repair.status}`">
                     <p>描述: {{ repair.description }}</p>
                     <img v-if="repair.imageUrl" :src="repair.imageUrl" style="max-width: 100px" />
                     <div v-if="isMerchant && repair.status === 'PENDING'">
                        <el-button size="small" type="primary" @click="handleRepairResponse(repair, 'PROCESSING')">受理</el-button>
                     </div>
                     <div v-if="isMerchant && repair.status === 'PROCESSING'">
                        <el-button size="small" type="success" @click="handleRepairResponse(repair, 'COMPLETED')">完成</el-button>
                     </div>
                  </el-collapse-item>
               </el-collapse>
            </div>
          </el-card>

          <!-- Return Info (Visible after return confirmed) -->
          <el-card style="margin-top: 20px" v-if="order.status === 'COMPLETED'">
             <template #header>
              <div class="card-header">
                <span>归还结算</span>
              </div>
            </template>
            <el-descriptions :column="1" border>
               <el-descriptions-item label="押金">¥{{ order.depositAmount }}</el-descriptions-item>
               <el-descriptions-item label="扣除金额">¥{{ order.deductionAmount }}</el-descriptions-item>
               <el-descriptions-item label="实际退还">¥{{ order.refundAmount }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 10px; text-align: center" v-if="isTenant">
               <el-button type="primary" @click="showReviewModal = true">评价订单</el-button>
            </div>
          </el-card>
        </el-col>

        <!-- Actions -->
        <el-col :span="8">
          <!-- Payment Action -->
          <el-card v-if="order.status === 'WAIT_PAY' || (order.status === 'WAIT_CONFIRM' && order.paymentMethod === 'ONLINE' && order.paymentStatus === 'PENDING')" style="margin-bottom: 20px">
            <template #header>
              <div class="card-header">
                <span>支付订单</span>
              </div>
            </template>
            <div style="text-align: center">
              <p style="margin-bottom: 15px; color: #f56c6c; font-weight: bold; font-size: 18px">
                待支付: ¥{{ order.totalAmount }}
              </p>
              <el-button type="danger" size="large" @click="goToPay" style="width: 100%">立即支付</el-button>
            </div>
          </el-card>

          <!-- Contract Action -->
          <el-card v-if="['CONFIRMED', 'DELIVERED', 'RENTING'].includes(order.status)">
            <template #header>
              <div class="card-header">
                <span>电子合同</span>
              </div>
            </template>
            <div style="text-align: center">
              <el-button type="primary" @click="showContract = true">查看/签署合同</el-button>
            </div>
          </el-card>

          <!-- Merchant Actions -->
          <el-card style="margin-top: 20px" v-if="isMerchant">
            <template #header>
              <div class="card-header">
                <span>商家操作</span>
              </div>
            </template>
            <div class="action-buttons">
              <!-- Show Confirm button only if WAIT_CONFIRM -->
              <el-button 
                v-if="order.status === 'WAIT_CONFIRM'" 
                type="success" 
                @click="handleConfirm" 
                style="width: 100%; margin-bottom: 10px"
              >
                确认订单
              </el-button>
              
              <!-- Show Delivery options only if CONFIRMED (Contract signed phase) -->
              <div v-if="order.status === 'CONFIRMED'">
                <p>选择交付方式：</p>
                <el-radio-group v-model="deliveryType" style="margin-bottom: 10px">
                  <el-radio label="DELIVERY">配送</el-radio>
                  <el-radio label="PICKUP">自提</el-radio>
                </el-radio-group>
                <el-button type="primary" @click="handleDelivery" style="width: 100%">确认发货/待自提</el-button>
              </div>

              <!-- Show Evidence upload if DELIVERED (Waiting for final confirmation) -->
              <div v-if="order.status === 'DELIVERED'">
                <el-input v-model="evidenceUrl" placeholder="输入验收照片URL (模拟上传)" style="margin-bottom: 10px" />
                <el-button type="primary" @click="handleEvidence" style="width: 100%">上传凭证并确认交付</el-button>
              </div>

              <!-- End Rental / Force Complete (Merchant Only) -->
              <div v-if="order.status === 'RENTING'">
                 <el-button type="danger" @click="showReturnModal = true" style="width: 100%; margin-top: 10px">结束租赁 / 确认归还</el-button>
              </div>
            </div>
          </el-card>

           <!-- Tenant Actions -->
           <el-card style="margin-top: 20px" v-if="isTenant && order.status === 'DELIVERED'">
            <template #header>
              <div class="card-header">
                <span>租户操作</span>
              </div>
            </template>
             <div class="action-buttons">
                <el-input v-model="evidenceUrl" placeholder="输入验收照片URL (模拟上传)" style="margin-bottom: 10px" />
                <el-button type="primary" @click="handleEvidence" style="width: 100%">上传凭证并确认收货</el-button>
             </div>
           </el-card>
        </el-col>
      </el-row>
    </div>

    <ContractModal 
      v-model="showContract" 
      :orderId="order?.id" 
      :userRole="userRole" 
      :userId="user.userid"
      @signed="refreshOrder"
    />

    <!-- Repair Modal -->
    <el-dialog v-model="showRepairModal" title="发起报修">
       <el-form :model="repairForm">
          <el-form-item label="故障描述">
             <el-input v-model="repairForm.description" type="textarea" />
          </el-form-item>
          <el-form-item label="图片URL">
             <el-input v-model="repairForm.imageUrl" placeholder="http://..." />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="showRepairModal = false">取消</el-button>
          <el-button type="primary" @click="handleRepairSubmit">提交</el-button>
       </template>
    </el-dialog>

    <!-- Return Modal -->
    <el-dialog v-model="showReturnModal" title="确认归还 & 结算">
       <el-form :model="returnForm">
          <el-form-item label="扣除金额(损坏赔偿)">
             <el-input-number v-model="returnForm.deduction" :min="0" :max="order?.depositAmount" />
          </el-form-item>
          <el-form-item label="备注">
             <el-input v-model="returnForm.comment" type="textarea" />
          </el-form-item>
          <p>实际退还押金: ¥{{ (order?.depositAmount - returnForm.deduction).toFixed(2) }}</p>
       </el-form>
       <template #footer>
          <el-button @click="showReturnModal = false">取消</el-button>
          <el-button type="primary" @click="handleReturnSubmit">确认归还</el-button>
       </template>
    </el-dialog>

    <!-- Review Modal -->
    <el-dialog v-model="showReviewModal" title="评价订单">
       <el-form :model="reviewForm">
          <el-form-item label="评分">
             <el-rate v-model="reviewForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
             <el-input v-model="reviewForm.comment" type="textarea" />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="showReviewModal = false">取消</el-button>
          <el-button type="primary" @click="handleReviewSubmit">提交评价</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail, confirmOrder, updateDelivery, uploadEvidence, completeOrder, confirmReturn } from '../api/order'
import { createRepair, getRepairsByOrder, updateRepairStatus } from '../api/repair'
import { createReview } from '../api/review'
import ContractModal from '../components/ContractModal.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const order = ref(null)
const showContract = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')
const userRole = user.role // 'TENANT' or 'MERCHANT'

const isMerchant = computed(() => userRole === 'MERCHANT')
const isTenant = computed(() => userRole === 'TENANT')

const deliveryType = ref('DELIVERY')
const evidenceUrl = ref('')
const repairList = ref([])
const showRepairModal = ref(false)
const repairForm = ref({ description: '', imageUrl: '' })
const showReturnModal = ref(false)
const returnForm = ref({ deduction: 0, comment: '' })
const showReviewModal = ref(false)
const reviewForm = ref({ rating: 5, comment: '' })

const activeStep = computed(() => {
  if (!order.value) return 0
  // Handle old data or edge case where status is WAIT_CONFIRM but not paid
  if (order.value.status === 'WAIT_CONFIRM' && order.value.paymentMethod === 'ONLINE' && order.value.paymentStatus === 'PENDING') {
      return 0
  }

  switch (order.value.status) {
    case 'WAIT_PAY': return 0 // Step 0 active (First step)
    case 'WAIT_CONFIRM': return 1 // Step 1 active (Second step)
    case 'CONFIRMED': return 2
    case 'DELIVERED': return 3
    case 'RENTING': return 4
    case 'WAIT_RETURN': return 4 
    case 'COMPLETED': return 6
    default: return 0
  }
})

const goToPay = () => {
  router.push({
    path: '/payment',
    query: {
      orderId: order.value.id,
      amount: order.value.totalAmount
    }
  })
}

const formatStatus = (status) => {
  if (status === 'WAIT_CONFIRM' && order.value?.paymentMethod === 'ONLINE' && order.value?.paymentStatus === 'PENDING') {
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

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res
    // Check expiry
    checkExpiryReminder()
    // Load repairs
    loadRepairs()
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadRepairs = async () => {
  try {
    const res = await getRepairsByOrder(route.params.id)
    repairList.value = res
  } catch (error) {
    console.error(error)
  }
}

const checkExpiryReminder = () => {
  if (isTenant.value && order.value.status === 'RENTING') {
    const end = new Date(order.value.endTime)
    const now = new Date()
    const diff = end - now
    const days = diff / (1000 * 60 * 60 * 24)
    if (days > 0 && days <= 3) {
      ElMessage.warning(`您的租赁将在 ${Math.ceil(days)} 天后到期，请及时归还设备`)
    }
  }
}

const handleRepairSubmit = async () => {
  if (!repairForm.value.description) return
  try {
    await createRepair({
      orderId: order.value.id,
      ...repairForm.value
    })
    ElMessage.success('报修申请已提交')
    showRepairModal.value = false
    loadRepairs()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleRepairResponse = async (repair, status) => {
  try {
    await updateRepairStatus(repair.id, { status, response: '已处理' })
    ElMessage.success('状态已更新')
    loadRepairs()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleReturnSubmit = async () => {
  try {
    await confirmReturn(order.value.id, returnForm.value)
    ElMessage.success('归还确认成功，押金已退还')
    showReturnModal.value = false
    loadOrder()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleReviewSubmit = async () => {
  try {
    await createReview({
      orderId: order.value.id,
      ...reviewForm.value
    })
    ElMessage.success('评价成功')
    showReviewModal.value = false
  } catch (error) {
    ElMessage.error('评价失败')
  }
}


const handleConfirm = async () => {
  try {
    await confirmOrder(order.value.id)
    ElMessage.success('订单已确认，合同已生成')
    loadOrder()
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const handleDelivery = async () => {
  try {
    await updateDelivery(order.value.id, deliveryType.value)
    ElMessage.success('交付状态已更新')
    loadOrder()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleEvidence = async () => {
  if (!evidenceUrl.value) {
    ElMessage.warning('请输入凭证URL')
    return
  }
  try {
    await uploadEvidence(order.value.id, evidenceUrl.value)
    ElMessage.success('凭证上传成功，租赁开始')
    loadOrder()
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleComplete = async () => {
  ElMessageBox.confirm(
    '确定要结束租赁并确认归还吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await completeOrder(order.value.id)
        ElMessage.success('订单已完成')
        loadOrder()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const refreshOrder = () => {
  loadOrder()
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.main-content {
  max-width: 1200px;
  margin: 0 auto;
}
.status-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
