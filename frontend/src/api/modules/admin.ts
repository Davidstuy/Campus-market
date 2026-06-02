import request from '@/utils/request'
import type { User, Product, Category, Faq, PageData } from '@/types'

export interface DashboardStats {
  totalUsers: number
  totalProducts: number
  activeProducts: number
  pendingReview: number
  totalCategories: number
}

export const adminApi = {
  /** 数据看板 */
  getDashboard: () => request.get('/v1/admin/dashboard') as Promise<DashboardStats>,

  /** 待审商品列表 */
  getPendingProducts: (page = 1, size = 10) =>
    request.get('/v1/admin/products/pending', { params: { page, size } }) as Promise<PageData<Product>>,

  /** 通过审核 */
  approveProduct: (id: number) => request.put(`/v1/admin/products/${id}/approve`),

  /** 驳回商品 */
  rejectProduct: (id: number, reason: string) =>
    request.put(`/v1/admin/products/${id}/reject`, { reason }),

  /** 用户列表 */
  getUsers: (page = 1, size = 10, keyword?: string) =>
    request.get('/v1/admin/users', { params: { page, size, keyword } }) as Promise<PageData<User>>,

  /** 封禁用户 */
  banUser: (id: number) => request.put(`/v1/admin/users/${id}/ban`),

  /** 解封用户 */
  unbanUser: (id: number) => request.put(`/v1/admin/users/${id}/unban`),

  /** 新增分类 */
  createCategory: (data: { name: string; icon: string; sortOrder: number }) =>
    request.post('/v1/admin/categories', data) as Promise<Category>,

  /** 编辑分类 */
  updateCategory: (id: number, data: { name: string; icon: string; sortOrder: number }) =>
    request.put(`/v1/admin/categories/${id}`, data) as Promise<Category>,

  /** 删除分类 */
  deleteCategory: (id: number) => request.delete(`/v1/admin/categories/${id}`),

  /** FAQ 列表 */
  getFaqs: () => request.get('/v1/admin/faqs') as Promise<Faq[]>,

  /** 新增 FAQ */
  createFaq: (data: { question: string; answer: string; sortOrder: number }) =>
    request.post('/v1/admin/faqs', data) as Promise<Faq>,

  /** 编辑 FAQ */
  updateFaq: (id: number, data: { question: string; answer: string; sortOrder: number }) =>
    request.put(`/v1/admin/faqs/${id}`, data) as Promise<Faq>,

  /** 删除 FAQ */
  deleteFaq: (id: number) => request.delete(`/v1/admin/faqs/${id}`),
}
