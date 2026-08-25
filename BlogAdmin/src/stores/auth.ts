import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/admin'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('blog_admin_token') || '')
  const username = ref(localStorage.getItem('blog_admin_username') || '')
  const loggedIn = computed(() => Boolean(token.value))
  let validatedToken = ''

  async function login(name: string, password: string) {
    const res = await authApi.login({ username: name, password })
    applySession(res.token, res.username)
  }

  function applySession(nextToken: string, name: string) {
    token.value = nextToken
    username.value = name
    validatedToken = nextToken
    localStorage.setItem('blog_admin_token', nextToken)
    localStorage.setItem('blog_admin_username', name)
  }

  async function validateSession() {
    if (!token.value) return false
    if (validatedToken === token.value) return true
    try {
      await authApi.session()
      validatedToken = token.value
      return true
    } catch {
      logout()
      return false
    }
  }

  function logout() {
    token.value = ''
    username.value = ''
    validatedToken = ''
    localStorage.removeItem('blog_admin_token')
    localStorage.removeItem('blog_admin_username')
  }

  return { token, username, loggedIn, login, logout, applySession, validateSession }
})
