<template>
  <view class="admin-home">
    <view class="header">
        <text class="title">管理后台</text>
        <text class="logout-btn" @click="handleLogout">退出</text>
    </view>

    <view class="tabs">
        <view :class="['tab-item', activeTab === 'dashboard' ? 'active' : '']" @click="activeTab = 'dashboard'">概览</view>
        <view :class="['tab-item', activeTab === 'audit' ? 'active' : '']" @click="activeTab = 'audit'">
            审核 <text v-if="pendingMerchants.length" class="dot"></text>
        </view>
        <view :class="['tab-item', activeTab === 'users' ? 'active' : '']" @click="activeTab = 'users'">用户</view>
    </view>

    <scroll-view scroll-y class="content-area">
        <!-- Dashboard -->
        <view v-if="activeTab === 'dashboard'">
            <view class="stats-grid">
                <view class="stat-card" v-for="stat in dashboardStats" :key="stat.title" :style="{ borderLeft: '4px solid ' + stat.color }">
                    <text class="stat-value">{{ stat.value }}</text>
                    <text class="stat-title">{{ stat.title }}</text>
                </view>
            </view>
            <view class="section">
                <text class="section-title">最近活动</text>
                <view class="activity-list">
                    <view class="activity-item" v-for="log in recentActivities" :key="log.id">
                        <text class="log-desc">{{ log.description }}</text>
                        <text class="log-time">{{ formatDate(log.timestamp) }}</text>
                    </view>
                </view>
            </view>
        </view>

        <!-- Audit -->
        <view v-if="activeTab === 'audit'">
            <view class="card-list">
                <view class="card" v-for="item in pendingMerchants" :key="item.id">
                    <view class="card-row">
                        <text class="card-title">{{ item.merchantName }}</text>
                        <text class="card-tag">{{ item.merchantType === 'PERSONAL' ? '个人' : '企业' }}</text>
                    </view>
                    <view class="card-row"><text class="card-label">申请人:</text> <text>{{ item.username }}</text></view>
                    <view class="card-row"><text class="card-label">电话:</text> <text>{{ item.contactPhone }}</text></view>
                    <view class="card-actions">
                        <button size="mini" type="primary" @click="handleApprove(item)">通过</button>
                        <button size="mini" type="warn" @click="handleReject(item)">驳回</button>
                    </view>
                </view>
                <view v-if="pendingMerchants.length === 0" class="empty">无待审核申请</view>
            </view>
        </view>

        <!-- Users -->
        <view v-if="activeTab === 'users'">
            <view class="card-list">
                <view class="card" v-for="u in allUsers" :key="u.userId">
                    <view class="card-row">
                        <text class="card-title">{{ u.username }}</text>
                        <text class="card-tag" :class="u.role">{{ u.role }}</text>
                    </view>
                    <view class="card-row"><text class="card-label">状态:</text> <text :class="u.status">{{ u.status }}</text></view>
                    <view class="card-actions">
                         <button size="mini" @click="toggleUserStatus(u)">
                             {{ u.status === 'ACTIVE' ? '禁用' : '启用' }}
                         </button>
                    </view>
                </view>
            </view>
        </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminStats, getMerchantApplications, auditApplication, getAllUsers, updateUserStatus, getActivityLogs } from '@/api/admin'

const activeTab = ref('dashboard')
const dashboardStats = ref([])
const pendingMerchants = ref([])
const allUsers = ref([])
const recentActivities = ref([])

const loadDashboard = async () => {
    try {
        const data = await getAdminStats('day')
        dashboardStats.value = [
            { title: '总用户', value: data.totalUsers || 0, color: '#3b82f6' },
            { title: '商家', value: data.totalMerchants || 0, color: '#f59e0b' },
            { title: '设备', value: data.onlineGenerators || 0, color: '#10b981' },
            { title: '交易额', value: '¥' + (data.totalTransactionVolume || 0), color: '#ef4444' }
        ]
        const logs = await getActivityLogs()
        recentActivities.value = logs
    } catch (e) { console.error(e) }
}

const loadAudit = async () => {
    try {
        const res = await getMerchantApplications('PENDING')
        pendingMerchants.value = res || []
    } catch (e) { console.error(e) }
}

const loadUsers = async () => {
    try {
        const res = await getAllUsers()
        allUsers.value = res || []
    } catch (e) { console.error(e) }
}

const handleApprove = (item) => {
    uni.showModal({
        title: '确认',
        content: `通过 ${item.merchantName} 的申请？`,
        success: async (res) => {
            if (res.confirm) {
                await auditApplication(item.id, 'APPROVED')
                uni.showToast({ title: '已通过' })
                loadAudit()
            }
        }
    })
}

const handleReject = (item) => {
    uni.showModal({
        title: '驳回',
        editable: true,
        placeholderText: '请输入驳回理由',
        success: async (res) => {
            if (res.confirm) {
                await auditApplication(item.id, 'REJECTED', res.content)
                uni.showToast({ title: '已驳回' })
                loadAudit()
            }
        }
    })
}

const toggleUserStatus = (u) => {
    const newStatus = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    uni.showModal({
        title: '确认',
        content: `确定要${newStatus === 'ACTIVE' ? '启用' : '禁用'}该用户？`,
        success: async (res) => {
            if (res.confirm) {
                await updateUserStatus(u.userId, newStatus)
                uni.showToast({ title: '已更新' })
                loadUsers()
            }
        }
    })
}

const handleLogout = () => {
    uni.removeStorageSync('user')
    uni.removeStorageSync('token')
    uni.reLaunch({ url: '/pages/login/login' })
}

const formatDate = (d) => {
    if (!d) return ''
    return new Date(d).toLocaleDateString()
}

onMounted(() => {
    loadDashboard()
    loadAudit()
    loadUsers()
})
</script>

<style lang="scss" scoped>
.admin-home {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f5f7fa;
}
.header {
    background: #fff;
    padding: 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.title { font-size: 32rpx; font-weight: bold; }
.logout-btn { color: #999; font-size: 26rpx; }

.tabs {
    display: flex;
    background: #fff;
    margin-bottom: 20rpx;
}
.tab-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 28rpx;
    position: relative;
    &.active {
        color: #409EFF;
        font-weight: bold;
        &:after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 30%;
            width: 40%;
            height: 4rpx;
            background: #409EFF;
        }
    }
}
.dot {
    display: inline-block;
    width: 12rpx;
    height: 12rpx;
    background: #f56c6c;
    border-radius: 50%;
    position: absolute;
    top: 20rpx;
    right: 40rpx;
}

.content-area {
    flex: 1;
    padding: 20rpx;
    box-sizing: border-box;
}

.stats-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-bottom: 30rpx;
}
.stat-card {
    width: 47%;
    background: #fff;
    padding: 20rpx;
    border-radius: 8rpx;
    box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.05);
}
.stat-value { font-size: 36rpx; font-weight: bold; display: block; }
.stat-title { font-size: 24rpx; color: #666; }

.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; display: block; }
.activity-item {
    background: #fff;
    padding: 20rpx;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.log-desc { font-size: 28rpx; color: #333; }
.log-time { font-size: 24rpx; color: #999; }

.card-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
}
.card {
    background: #fff;
    padding: 24rpx;
    border-radius: 12rpx;
}
.card-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12rpx;
}
.card-title { font-size: 30rpx; font-weight: bold; }
.card-tag { font-size: 22rpx; padding: 2rpx 8rpx; background: #eee; border-radius: 4rpx; }
.card-label { color: #666; font-size: 26rpx; margin-right: 10rpx; }
.card-actions {
    display: flex;
    justify-content: flex-end;
    gap: 20rpx;
    margin-top: 20rpx;
}
.empty { text-align: center; color: #999; padding: 40rpx; }
</style>
