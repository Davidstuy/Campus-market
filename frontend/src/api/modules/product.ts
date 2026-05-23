import request from '@/utils/request'
import type { ProductForm, ProductQuery, PageData, Product } from '@/types'

export const productApi = {
  list: (query: ProductQuery) => request.get('/v1/products', { params: query }) as Promise<PageData<Product>>,
  detail: (id: number) => request.get(`/v1/products/${id}`) as Promise<Product>,
  create: (data: ProductForm) => request.post('/v1/products', data),
  update: (id: number, data: Partial<ProductForm>) => request.put(`/v1/products/${id}`, data),
  delete: (id: number) => request.delete(`/v1/products/${id}`),
  updateStatus: (id: number, status: string) => request.put(`/v1/products/${id}/status`, { status }),
  mine: (query: ProductQuery) => request.get('/v1/products/mine', { params: query }) as Promise<PageData<Product>>,
}
