import request from '@/utils/request'
import type { LoginForm, RegisterForm } from '@/types'

export const authApi = {
  login: (data: LoginForm) => request.post('/v1/auth/login', data),
  register: (data: RegisterForm) => request.post('/v1/auth/register', data),
}

export const userApi = {
  getProfile: () => request.get('/v1/users/me'),
  updateProfile: (data: Record<string, string>) => request.put('/v1/users/me', data),
  uploadAvatar: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/v1/users/me/avatar', form)
  },
}
