import request from '@/utils/request'

export const addToCart = (data) => {
  return request.post('/cart/add', data)
}

export const getCart = () => {
  return request.get('/cart')
}

export const updateCartItem = (id, data) => {
  return request.put(`/cart/${id}`, data)
}

export const deleteCartItem = (id) => {
  return request.delete(`/cart/${id}`)
}

export const batchDeleteCartItems = (ids) => {
  return request.post('/cart/batch-delete', { ids })
}
