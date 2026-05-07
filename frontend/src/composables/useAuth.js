import { reactive } from 'vue'
import { login as apiLogin, register as apiRegister, fetchAuthStatus } from '../api/auth.js'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

function loadPersisted() {
  const token = localStorage.getItem(TOKEN_KEY)
  let user = null
  try {
    const raw = localStorage.getItem(USER_KEY)
    if (raw) user = JSON.parse(raw)
  } catch {
    user = null
  }
  return { token, user }
}

const state = reactive({
  token: null,
  user: null,
  loading: false,
  error: null,
})

// Restore from localStorage on module load
const persisted = loadPersisted()
state.token = persisted.token
state.user = persisted.user

function persist(token, user) {
  state.token = token
  state.user = user
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
}

export function useAuth() {
  const isLoggedIn = () => !!state.token

  async function login(username, password) {
    state.loading = true
    state.error = null
    try {
      const data = await apiLogin(username, password)
      persist(data.token, { username: data.username, displayName: data.displayName })
      return data
    } catch (e) {
      state.error = e.message
      throw e
    } finally {
      state.loading = false
    }
  }

  async function register(username, password, displayName) {
    state.loading = true
    state.error = null
    try {
      const data = await apiRegister(username, password, displayName)
      persist(data.token, { username: data.username, displayName: data.displayName })
      return data
    } catch (e) {
      state.error = e.message
      throw e
    } finally {
      state.loading = false
    }
  }

  function logout() {
    persist(null, null)
  }

  async function checkStatus() {
    return await fetchAuthStatus()
  }

  function clearError() {
    state.error = null
  }

  return {
    state,
    isLoggedIn,
    login,
    register,
    logout,
    checkStatus,
    clearError,
  }
}
