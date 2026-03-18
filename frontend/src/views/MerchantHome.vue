<template>
  <div class="merchant-home">
    <el-container>
      <el-header class="header">
        <div class="logo">商家管理后台</div>
        <div class="user-info">
          <ThemeToggle />
          <el-button type="primary" size="small" @click="$router.push('/tenant')">切换用户版</el-button>
          <el-avatar
            :size="32"
            :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            class="user-avatar"
            @click="$router.push('/profile')"
          />
          <span>{{ user.name }} ({{ user.username }})</span>
          <el-button type="text" @click="logout" class="logout-btn">退出</el-button>
        </div>
      </el-header>

      <el-main class="main-content">
        <el-card class="main-card" shadow="never">
          <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="merchant-tabs">
            <!-- Device Management Tab -->
            <el-tab-pane label="设备管理" name="devices">
              <div class="tab-actions">
                <el-button type="primary" @click="openDeviceModal(null)" :icon="Plus">发布新设备</el-button>
              </div>
              <el-table :data="devices" v-loading="loadingDevices" style="width: 100%" class="data-table">
                <el-table-column prop="id" label="ID" width="80" align="center" />
                <el-table-column label="图片" width="100" align="center">
                  <template #default="scope">
                    <el-image
                      :src="scope.row.imageUrl ? scope.row.imageUrl.split(',')[0] : 'https://via.placeholder.com/50'"
                      style="width: 50px; height: 50px; border-radius: 4px"
                      fit="cover"
                      loading="lazy"
                      :preview-src-list="scope.row.imageUrl ? scope.row.imageUrl.split(',') : []"
                      preview-teleported
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="name" label="设备名称" min-width="150" />
                <el-table-column prop="power" label="功率" width="100" />
                <el-table-column label="日租金" width="120">
                  <template #default="scope">
                    <span class="price">¥{{ scope.row.dailyRent }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="库存状态" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="getStockStatusType(scope.row.stockStatus)" effect="light" size="small">
                      {{ formatStockStatus(scope.row.stockStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="审核状态" width="120" align="center">
                  <template #default="scope">
                    <el-tooltip v-if="scope.row.auditStatus === 'REJECTED'" :content="scope.row.rejectionReason" placement="top">
                      <el-tag type="danger" effect="light" size="small">
                        已驳回 <el-icon><InfoFilled /></el-icon>
                      </el-tag>
                    </el-tooltip>
                    <el-tag v-else :type="getAuditStatusType(scope.row.auditStatus)" effect="light" size="small">
                      {{ formatAuditStatus(scope.row.auditStatus) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180" align="center">
                  <template #default="scope">
                    <el-button size="small" link type="primary" @click="openDeviceModal(scope.row)">编辑</el-button>
                    <el-popconfirm title="确定要下架删除吗？" @confirm="handleDelete(scope.row.id)">
                      <template #reference>
                         <el-button size="small" link type="danger">下架</el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- Order Management Tab -->
            <el-tab-pane label="订单管理" name="orders">
               <el-table :data="orders" v-loading="loadingOrders" style="width: 100%" class="data-table">
                <el-table-column prop="id" label="订单号" width="100" align="center" />
                <el-table-column prop="generatorName" label="租赁设备" min-width="150" />
                <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
                   <template #default="scope">
                     <span class="price">¥{{ scope.row.totalAmount }}</span>
                   </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)" effect="light" size="small">{{ formatStatus(scope.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="下单时间" width="180" align="center">
                   <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center">
                  <template #default="scope">
                    <el-button size="small" link type="primary" @click="viewOrder(scope.row.id)">查看详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- Customer Management Tab -->
            <el-tab-pane label="客户管理" name="customers">
              <div class="tab-actions">
                <el-input
                  v-model="searchCustomer"
                  placeholder="搜索客户姓名/手机号"
                  style="width: 250px"
                  clearable
                  :prefix-icon="Search"
                />
              </div>
              <el-table :data="filterCustomers" v-loading="loadingCustomers" style="width: 100%" class="data-table">
                <el-table-column label="客户账号" min-width="120">
                  <template #default="scope">
                    <span>{{ scope.row.username }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="name" label="真实姓名" width="120" align="center">
                  <template #default="scope">{{ scope.row.name || '-' }}</template>
                </el-table-column>
                <el-table-column prop="phone" label="联系电话" width="130" align="center" />
                <el-table-column prop="totalOrders" label="累计订单" width="100" align="center" sortable />
                <el-table-column prop="totalAmount" label="累计金额" width="130" align="right" sortable>
                  <template #default="scope">
                    <span class="price">¥{{ scope.row.totalAmount.toFixed(2) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="最后下单" width="180" align="center">
                  <template #default="scope">{{ formatDate(scope.row.lastOrderTime) }}</template>
                </el-table-column>
                <el-table-column label="信用状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.creditStatus === '良好' ? 'success' : 'warning'" size="small">
                      {{ scope.row.creditStatus }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center" fixed="right">
                  <template #default="scope">
                    <el-button size="small" link type="primary" @click="showHistory(scope.row)">租赁历史</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-main>

      <!-- History Dialog -->
      <el-dialog
        v-model="historyVisible"
        :title="'与 ' + currentCustomer?.name + ' 的租赁历史'"
        width="800px"
        center
      >
        <el-table :data="orderHistory" v-loading="historyLoading" border stripe>
          <el-table-column prop="generatorName" label="设备名称" min-width="180" />
          <el-table-column label="订单金额" width="130" align="right">
            <template #default="scope">
              <span class="price">¥{{ scope.row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ formatStatus(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="180" align="center">
            <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button size="small" link type="primary" @click="viewOrder(scope.row.id)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- Device Modal -->
      <el-dialog v-model="showDeviceModal" :title="isEdit ? '编辑设备' : '发布新设备'" width="600px" center>
        <el-form :model="deviceForm" label-width="100px" class="device-form">
          <el-form-item label="设备名称">
            <el-input v-model="deviceForm.name" placeholder="请输入设备名称" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="功率">
                <el-select v-model="deviceForm.power" placeholder="请选择功率" style="width: 100%">
                  <el-option label="50KW" value="50KW" />
                  <el-option label="100KW" value="100KW" />
                  <el-option label="200KW" value="200KW" />
                  <el-option label="500KW" value="500KW" />
                  <el-option label="1000KW" value="1000KW" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="油耗">
                <el-input v-model="deviceForm.fuelConsumption" placeholder="例如: 10L/h" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="库存状态">
                 <el-select v-model="deviceForm.stockStatus" style="width: 100%">
                   <el-option label="可租" value="AVAILABLE" />
                   <el-option label="维护中" value="MAINTENANCE" />
                   <el-option label="已租" value="RENTED" disabled />
                 </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="重量">
                <el-input v-model="deviceForm.weight" placeholder="例如: 1200kg" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="尺寸">
                <el-input v-model="deviceForm.size" placeholder="例如: 200x100x150cm" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
             <el-col :span="8">
               <el-form-item label="日租金" required>
                 <el-input-number v-model="deviceForm.dailyRent" :min="0" style="width: 100%" :controls="false" placeholder="必填" />
               </el-form-item>
             </el-col>
             <el-col :span="8">
               <el-form-item label="周租金">
                 <el-input-number v-model="deviceForm.weeklyRent" :min="0" style="width: 100%" :controls="false" placeholder="选填" />
               </el-form-item>
             </el-col>
             <el-col :span="8">
               <el-form-item label="月租金">
                 <el-input-number v-model="deviceForm.monthlyRent" :min="0" style="width: 100%" :controls="false" placeholder="选填" />
               </el-form-item>
             </el-col>
           </el-row>

          <el-form-item label="押金">
             <el-input-number v-model="deviceForm.deposit" :min="0" style="width: 100%" :controls="false" />
          </el-form-item>

          <el-form-item label="图片" required>
            <UploadImage v-model="deviceForm.imageUrl" type="product" :multiple="true" :limit="5" :max-count="5" />
          </el-form-item>
          <el-form-item label="权属证明" required>
            <UploadImage v-model="deviceForm.proofOfOwnershipUrl" type="cert" :multiple="false" />
            <div class="form-tip">请上传发票、合格证或权属证明文件</div>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="deviceForm.description" type="textarea" :rows="3" />
          </el-form-item>
          <el-divider content-position="left">配送设置</el-divider>
          <el-form-item label="配送方式">
             <el-radio-group v-model="deviceForm.deliveryMethod">
               <el-radio label="Seller Delivery">商家配送</el-radio>
               <el-radio label="Self Pickup">自提</el-radio>
             </el-radio-group>
          </el-form-item>

          <template v-if="deviceForm.deliveryMethod === 'Seller Delivery'">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="配送范围(km)">
                   <el-input-number v-model="deviceForm.deliveryRange" :min="0" style="width: 100%" :controls="false" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                 <el-form-item label="配送费">
                   <el-input-number v-model="deviceForm.deliveryFee" :min="0" style="width: 100%" :controls="false" />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <el-form-item label="设备位置">
             <el-input v-model="deviceForm.address" placeholder="输入地址" />
             <div style="display: flex; margin-top: 10px; gap: 10px">
               <el-input v-model="deviceForm.latitude" placeholder="纬度" />
               <el-input v-model="deviceForm.longitude" placeholder="经度" />
             </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showDeviceModal = false">取消</el-button>
          <el-button type="primary" @click="saveDevice">保存</el-button>
        </template>
      </el-dialog>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getMyGenerators, createGenerator, updateGenerator, deleteGenerator } from '../api/generator'
import { getMerchantOrders, getMerchantCustomers, getCustomerOrderHistory } from '../api/order'
import { ElMessage } from 'element-plus'
import { Plus, Search, InfoFilled } from '@element-plus/icons-vue'
import UploadImage from '../components/UploadImage.vue'
import ThemeToggle from '../components/ThemeToggle.vue'
import { computed } from 'vue'

const router = useRouter()
const user = ref({})
const activeTab = ref('devices')
const devices = ref([])
const orders = ref([])
const customers = ref([])
const searchCustomer = ref('')
const loadingDevices = ref(false)
const loadingOrders = ref(false)
const loadingCustomers = ref(false)
const historyVisible = ref(false)
const historyLoading = ref(false)
const orderHistory = ref([])
const currentCustomer = ref(null)

const filterCustomers = computed(() => {
  return customers.value.filter(c => 
    (c.name && c.name.toLowerCase().includes(searchCustomer.value.toLowerCase())) ||
    (c.phone && c.phone.includes(searchCustomer.value)) ||
    (c.username && c.username.toLowerCase().includes(searchCustomer.value.toLowerCase()))
  )
})

const loadUser = () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }
}

// Modal State
const showDeviceModal = ref(false)
const isEdit = ref(false)
const deviceForm = reactive({
  id: null,
  name: '',
  power: '',
  fuelConsumption: '',
  weight: '',
  size: '',
  dailyRent: undefined,
  weeklyRent: undefined,
  monthlyRent: undefined,
  deposit: 0,
  stockStatus: 'AVAILABLE',
  imageUrl: '',
  proofOfOwnershipUrl: '',
  description: '',
  deliveryMethod: 'Seller Delivery',
  deliveryRange: 50,
  deliveryFee: 0,
  address: '',
  latitude: 39.9042,
  longitude: 116.4074
})

const loadDevices = async () => {
  loadingDevices.value = true
  try {
    if (!user.value.id) {
        // If user ID is not yet loaded, wait a bit or try to reload user
        const storedUser = localStorage.getItem('user')
        if (storedUser) {
            user.value = JSON.parse(storedUser)
        }
    }

    if (user.value.id) {
        const res = await getMyGenerators()
        devices.value = res
    }
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  } finally {
    loadingDevices.value = false
  }
}

const loadOrders = async () => {
  loadingOrders.value = true
  try {
    const res = await getMerchantOrders()
    orders.value = res
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loadingOrders.value = false
  }
}

const loadCustomers = async () => {
  loadingCustomers.value = true
  try {
    const res = await getMerchantCustomers()
    customers.value = res || []
  } catch (error) {
    ElMessage.error('获取客户列表失败')
  } finally {
    loadingCustomers.value = false
  }
}

const showHistory = async (customer) => {
  currentCustomer.value = customer
  historyVisible.value = true
  historyLoading.value = true
  try {
    const res = await getCustomerOrderHistory(customer.id)
    orderHistory.value = res || []
  } catch (error) {
    ElMessage.error('获取租赁历史失败')
  } finally {
    historyLoading.value = false
  }
}

const handleTabClick = (tab) => {
  if (tab.paneName === 'devices') {
    loadDevices()
  } else if (tab.paneName === 'orders') {
    loadOrders()
  } else if (tab.paneName === 'customers') {
    loadCustomers()
  }
}

const openDeviceModal = (device) => {
  if (device) {
    isEdit.value = true
    Object.assign(deviceForm, device)
  } else {
    isEdit.value = false
    // Reset
    deviceForm.id = null
    deviceForm.name = ''
    deviceForm.power = ''
    deviceForm.fuelConsumption = ''
    deviceForm.weight = ''
    deviceForm.size = ''
    deviceForm.dailyRent = undefined
    deviceForm.weeklyRent = undefined
    deviceForm.monthlyRent = undefined
    deviceForm.deposit = 0
    deviceForm.stockStatus = 'AVAILABLE'
    deviceForm.imageUrl = ''
    deviceForm.proofOfOwnershipUrl = ''
    deviceForm.description = ''
    deviceForm.deliveryMethod = 'Seller Delivery'
    deviceForm.deliveryRange = 50
    deviceForm.deliveryFee = 0
    deviceForm.address = ''
  }
  showDeviceModal.value = true
}

const saveDevice = async () => {
  if (!deviceForm.name || !deviceForm.power || !deviceForm.dailyRent) {
    ElMessage.warning('请填写名称、功率和日租金')
    return
  }
  try {
    if (isEdit.value) {
      await updateGenerator(deviceForm.id, deviceForm)
      ElMessage.success('更新成功')
    } else {
      if (!user.value.id) {
         ElMessage.error('用户信息加载中，请稍后再试')
         return
      }
      await createGenerator(deviceForm, user.value.id)
      ElMessage.success('发布成功')
    }
    showDeviceModal.value = false
    loadDevices()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await deleteGenerator(id)
    ElMessage.success('已下架')
    loadDevices()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const viewOrder = (orderId) => {
  router.push(`/order/${orderId}`)
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

const getStatusType = (status) => {
  switch (status) {
    case 'WAIT_CONFIRM': return 'warning'
    case 'CONFIRMED': return 'primary'
    case 'DELIVERED': return 'info'
    case 'RENTING': return 'success'
    case 'COMPLETED': return 'success'
    default: return 'info'
  }
}

const formatStatus = (status) => {
  const map = {
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

const getStockStatusType = (status) => {
  if (status === 'AVAILABLE') return 'success'
  if (status === 'RENTED') return 'warning'
  if (status === 'MAINTENANCE') return 'danger'
  return 'info'
}

const formatStockStatus = (status) => {
  const map = {
    'AVAILABLE': '可租',
    'RENTED': '已租',
    'MAINTENANCE': '维护中'
  }
  return map[status] || status
}

const getAuditStatusType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

const formatAuditStatus = (status) => {
  const map = {
    'PENDING': '审核中',
    'APPROVED': '已通过',
    'REJECTED': '已驳回'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  loadUser()
  loadDevices()
})
</script>

<style scoped>
.merchant-home {
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
.main-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  min-height: 500px;
}
.tab-actions {
  margin-bottom: 20px;
}
.price {
  font-weight: 600;
  color: #303133;
}
.device-form {
  padding: 0 20px;
}
.customer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.customer-info span {
  font-weight: 500;
  color: #303133;
}
.data-table {
  border-radius: 8px;
  overflow: hidden;
  margin-top: 10px;
}
:deep(.el-table__header-wrapper th) {
  background-color: #f8f9fb !important;
  color: #606266;
  font-weight: 600;
}
</style>
