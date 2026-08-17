import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/admin'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('blog_admin_token') || '')
  const username = ref(localStorage.getItem('blog_admin_username') || '')
  const loggedIn = computed(() => Boolean(token.value))

  async function login(name: string, password: string) {
    const res = await authApi.login({ username: name, password })
    applySession(res.token, res.username)
  }

  function applySession(nextToken: string, name: string) {
    token.value = nextToken
    username.value = name
    localStorage.setItem('blog_admin_token', nextToken)
    localStorage.setItem('blog_admin_username', name)
  }

  function logout() {
    token.value = ''
    username.value = ''
    localStorage.removeItem('blog_admin_token')
    localStorage.removeItem('blog_admin_username')
  }

  return { token, username, loggedIn, login, logout, applySession }
})
