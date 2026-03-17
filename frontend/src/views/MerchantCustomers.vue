<template>
  <div class="merchant-customers">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>客户管理</span>
          <el-input
            v-model="search"
            placeholder="搜索客户姓名/手机号"
            style="width: 200px"
            clearable
          />
        </div>
      </template>

      <el-table :data="filterCustomers" style="width: 100%" v-loading="loading">
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
            ¥{{ scope.row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastOrderTime" label="最后下单" width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.lastOrderTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="creditStatus" label="信用状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.creditStatus === '良好' ? 'success' : 'warning'" size="small">
              {{ scope.row.creditStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="showHistory(scope.row)">
              租赁历史
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 租赁历史对话框 -->
    <el-dialog
      v-model="historyVisible"
      :title="'与 ' + currentCustomer?.name + ' 的租赁历史'"
      width="70%"
    >
      <el-table :data="orderHistory" v-loading="historyLoading">
        <el-table-column prop="generatorName" label="设备名称" />
        <el-table-column prop="totalAmount" label="订单金额">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag>{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMerchantCustomers, getCustomerOrderHistory } from '@/api/order'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const historyLoading = ref(false)
const customers = ref([])
const search = ref('')
const historyVisible = ref(false)
const orderHistory = ref([])
const currentCustomer = ref(null)

const filterCustomers = computed(() => {
  return customers.value.filter(c => 
    c.name.toLowerCase().includes(search.value.toLowerCase()) ||
    c.phone.includes(search.value)
  )
})

const fetchCustomers = async () => {
  loading.value = true
  try {
    const res = await getMerchantCustomers()
    customers.value = res || []
  } catch (error) {
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

onMounted(() => {
  fetchCustomers()
})
</script>

<style scoped>
.merchant-customers {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.customer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.customer-info .name {
  font-weight: bold;
}
.customer-info .username {
  font-size: 12px;
  color: #999;
}
</style>
