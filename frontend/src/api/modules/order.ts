import request from '@/utils/request'
import type { OrderVO, CreateOrderRequest, PageData } from '@/types'

export const orderApi = {
  create: (data: CreateOrderRequest) => request.post('/v1/orders', data) as Promise<OrderVO>,
  pay: (id: number) => request.put(`/v1/orders/${id}/pay`) as Promise<OrderVO>,
  ship: (id: number) => request.put(`/v1/orders/${id}/ship`) as Promise<OrderVO>,
  complete: (id: number) => request.put(`/v1/orders/${id}/complete`) as Promise<OrderVO>,
  cancel: (id: number) => request.put(`/v1/orders/${id}/cancel`),
  listBuy: (page = 1, size = 10) =>
    request.get('/v1/orders/buy', { params: { page, size } }) as Promise<PageData<OrderVO>>,
  listSell: (page = 1, size = 10) =>
    request.get('/v1/orders/sell', { params: { page, size } }) as Promise<PageData<OrderVO>>,
  detail: (id: number) => request.get(`/v1/orders/${id}`) as Promise<OrderVO>,
}
