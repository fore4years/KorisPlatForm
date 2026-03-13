import request from '@/utils/request'

export const getContract = (orderId) => {
  return request.get(`/contracts/order/${orderId}`)
}

export const signContract = (orderId, userId, role) => {
  return request.post('/contracts/sign', { orderId, userId, role })
}
