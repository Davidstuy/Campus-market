import request from '@/utils/request'
import type { Faq } from '@/types'

export const faqApi = {
  list: () => request.get('/v1/faqs') as Promise<Faq[]>,
}
