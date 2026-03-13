import request from '@/utils/request'

export const getProfile = () => {
  return request.get('/user/profile')
}

export const updateProfile = (data) => {
  return request.put('/user/profile', data)
}

export const submitMerchantApplication = (data) => {
  return request.post('/user/merchant-application', data)
}

export const getMyMerchantApplication = () => {
  return request.get('/user/merchant-application')
}
