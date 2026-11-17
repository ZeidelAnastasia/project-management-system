import { useEffect, useState } from 'react'
import {
  Box, Button, Chip, List, ListItem, ListItemText, Typography, Paper, Divider,
} from '@mui/material'
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '../api/services'
import type { Notification } from '../types'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])

  const load = () => getNotifications().then(setNotifications).catch(() => {})

  useEffect(() => { load() }, [])

  const handleMarkRead = async (id: string) => {
    await markNotificationRead(id)
    load()
  }

  const handleMarkAll = async () => {
    await markAllNotificationsRead()
    load()
  }

  const unread = notifications.filter(n => !n.readAt)

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Уведомления</Typography>
        {unread.length > 0 && (
          <Button variant="outlined" onClick={handleMarkAll}>Отметить все как прочитанные</Button>
        )}
      </Box>

      {notifications.length === 0 ? (
        <Typography color="text.secondary" textAlign="center" mt={4}>Уведомлений нет</Typography>
      ) : (
        <Paper>
          <List disablePadding>
            {notifications.map((n, i) => (
              <Box key={n.id}>
                {i > 0 && <Divider />}
                <ListItem
                  sx={{ bgcolor: n.readAt ? 'transparent' : 'action.hover' }}
                  secondaryAction={
                    !n.readAt && (
                      <Button size="small" onClick={() => handleMarkRead(n.id)}>Прочитано</Button>
                    )
                  }
                >
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="subtitle2">{n.title}</Typography>
                        {!n.readAt && <Chip label="Новое" size="small" color="primary" />}
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="body2">{n.message}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(n.createdAt).toLocaleString('ru-RU')}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
              </Box>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  )
}
