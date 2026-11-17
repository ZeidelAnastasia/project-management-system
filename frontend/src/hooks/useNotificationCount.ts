import { useEffect, useState } from 'react'
import { getUnreadNotifications } from '../api/services'

export function useNotificationCount() {
  const [count, setCount] = useState(0)

  useEffect(() => {
    getUnreadNotifications().then(n => setCount(n.length)).catch(() => {})
    const interval = setInterval(() => {
      getUnreadNotifications().then(n => setCount(n.length)).catch(() => {})
    }, 30000)
    return () => clearInterval(interval)
  }, [])

  return count
}
