import request from '@/utils/request'

export const calculatePrice = (params) => {
  return request.post('/orders/calculate-price', params)
}

export const createOrder = (data) => {
  return request.post('/orders', data)
}

export const getTenantOrders = (tenantId) => {
  return request.get(`/orders/tenant/${tenantId}`)
}

export const getMerchantOrders = () => {
  const user = JSON.parse(localStorage.getItem('user'))
  return request.get(`/orders/merchant/${user?.userid}`)
}

export const getOrderDetail = (orderId) => {
  return request.get(`/orders/${orderId}`)
}

export const confirmOrder = (orderId) => {
  return request.post(`/orders/${orderId}/confirm`)
}

export const updateDelivery = (orderId, deliveryType) => {
  return request.post(`/orders/${orderId}/delivery`, { deliveryType })
}

export const uploadEvidence = (orderId, evidenceUrl) => {
  return request.post(`/orders/${orderId}/evidence`, { evidenceUrl })
}

export const confirmReceipt = (id) => {
  return request.post(`/orders/${id}/receipt`)
}

export const completeOrder = (id) => {
  return request.post(`/orders/${id}/complete`)
}

export const confirmReturn = (id, data) => {
  return request.post(`/orders/${id}/return`, data)
}
