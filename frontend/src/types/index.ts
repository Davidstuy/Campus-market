// ============ 用户 ============
export interface User {
  id: number
  username: string
  nickname: string
  avatarUrl: string
  phone: string
  wechat: string
  qq: string
  role: 'USER' | 'ADMIN'
  status: 'ACTIVE' | 'BANNED'
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
  status: 'PENDING_REVIEW' | 'ACTIVE' | 'REJECTED' | 'SOLD' | 'DELISTED'
  reviewReason?: string
  riskLevel?: 'LOW' | 'HIGH'
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
  sellerId?: number
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

// ============ 订单 ============
export type OrderStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED'

export interface OrderVO {
  id: number
  orderNo: string
  buyerId: number
  sellerId: number
  productId: number
  productTitle: string
  productPrice: number
  productCover: string
  status: OrderStatus
  buyerRemark: string
  paidAt: string | null
  shippedAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  createdAt: string
  updatedAt: string
  buyer?: User
  seller?: User
}

export interface CreateOrderRequest {
  productId: number
  buyerRemark?: string
}

// ============ 聊天 ============
export interface Conversation {
  id: number
  buyerId: number
  sellerId: number
  productId: number
  type?: string
  productTitle: string
  productCover: string
  lastMessage: string
  lastMessageAt: string | null
  unreadCount: number
  otherPartyName: string
  otherPartyAvatar: string
  createdAt: string
}

export interface ChatMessage {
  id: number
  conversationId: number
  senderId: number
  receiverId: number
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'VIDEO'
  imageUrl?: string
  videoUrl?: string
  isRead: number
  createdAt: string
}

// ============ 评论 ============
export interface Comment {
  id: number
  productId: number
  userId: number
  content: string
  imageUrl?: string
  videoUrl?: string
  parentId: number | null
  replyToUserId: number | null
  replyToUser?: User | null
  replies?: Comment[]
  createdAt: string
  user?: User
  upCount: number
  downCount: number
  myVote: number | null  // 1=赞 -1=踩 null=未投票
}

// ============ 社区 ============
export interface Topic {
  id: number
  name: string
  icon: string
  sortOrder: number
  postCount: number
}

export interface Post {
  id: number
  userId: number
  topicId: number
  title: string
  content: string
  createdAt: string
  updatedAt: string
  user?: User
  topic?: Topic
  media?: PostMedia[]
  commentCount: number
}

export interface PostMedia {
  id: number
  postId: number
  mediaType: 'IMAGE' | 'VIDEO'
  url: string
  sortOrder: number
}

export interface PostComment {
  id: number
  postId: number
  userId: number
  content: string
  imageUrl: string | null
  videoUrl: string | null
  parentId: number | null
  replyToUserId: number | null
  createdAt: string
  user?: User
  replyToUser?: User | null
  replies?: PostComment[]
}

export interface CreatePostRequest {
  title: string
  content: string
  topicId: number
  media: string[]
}

// ============ 通知 ============
export interface Notification {
  id: number
  type: string
  title: string
  content: string
  orderId: number | null
  isRead: number
  createdAt: string
}

// ============ 常见问题 ============
export interface Faq {
  id: number
  question: string
  answer: string
  sortOrder: number
  createdAt: string
  updatedAt: string
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
