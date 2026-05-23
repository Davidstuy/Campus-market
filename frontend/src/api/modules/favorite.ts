import request from '@/utils/request'
import type { PageData, Favorite } from '@/types'

export const favoriteApi = {
  list: (params: { page?: number; size?: number }) =>
    request.get('/v1/favorites', { params }) as Promise<PageData<Favorite>>,
  add: (productId: number) => request.post('/v1/favorites', { productId }),
  remove: (productId: number) => request.delete(`/v1/favorites/${productId}`),
  check: (productIds: number[]) =>
    request.post('/v1/favorites/check', { productIds }) as Promise<Record<number, boolean>>,
}
