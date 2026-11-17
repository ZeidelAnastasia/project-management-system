import { create } from 'zustand'
import type { User, Role } from '../types'
import { getMe } from '../api/services'

interface AuthState {
  user: User | null
  loading: boolean
  setUser: (user: User | null) => void
  fetchMe: () => Promise<void>
  logout: () => void
  hasRole: (role: Role) => boolean
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  loading: true,
  setUser: (user) => set({ user }),
  fetchMe: async () => {
    try {
      const user = await getMe()
      set({ user, loading: false })
    } catch {
      set({ user: null, loading: false })
    }
  },
  logout: () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    set({ user: null })
  },
  hasRole: (role) => get().user?.roles.includes(role) ?? false,
}))
