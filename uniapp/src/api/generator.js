import request from '@/utils/request'

export const searchGenerators = (params) => {
  return request.get('/generators/search', { params })
}

export const getGeneratorDetail = (id) => {
  return request.get(`/generators/${id}`)
}

export const getMyGenerators = (userId) => {
  // Allow passing userId, fallback to storage
  let uid = userId;
  if (!uid) {
      const userStr = uni.getStorageSync('user');
      if (userStr) {
          try {
             const user = JSON.parse(userStr);
             uid = user.userid;
          } catch(e) {}
      }
  }
  return request.get(`/generators/merchant/${uid}`)
}

export const createGenerator = (data) => {
  return request.post('/generators', data)
}

export const updateGenerator = (id, data) => {
  return request.put(`/generators/${id}`, data)
}

export const deleteGenerator = (id) => {
  return request.delete(`/generators/${id}`)
}
