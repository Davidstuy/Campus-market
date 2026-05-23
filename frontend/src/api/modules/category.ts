import request from '@/utils/request'
import type { Category } from '@/types'

export const categoryApi = {
  list: () => request.get('/v1/categories') as Promise<Category[]>,
}

export const fileApi = {
  upload: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/v1/files/upload', form) as Promise<{ url: string }>
  },
}
