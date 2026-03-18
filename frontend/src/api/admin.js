import request from '@/utils/request'

// --- Statistics ---
export const getAdminStats = (dimension) => {
  return request.get('/admin/stats', { params: { dimension } })
}

// --- Merchant Audit ---
export const getPendingMerchants = () => {
  return request.get('/admin/merchants/pending')
}

export const approveMerchant = (userId) => {
  return request.post(`/admin/merchants/${userId}/approve`)
}

export const rejectMerchant = (userId, reason) => {
  return request.post(`/admin/merchants/${userId}/reject`, { reason })
}

// New Audit APIs
export const getMerchantApplications = (status) => {
  return request.get('/admin/merchant-applications', { params: { status } })
}

export const auditApplication = (id, status, rejectionReason) => {
  return request.post(`/admin/merchant-applications/${id}/audit`, { status, rejectionReason })
}

// --- User Management ---
export const getAllUsers = () => {
  return request.get('/admin/users')
}

export const updateUserStatus = (userId, status) => {
  return request.post(`/admin/users/${userId}/status`, { status })
}

// --- Platform Config ---
export const getAllConfigs = () => {
  return request.get('/admin/config')
}

export const updateConfig = (key, value) => {
  return request.post('/admin/config/update', { key, value })
}

// --- Generator Audit ---
export const getPendingGenerators = () => {
  return request.get('/admin/generators/pending')
}

export const auditGenerator = (id, status, reason) => {
  return request.post(`/admin/generators/${id}/audit`, { status, reason })
}

// --- Complaints (Note: Different Controller URL) ---
export const getAllComplaints = () => {
  return request.get('/complaints/admin/all')
}

export const resolveComplaint = (id, resolution) => {
  return request.post(`/complaints/${id}/resolve`, { resolution })
}

// --- Activity Logs (Mock/Placeholder) ---
export const getActivityLogs = (params) => {
  // Mock implementation since backend endpoint was not found
  return Promise.resolve({
    data: [
      { id: 1, description: '系统初始化完成', timestamp: new Date(), type: 'success' },
      { id: 2, description: '管理员登录', timestamp: new Date(), type: 'primary' }
    ]
  })
}
