import request from '@/utils/request'
import type { Conversation, ChatMessage, PageData } from '@/types'

export const chatApi = {
  listConversations(): Promise<Conversation[]> {
    return request.get('/v1/chat/conversations')
  },

  getMessages(conversationId: number, page = 1, size = 30): Promise<PageData<ChatMessage>> {
    return request.get(`/v1/chat/conversations/${conversationId}/messages`, {
      params: { page, size },
    })
  },

  sendMessage(conversationId: number, receiverId: number, content: string): Promise<ChatMessage> {
    return request.post(`/v1/chat/conversations/${conversationId}/messages`, {
      receiverId,
      content,
    })
  },

  markRead(conversationId: number): Promise<void> {
    return request.put(`/v1/chat/conversations/${conversationId}/read`)
  },

  getOrCreateConversation(sellerId: number, productId: number): Promise<Conversation> {
    return request.post('/v1/chat/conversations', null, {
      params: { sellerId, productId },
    })
  },
}
