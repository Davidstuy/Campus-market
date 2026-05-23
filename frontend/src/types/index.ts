// ============ 用户 ============
export interface User {
  id: number
  username: string
  nickname: string
  avatarUrl: string
  phone: string
  wechat: string
  qq: string
  createdAt: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
}

// ============ 分类 ============
export interface Category {
  id: number
  name: string
  icon: string
  sortOrder: number
}

// ============ 商品 ============
export interface Product {
  id: number
  title: string
  description: string
  price: number
  categoryId: number
  sellerId: number
  status: 'ACTIVE' | 'SOLD' | 'DELISTED'
  coverImage: string
  contactWechat: string
  contactQq: string
  createdAt: string
  updatedAt: string
  // 关联数据
  seller?: User
  category?: Category
  images?: ProductImage[]
  isFavorited?: boolean
}

export interface ProductImage {
  id: number
  productId: number
  url: string
  sortOrder: number
}

export interface ProductForm {
  title: string
  description: string
  price: number
  categoryId: number
  coverImage: string
  images: string[]
  contactWechat: string
  contactQq: string
}

export interface ProductQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  status?: string
  sort?: string
}

// ============ 收藏 ============
export interface Favorite {
  id: number
  userId: number
  productId: number
  createdAt: string
  product?: Product
}

// ============ 通用 ============
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageData<T> {
  records: T[]
  total: number
  page: number
  size: number
}
