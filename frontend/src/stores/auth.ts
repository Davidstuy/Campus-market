import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, LoginForm, RegisterForm } from '@/types'
import { authApi, userApi } from '@/api/modules/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '')

  const isLoggedIn = () => !!token.value

  const fetchUser = async () => {
    if (!token.value) return
    try {
      const data = await userApi.getProfile() as User
      user.value = data
      localStorage.setItem('user', JSON.stringify(data))
    } catch {
      logout()
    }
  }

  const login = async (form: LoginForm) => {
    const data = await authApi.login(form) as { token: string; user: User }
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
  }

  const register = async (form: RegisterForm) => {
    await authApi.register(form)
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, token, isLoggedIn, fetchUser, login, register, logout }
})
