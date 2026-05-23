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

/** 排序选项 */
export const SORT_OPTIONS = [
  { value: 'latest', label: '最新发布' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
]
