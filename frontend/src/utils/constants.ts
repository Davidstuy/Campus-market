/** 商品状态 */
export const PRODUCT_STATUS = {
  ACTIVE: '在售',
  SOLD: '已售出',
  DELISTED: '已下架',
} as const

export const PRODUCT_STATUS_OPTIONS = [
  { value: 'ACTIVE', label: '在售' },
  { value: 'SOLD', label: '已售出' },
  { value: 'DELISTED', label: '已下架' },
]

/** 订单状态 */
export const ORDER_STATUS: Record<string, string> = {
  PENDING: '待付款',
  PAID: '待发货',
  SHIPPED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

export const ORDER_STATUS_TAG_TYPE: Record<string, string> = {
  PENDING: 'warning',
  PAID: 'success',
  SHIPPED: 'primary',
  COMPLETED: 'info',
  CANCELLED: 'danger',
}

/** 排序选项 */
export const SORT_OPTIONS = [
  { value: 'latest', label: '最新发布' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
]
