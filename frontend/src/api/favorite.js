import request from '@/utils/request'

export const getFavorites = () => {
  return request.get('/favorites')
}

export const toggleFavorite = (generatorId) => {
  return request.post('/favorites/toggle', { generatorId })
}
