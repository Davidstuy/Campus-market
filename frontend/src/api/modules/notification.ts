import request from '@/utils/request'
import type { Notification, PageData } from '@/types'

export const notificationApi = {
  list(page = 1, size = 20): Promise<PageData<Notification>> {
    return request.get('/v1/notifications', { params: { page, size } })
  },

  getUnreadCount(): Promise<number> {
    return request.get('/v1/notifications/unread-count')
  },

  markAsRead(id: number): Promise<void> {
    return request.put(`/v1/notifications/${id}/read`)
  },

  markAllAsRead(): Promise<void> {
    return request.put('/v1/notifications/read-all')
  },

  markChatRead(): Promise<void> {
    return request.put('/v1/notifications/read-chat')
  },

  deleteOne(id: number): Promise<void> {
    return request.delete(`/v1/notifications/${id}`)
  },

  deleteAll(): Promise<void> {
    return request.delete('/v1/notifications/all')
  },
}
