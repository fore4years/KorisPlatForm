import request from '@/utils/request'

export const createRepair = (data) => {
  return request.post('/repairs/create', data)
}

export const getRepairsByOrder = (orderId) => {
  return request.get(`/repairs/order/${orderId}`)
}

export const updateRepairStatus = (id, data) => {
  return request.post(`/repairs/${id}/update`, data)
}
