<template>
  <div class="admin-home">
    <el-container>
      <!-- Enhanced Header -->
      <el-header class="header">
        <div class="header-left">
          <div class="logo">
            <el-icon size="24" style="margin-right: 8px"><Setting /></el-icon>
            平台管理后台
          </div>
          <el-tag type="info" effect="dark" size="small">管理员模式</el-tag>
        </div>
        <div class="header-right">
          <div class="user-info">
            <span class="username">管理员ID: {{ adminId }}</span>
            <el-divider direction="vertical" />
            <el-button type="primary" link @click="handleCommand('refresh')">刷新数据</el-button>
            <el-divider direction="vertical" />
            <el-button type="danger" link @click="handleCommand('logout')">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <!-- Statistics Dashboard -->
        <el-row :gutter="20" class="stats-row" v-if="activeTab === 'dashboard'">
          <el-col :xs="24" :sm="12" :md="6" v-for="stat in dashboardStats" :key="stat.title">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" :style="{ background: stat.color + '20', color: stat.color }">
                  <el-icon size="24"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-title">{{ stat.title }}</div>
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-trend" :class="stat.trend > 0 ? 'trend-up' : 'trend-down'">
                    <el-icon><CaretTop v-if="stat.trend > 0" /><CaretBottom v-else /></el-icon>
                    {{ Math.abs(stat.trend) }}%
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- Enhanced Navigation Tabs -->
        <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="admin-tabs">
          
          <!-- 0. Dashboard -->
          <el-tab-pane name="dashboard">
            <template #label>
              <span class="tab-label">
                <el-icon><DataAnalysis /></el-icon>
                数据概览
              </span>
            </template>
            
            <div class="tab-content">
              <div class="tab-header">
                <h3>平台数据概览</h3>
                <div class="tab-actions">
                  <el-radio-group v-model="timeDimension" size="small" @change="refreshDashboard">
                    <el-radio-button label="day">日</el-radio-button>
                    <el-radio-button label="week">周</el-radio-button>
                    <el-radio-button label="month">月</el-radio-button>
                    <el-radio-button label="year">年</el-radio-button>
                  </el-radio-group>
                  <el-button type="primary" @click="refreshDashboard" :icon="Refresh">刷新数据</el-button>
                </div>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card class="chart-card" shadow="hover">
                    <template #header>
                      <span>用户增长趋势</span>
                    </template>
                    <div class="simple-bar-chart">
                      <div v-if="chartData.userGrowth.length === 0" class="no-data">暂无数据</div>
                      <div v-else v-for="(value, index) in chartData.userGrowth" :key="index" class="bar-container">
                        <div class="bar" :style="{ height: Math.max(value / getMax(chartData.userGrowth) * 100, 5) + '%', backgroundColor: '#3b82f6' }" :title="value"></div>
                        <span class="label">{{ chartData.dates[index] }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="chart-card" shadow="hover">
                    <template #header>
                      <span>订单量统计</span>
                    </template>
                    <div class="simple-bar-chart">
                      <div v-if="chartData.orderVolume.length === 0" class="no-data">暂无数据</div>
                      <div v-else v-for="(value, index) in chartData.orderVolume" :key="index" class="bar-container">
                        <div class="bar" :style="{ height: Math.max(value / getMax(chartData.orderVolume) * 100, 5) + '%', backgroundColor: '#f59e0b' }" :title="value"></div>
                        <span class="label">{{ chartData.dates[index] }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="chart-card" shadow="hover">
                    <template #header>
                      <span>收入分析</span>
                    </template>
                    <div class="simple-bar-chart">
                      <div v-if="chartData.revenueGrowth.length === 0" class="no-data">暂无数据</div>
                      <div v-else v-for="(value, index) in chartData.revenueGrowth" :key="index" class="bar-container">
                        <div class="bar" :style="{ height: Math.max(value / getMax(chartData.revenueGrowth) * 100, 5) + '%', backgroundColor: '#10b981' }" :title="value"></div>
                        <span class="label">{{ chartData.dates[index] }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
              
              <el-card class="activity-card" shadow="hover" style="margin-top: 20px;">
                <template #header>
                  <span>最近活动</span>
                </template>
                <el-timeline>
                  <el-timeline-item
                    v-for="activity in recentActivities"
                    :key="activity.id"
                    :timestamp="formatDate(activity.timestamp)"
                    :type="activity.type"
                  >
                    {{ activity.description }}
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </div>
          </el-tab-pane>

          <!-- 1. Merchant Audit -->
          <el-tab-pane name="audit">
            <template #label>
              <span class="tab-label">
                <el-icon><Shop /></el-icon>
                商家审核
                <el-badge v-if="pendingMerchants.length > 0" :value="pendingMerchants.length" class="tab-badge" />
              </span>
            </template>
            
            <div class="tab-content">
              <div class="tab-header">
                <h3>待审核商家列表</h3>
                <el-button type="primary" @click="loadAudit" :icon="Refresh">刷新</el-button>
              </div>
              
              <el-table :data="pendingMerchants" v-loading="loadingAudit" style="width: 100%" stripe border>
                <el-table-column type="expand">
                  <template #default="props">
                    <div class="merchant-detail">
                      <el-descriptions :column="2" border>
                        <el-descriptions-item label="商户类型">{{ props.row.merchantType === 'PERSONAL' ? '个人' : '企业' }}</el-descriptions-item>
                        <el-descriptions-item label="服务类型">{{ props.row.serviceType }}</el-descriptions-item>
                        <el-descriptions-item label="经营地址" :span="2">{{ props.row.address }}</el-descriptions-item>
                        <el-descriptions-item label="申请时间">{{ formatDate(props.row.createdAt) }}</el-descriptions-item>
                      </el-descriptions>
                      <div style="margin-top: 10px;">
                        <h4>资质凭证</h4>
                        <div class="proof-images">
                           <div class="proof-item" v-if="props.row.idCardFront">
                             <el-image 
                               :src="props.row.idCardFront" 
                               :preview-src-list="getMerchantImages(props.row)" 
                               :initial-index="getMerchantImages(props.row).indexOf(props.row.idCardFront)"
                               fit="cover"
                               class="proof-img" 
                               preview-teleported
                             />
                             <span class="proof-label">身份证正面</span>
                           </div>
                           <div class="proof-item" v-if="props.row.idCardBack">
                             <el-image 
                               :src="props.row.idCardBack" 
                               :preview-src-list="getMerchantImages(props.row)"
                               :initial-index="getMerchantImages(props.row).indexOf(props.row.idCardBack)"
                               fit="cover" 
                               class="proof-img" 
                               preview-teleported
                             />
                             <span class="proof-label">身份证反面</span>
                           </div>
                           <div class="proof-item" v-if="props.row.businessLicense">
                             <el-image 
                               :src="props.row.businessLicense" 
                               :preview-src-list="getMerchantImages(props.row)"
                               :initial-index="getMerchantImages(props.row).indexOf(props.row.businessLicense)"
                               fit="cover" 
                               class="proof-img" 
                               preview-teleported
                             />
                             <span class="proof-label">营业执照</span>
                           </div>
                           <div class="proof-item" v-if="props.row.generatorOwnerCert">
                             <el-image 
                               :src="props.row.generatorOwnerCert" 
                               :preview-src-list="getMerchantImages(props.row)"
                               :initial-index="getMerchantImages(props.row).indexOf(props.row.generatorOwnerCert)"
                               fit="cover" 
                               class="proof-img" 
                               preview-teleported
                             />
                             <span class="proof-label">权属证明</span>
                           </div>
                           <div class="proof-item" v-if="props.row.generatorPhoto">
                             <el-image 
                               :src="props.row.generatorPhoto" 
                               :preview-src-list="getMerchantImages(props.row)"
                               :initial-index="getMerchantImages(props.row).indexOf(props.row.generatorPhoto)"
                               fit="cover" 
                               class="proof-img" 
                               preview-teleported
                             />
                             <span class="proof-label">设备实拍</span>
                           </div>
                        </div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="username" label="申请账号" min-width="120" show-overflow-tooltip />
                <el-table-column prop="merchantName" label="商户名称" min-width="150" show-overflow-tooltip />
                <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
                <el-table-column prop="merchantType" label="类型" width="100">
                    <template #default="scope">
                        <el-tag>{{ scope.row.merchantType === 'PERSONAL' ? '个人' : '企业' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button-group>
                      <el-button type="success" size="small" @click="handleApprove(scope.row)" :icon="Check">通过</el-button>
                      <el-button type="danger" size="small" @click="openRejectModal(scope.row)" :icon="Close">驳回</el-button>
                    </el-button-group>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 2. User Management -->
          <el-tab-pane name="users">
            <template #label>
              <span class="tab-label">
                <el-icon><User /></el-icon>
                用户管理
              </span>
            </template>
            
            <div class="tab-content">
              <div class="tab-header">
                <h3>用户账号管理</h3>
                <div class="tab-actions">
                  <el-input v-model="userSearch" placeholder="搜索用户" style="width: 200px" :prefix-icon="Search" />
                  <el-button type="primary" @click="loadUsers" :icon="Refresh">刷新</el-button>
                </div>
              </div>
              
              <el-table :data="filteredUsers" v-loading="loadingUsers" style="width: 100%" stripe border>
                <el-table-column type="index" width="60" align="center" />
                <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
                <el-table-column prop="userId" label="用户ID" min-width="150" show-overflow-tooltip />
                <el-table-column prop="name" label="姓名" min-width="100" show-overflow-tooltip />
                <el-table-column prop="role" label="角色" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="getRoleType(scope.row.role)" effect="light" size="small">{{ scope.row.role }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)" effect="light" size="small">
                      <el-icon><CircleCheck v-if="scope.row.status === 'ACTIVE'" /><CircleClose v-else /></el-icon>
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="phone" label="手机号" min-width="120" />
                <el-table-column label="注册时间" min-width="160">
                  <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right" align="center">
                  <template #default="scope">
                    <el-switch
                      v-model="scope.row.status"
                      active-value="ACTIVE"
                      inactive-value="DISABLED"
                      @change="toggleUserStatus(scope.row, scope.row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE')"
                      :loading="userStatusLoading[scope.row.userId]"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 3. Complaint Management -->
          <el-tab-pane name="complaints">
            <template #label>
              <span class="tab-label">
                <el-icon><Warning /></el-icon>
                投诉处理
                <el-badge v-if="pendingComplaintsCount > 0" :value="pendingComplaintsCount" class="tab-badge" type="warning" />
              </span>
            </template>
            
            <div class="tab-content">
              <div class="tab-header">
                <h3>投诉处理中心</h3>
                <div class="tab-actions">
                  <el-select v-model="complaintFilter" placeholder="筛选状态" style="width: 120px">
                    <el-option label="全部" value="" />
                    <el-option label="待处理" value="PENDING" />
                    <el-option label="已处理" value="RESOLVED" />
                  </el-select>
                  <el-button type="primary" @click="loadComplaints" :icon="Refresh">刷新</el-button>
                </div>
              </div>
              
              <el-table :data="filteredComplaints" v-loading="loadingComplaints" style="width: 100%" stripe border>
                <el-table-column type="expand">
                  <template #default="props">
                    <div class="complaint-detail">
                      <el-descriptions :column="2" border>
                        <el-descriptions-item label="投诉详情">{{ props.row.content }}</el-descriptions-item>
                        <el-descriptions-item label="处理结果" v-if="props.row.resolution">{{ props.row.resolution }}</el-descriptions-item>
                        <el-descriptions-item label="投诉时间">{{ formatDate(props.row.createdAt) }}</el-descriptions-item>
                        <el-descriptions-item label="处理时间" v-if="props.row.resolvedAt">{{ formatDate(props.row.resolvedAt) }}</el-descriptions-item>
                      </el-descriptions>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column type="index" width="60" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column label="订单信息" width="120">
                  <template #default="scope">
                    <el-tag type="info" size="small">订单 #{{ scope.row.orderId }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="投诉人" min-width="120" show-overflow-tooltip>
                  <template #default="scope">
                    <div class="user-info-cell">
                      <el-icon><User /></el-icon>
                      {{ scope.row.complainant?.username }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="被投诉人" min-width="120" show-overflow-tooltip>
                  <template #default="scope">
                    <div class="user-info-cell">
                      <el-icon><User /></el-icon>
                      {{ scope.row.respondent?.username }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
                <el-table-column label="状态" width="120">
                  <template #default="scope">
                    <el-tag :type="getComplaintStatusType(scope.row.status)" effect="dark">
                      <el-icon><Clock v-if="scope.row.status === 'PENDING'" /><Check v-else /></el-icon>
                      {{ scope.row.status === 'PENDING' ? '待处理' : '已处理' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="投诉时间" width="180">
                  <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="scope">
                    <el-button 
                      v-if="scope.row.status === 'PENDING'" 
                      type="warning" 
                      size="small" 
                      @click="openResolveModal(scope.row)"
                      :icon="Edit"
                    >处理</el-button>
                    <el-tag v-else type="success" effect="plain">已完成</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 4. Platform Config -->
          <el-tab-pane name="config">
            <template #label>
              <span class="tab-label">
                <el-icon><Tools /></el-icon>
                平台配置
              </span>
            </template>
            
            <div class="tab-content">
              <div class="tab-header">
                <h3>系统配置管理</h3>
                <el-button type="primary" @click="loadConfigs" :icon="Refresh">刷新配置</el-button>
              </div>
              
              <el-table :data="configs" v-loading="loadingConfig" style="width: 100%" stripe border>
                <el-table-column type="index" width="60" />
                <el-table-column prop="key" label="配置项" width="200">
                  <template #default="scope">
                    <el-tag type="info" effect="light">{{ scope.row.key }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="配置说明" />
                <el-table-column label="当前值" width="350">
                  <template #default="scope">
                    <div class="config-input-group">
                      <el-input 
                        v-model="scope.row.editValue" 
                        placeholder="输入新值"
                        size="small"
                        :disabled="configSaving[scope.row.key]"
                      />
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="handleUpdateConfig(scope.row)"
                        :loading="configSaving[scope.row.key]"
                        :icon="Check"
                      >保存</el-button>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="操作记录" width="150">
                  <template #default="scope">
                    <el-button type="text" size="small" @click="showConfigHistory(scope.row)">
                      <el-icon><Document /></el-icon>
                      查看历史
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>

      <!-- Enhanced Modals -->
      <!-- Reject Modal -->
      <el-dialog 
        v-model="showRejectModal" 
        title="驳回商家申请"
        width="500px"
        :close-on-click-modal="false"
      >
        <div class="modal-content">
          <el-alert
            title="驳回后将无法恢复"
            type="warning"
            show-icon
            :closable="false"
            style="margin-bottom: 16px"
          />
          <el-form :model="rejectForm" ref="rejectFormRef" label-width="80px">
            <el-form-item 
              label="驳回理由" 
              prop="reason"
              :rules="[{ required: true, message: '请输入驳回理由', trigger: 'blur' }]"
            >
              <el-input 
                v-model="rejectForm.reason" 
                type="textarea" 
                :rows="4"
                placeholder="请详细说明驳回理由，这将帮助商家了解问题并改进..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="showRejectModal = false">取消</el-button>
          <el-button type="danger" @click="handleReject" :loading="rejectLoading">确认驳回</el-button>
        </template>
      </el-dialog>

      <!-- Resolve Complaint Modal -->
      <el-dialog 
        v-model="showResolveModal" 
        title="处理投诉"
        width="600px"
        :close-on-click-modal="false"
      >
        <div class="modal-content">
          <el-alert
            title="请公正处理，维护平台秩序"
            type="info"
            show-icon
            :closable="false"
            style="margin-bottom: 16px"
          />
          <el-form :model="resolveForm" ref="resolveFormRef" label-width="100px">
            <el-form-item label="投诉详情">
              <el-input 
                :value="currentComplaint?.content" 
                type="textarea" 
                :rows="3"
                readonly
              />
            </el-form-item>
            <el-form-item 
              label="处理方案" 
              prop="resolution"
              :rules="[{ required: true, message: '请输入处理方案', trigger: 'blur' }]"
            >
              <el-input 
                v-model="resolveForm.resolution" 
                type="textarea" 
                :rows="4"
                placeholder="请输入详细的处理方案或调解结果..."
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="处理类型">
              <el-radio-group v-model="resolveForm.type">
                <el-radio label="mediate">调解处理</el-radio>
                <el-radio label="penalty">违规处罚</el-radio>
                <el-radio label="dismiss">驳回投诉</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="showResolveModal = false">取消</el-button>
          <el-button type="primary" @click="handleResolve" :loading="resolveLoading">确认处理</el-button>
        </template>
      </el-dialog>

    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { 
  getMerchantApplications, auditApplication,
  getAllUsers, updateUserStatus, 
  getAllConfigs, updateConfig, 
  getAllComplaints, resolveComplaint,
  getAdminStats, getActivityLogs
} from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Shop, User, Warning, Tools, MoreFilled, UserFilled,
  Refresh, Check, Close, Search, CircleCheck, CircleClose,
  Clock, Edit, Document, DataAnalysis, TrendCharts, PieChart, 
  Histogram, CaretTop, CaretBottom
} from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('dashboard')

// Data
const pendingMerchants = ref([])
const allUsers = ref([])
const complaints = ref([])
const configs = ref([])
const dashboardStats = ref([])
const chartData = ref({
  dates: [],
  userGrowth: [],
  orderVolume: [],
  revenueGrowth: []
})
const timeDimension = ref('day')
const recentActivities = ref([])
const adminId = ref('')

// Enhanced UI states
const userSearch = ref('')
const complaintFilter = ref('')
const userStatusLoading = ref({})
const configSaving = ref({})

// Enhanced modal forms
const rejectForm = reactive({
  reason: ''
})
const resolveForm = reactive({
  resolution: '',
  type: 'mediate'
})
const currentComplaint = ref(null)

// Loading states
const loadingAudit = ref(false)
const loadingUsers = ref(false)
const loadingComplaints = ref(false)
const loadingConfig = ref(false)
const rejectLoading = ref(false)
const resolveLoading = ref(false)

// Modals
const showRejectModal = ref(false)
const currentRejectId = ref(null)

const showResolveModal = ref(false)
const currentComplaintId = ref(null)

// Computed properties
const filteredUsers = computed(() => {
  if (!userSearch.value) return allUsers.value
  return allUsers.value.filter(user => 
    user.username.toLowerCase().includes(userSearch.value.toLowerCase()) ||
    user.name.toLowerCase().includes(userSearch.value.toLowerCase()) ||
    user.phone.includes(userSearch.value)
  )
})

const filteredComplaints = computed(() => {
  if (!complaintFilter.value) return complaints.value
  return complaints.value.filter(complaint => complaint.status === complaintFilter.value)
})

const pendingComplaintsCount = computed(() => {
  return complaints.value.filter(complaint => complaint.status === 'PENDING').length
})

// --- Actions ---

const handleTabClick = (tab) => {
  loadData(tab.paneName)
}

const loadData = (tabName) => {
  if (tabName === 'dashboard') refreshDashboard()
  else if (tabName === 'audit') loadAudit()
  else if (tabName === 'users') loadUsers()
  else if (tabName === 'complaints') loadComplaints()
  else if (tabName === 'config') loadConfigs()
}

const handleCommand = (command) => {
  if (command === 'refresh') {
    loadData(activeTab.value)
    ElMessage.success('数据已刷新')
  } else if (command === 'logout') {
    logout()
  }
}

// 1. Audit
const loadAudit = async () => {
  loadingAudit.value = true
  try {
    const res = await getMerchantApplications('PENDING')
    pendingMerchants.value = res
  } catch (e) { 
    ElMessage.error('加载待审核商家失败')
  } finally { 
    loadingAudit.value = false 
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认通过商家 "${row.merchantName}" 的入驻申请？`,
      '审核确认',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await auditApplication(row.id, 'APPROVED', null)
    ElMessage.success('商家审核已通过')
    loadAudit()
  } catch (e) { 
    if (e !== 'cancel') {
      ElMessage.error('审核操作失败')
    }
  }
}

const openRejectModal = (row) => {
  currentRejectId.value = row.id
  rejectForm.reason = ''
  showRejectModal.value = true
}

const handleReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入驳回理由')
    return
  }
  
  rejectLoading.value = true
  try {
    await auditApplication(currentRejectId.value, 'REJECTED', rejectForm.reason.trim())
    ElMessage.success('商家申请已驳回')
    showRejectModal.value = false
    loadAudit()
  } catch (e) { 
    ElMessage.error('驳回操作失败')
  } finally {
    rejectLoading.value = false
  }
}

// 2. Users
const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await getAllUsers()
    allUsers.value = res
  } catch (e) { 
    ElMessage.error('加载用户列表失败')
  } finally { 
    loadingUsers.value = false 
  }
}

const toggleUserStatus = async (row, status) => {
  userStatusLoading.value[row.userId] = true
  try {
    const action = status === 'DISABLED' ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确认${action}用户 "${row.username}" 的账号？`,
      '状态变更确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateUserStatus(row.userId, status)
    ElMessage.success(`用户账号已${action}`)
    loadUsers()
  } catch (e) { 
    if (e !== 'cancel') {
      ElMessage.error('状态更新失败')
    }
    // Reset the switch state
    row.status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  } finally {
    userStatusLoading.value[row.userId] = false
  }
}

// 3. Complaints
const loadComplaints = async () => {
  loadingComplaints.value = true
  try {
    const res = await getAllComplaints()
    complaints.value = res
  } catch (e) { 
    ElMessage.error('加载投诉列表失败')
  } finally { 
    loadingComplaints.value = false 
  }
}

const openResolveModal = (row) => {
  currentComplaint.value = row
  currentComplaintId.value = row.id
  resolveForm.resolution = ''
  resolveForm.type = 'mediate'
  showResolveModal.value = true
}

const handleResolve = async () => {
  if (!resolveForm.resolution.trim()) {
    ElMessage.warning('请输入处理方案')
    return
  }
  
  resolveLoading.value = true
  try {
    await resolveComplaint(currentComplaintId.value, resolveForm.resolution.trim())
    ElMessage.success('投诉处理完成')
    showResolveModal.value = false
    loadComplaints()
  } catch (e) { 
    ElMessage.error('处理投诉失败')
  } finally {
    resolveLoading.value = false
  }
}

// 4. Config
const loadConfigs = async () => {
  loadingConfig.value = true
  try {
    const res = await getAllConfigs()
    configs.value = res.map(c => ({...c, editValue: c.value}))
  } catch (e) { 
    ElMessage.error('加载配置失败')
  } finally { 
    loadingConfig.value = false 
  }
}

const handleUpdateConfig = async (row) => {
  if (!row.editValue.trim()) {
    ElMessage.warning('配置值不能为空')
    return
  }
  
  configSaving.value[row.key] = true
  try {
    await updateConfig(row.key, row.editValue.trim())
    ElMessage.success('配置保存成功')
    loadConfigs()
  } catch (e) { 
    ElMessage.error('配置保存失败')
  } finally {
    configSaving.value[row.key] = false
  }
}

// Helper functions
const getMerchantImages = (row) => {
  const images = []
  if (row.idCardFront) images.push(row.idCardFront)
  if (row.idCardBack) images.push(row.idCardBack)
  if (row.businessLicense) images.push(row.businessLicense)
  if (row.generatorOwnerCert) images.push(row.generatorOwnerCert)
  if (row.generatorPhoto) images.push(row.generatorPhoto)
  return images
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const getRoleType = (role) => {
  const roleTypes = {
    'ADMIN': 'danger',
    'MERCHANT': 'warning',
    'TENANT': 'info'
  }
  return roleTypes[role] || 'info'
}

const getStatusType = (status) => {
  return status === 'ACTIVE' ? 'success' : 'danger'
}

const getComplaintStatusType = (status) => {
  return status === 'PENDING' ? 'warning' : 'success'
}

const showConfigHistory = (row) => {
  ElMessage.info(`配置项 "${row.key}" 的历史记录功能开发中`)
}

const logout = () => {
  ElMessageBox.confirm(
    '确认退出管理员后台？',
    '退出确认',
    {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    localStorage.removeItem('user')
    router.push('/login')
    ElMessage.success('已安全退出')
  }).catch(() => {})
}

// Dashboard functions
const refreshDashboard = async () => {
  try {
    // Load dashboard statistics
    const statsRes = await getAdminStats(timeDimension.value)
    const data = statsRes

    // Update Overview Cards
    dashboardStats.value = [
      {
        title: '总用户数',
        value: data.totalUsers || 0,
        trend: 0, // Backend doesn't calculate trend percentage yet, keeping 0 or removing
        icon: 'User',
        color: '#3b82f6'
      },
      {
        title: '入驻商家',
        value: data.totalMerchants || 0,
        trend: 0,
        icon: 'Shop',
        color: '#f59e0b'
      },
      {
        title: '在线设备',
        value: data.onlineGenerators || 0,
        trend: 0,
        icon: 'Tools',
        color: '#10b981'
      },
      {
        title: '总交易额',
        value: '¥' + (data.totalTransactionVolume || 0),
        trend: 0,
        icon: 'Document',
        color: '#ef4444'
      }
    ]
    
    // Update Chart Data
    chartData.value = {
      dates: data.dates || [],
      userGrowth: data.userGrowth || [],
      orderVolume: data.orderVolume || [],
      revenueGrowth: data.revenueGrowth || []
    }

    // Load recent activities
    const activityRes = await getActivityLogs({ limit: 10 })
    recentActivities.value = activityRes.map(activity => ({
      id: activity.id,
      description: activity.description,
      timestamp: activity.timestamp,
      type: activity.type || 'primary'
    }))
    
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

// Chart Helpers
const getMax = (arr) => {
  if (!arr || arr.length === 0) return 1
  return Math.max(...arr)
}

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user'))
  if (user) {
    adminId.value = user.userid
  }
  loadData('dashboard')
})
</script>

<style scoped>
.admin-home {
  height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Enhanced Header */
.header {
  background: #ffffff;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid #e4e7ed;
  z-index: 10;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  letter-spacing: 0.5px;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-weight: 500;
  font-size: 14px;
  color: #606266;
}

/* Enhanced Main Content */
.main-content {
  padding: 24px;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

/* Enhanced Tabs */
.admin-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.admin-tabs :deep(.el-tabs__header) {
  background: #ffffff;
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
}

.admin-tabs :deep(.el-tabs__nav) {
  padding: 0 24px;
}

.admin-tabs :deep(.el-tabs__item) {
  height: 56px;
  line-height: 56px;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  padding: 0 24px;
  transition: all 0.3s ease;
}

.admin-tabs :deep(.el-tabs__item:hover) {
  color: #409eff;
}

.admin-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.admin-tabs :deep(.el-tabs__active-bar) {
  background-color: #409eff;
  height: 3px;
  border-radius: 2px;
}

/* Tab Label */
.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

.tab-badge {
  margin-left: 8px;
}

/* Tab Content */
.tab-content {
  padding: 24px;
  background: white;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.tab-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.tab-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Enhanced Tables */
:deep(.el-table) {
  border-radius: 4px;
  overflow: hidden;
  --el-table-header-bg-color: #f5f7fa;
  --el-table-row-hover-bg-color: #f5f7fa;
}

:deep(.el-table__header) {
  background: #f5f7fa;
}

:deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table td) {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

:deep(.el-table__body tr:hover > td) {
  background: #f0f9eb !important;
}

/* Enhanced Buttons */
:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.2s ease;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #409eff;
  --el-button-border-color: #409eff;
  --el-button-hover-bg-color: #66b1ff;
  --el-button-hover-border-color: #66b1ff;
}

:deep(.el-button--success) {
  --el-button-bg-color: #67c23a;
  --el-button-border-color: #67c23a;
  --el-button-hover-bg-color: #85ce61;
  --el-button-hover-border-color: #85ce61;
}

:deep(.el-button--danger) {
  --el-button-bg-color: #f56c6c;
  --el-button-border-color: #f56c6c;
  --el-button-hover-bg-color: #f78989;
  --el-button-hover-border-color: #f78989;
}

:deep(.el-button--warning) {
  --el-button-bg-color: #e6a23c;
  --el-button-border-color: #e6a23c;
  --el-button-hover-bg-color: #ebb563;
  --el-button-hover-border-color: #ebb563;
}

/* Enhanced Tags */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* Enhanced Input */
:deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

/* Enhanced Dialog */
:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  margin: 0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  background-color: #f9fafc;
  border-radius: 0 0 8px 8px;
}

/* Modal Content */
.modal-content {
  padding: 8px 0;
}

/* Dashboard Statistics */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  background: white;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

/* Chart Cards */
.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
  background: white;
}

.chart-card :deep(.el-card__header) {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  font-weight: 600;
  color: #303133;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 14px;
}

.chart-placeholder p {
  margin-top: 12px;
  color: #909399;
}

/* Activity Card */
.activity-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  background: white;
}

.activity-card :deep(.el-card__header) {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  font-weight: 600;
  color: #303133;
}

.activity-card :deep(.el-timeline) {
  padding: 16px 0;
}

.activity-card :deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 12px;
}

/* Enhanced Timeline */
:deep(.el-timeline-item__node) {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

:deep(.el-timeline-item__node--primary) {
  background: #409eff;
}

:deep(.el-timeline-item__node--success) {
  background: #67c23a;
}

:deep(.el-timeline-item__node--warning) {
  background: #e6a23c;
}

:deep(.el-timeline-item__node--danger) {
  background: #f56c6c;
}

/* Merchant Detail */
.merchant-detail {
  padding: 16px;
  background: #f9fafc;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.proof-images {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.proof-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.proof-label {
  font-size: 12px;
  color: #606266;
}

.proof-img {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  cursor: zoom-in;
  transition: all 0.2s;
}

.proof-img:hover {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}

/* Complaint Detail */
.complaint-detail {
  padding: 16px;
  background: #f9fafc;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

/* User Info Cell */
.user-info-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

/* Config Input Group */
.config-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.config-input-group .el-input {
  flex: 1;
}

/* Enhanced Switch */
:deep(.el-switch) {
  --el-switch-on-color: #67c23a;
  --el-switch-off-color: #dcdfe6;
}

/* Loading Animation */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.9);
}

/* Responsive Design */
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
  
  .tab-content {
    padding: 16px;
  }
  
  .tab-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .tab-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 12px;
  }
}

/* Simple Bar Chart */
.simple-bar-chart {
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  padding-bottom: 20px;
  position: relative;
}

.bar-container {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  margin: 0 4px;
}

.bar {
  width: 100%;
  max-width: 20px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  min-height: 4px;
}

.bar:hover {
  opacity: 0.8;
}

.label {
  font-size: 10px;
  color: #909399;
  margin-top: 4px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.no-data {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
}
</style>
