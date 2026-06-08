import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const name = ref(localStorage.getItem('name') || '')

  function setUserInfo(userInfo) {
    token.value = userInfo.token
    userId.value = userInfo.userId
    username.value = userInfo.username
    role.value = userInfo.role
    name.value = userInfo.name

    localStorage.setItem('token', userInfo.token)
    localStorage.setItem('userId', userInfo.userId)
    localStorage.setItem('username', userInfo.username)
    localStorage.setItem('role', userInfo.role)
    localStorage.setItem('name', userInfo.name)
  }

  async function logout() {
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('退出登录请求失败:', error)
    } finally {
      clearUserInfo()
    }
  }

  function clearUserInfo() {
    token.value = ''
    userId.value = ''
    username.value = ''
    role.value = ''
    name.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('name')
  }

  return {
    token,
    userId,
    username,
    role,
    name,
    setUserInfo,
    logout,
    clearUserInfo
  }
})
