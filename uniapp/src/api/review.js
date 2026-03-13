import request from '@/utils/request'

export const createReview = (data) => {
  return request.post('/reviews/create', data)
}

export const getGeneratorReviews = (generatorId) => {
  return request.get(`/reviews/generator/${generatorId}`)
}
