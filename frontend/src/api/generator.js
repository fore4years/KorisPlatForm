import request from '@/utils/request'

export const searchGenerators = (params) => {
  return request.get('/generators/search', { params })
}

export const getGeneratorDetail = (id) => {
  return request.get(`/generators/${id}`)
}

export const getMyGenerators = () => {
  const user = JSON.parse(localStorage.getItem('user'))
  return request.get(`/generators/merchant/${user?.userid}`)
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
