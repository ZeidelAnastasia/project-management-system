import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  AppBar, Toolbar, Typography, Drawer, List, ListItemButton,
  ListItemIcon, ListItemText, IconButton, Badge, Box, Avatar,
  Menu, MenuItem, Divider,
} from '@mui/material'
import FolderIcon from '@mui/icons-material/Folder'
import NotificationsIcon from '@mui/icons-material/Notifications'
import BarChartIcon from '@mui/icons-material/BarChart'
import PsychologyIcon from '@mui/icons-material/Psychology'
import PeopleIcon from '@mui/icons-material/People'
import LogoutIcon from '@mui/icons-material/Logout'
import { useAuthStore } from '../store/authStore'
import { logout as apiLogout } from '../api/services'
import { useNotificationCount } from '../hooks/useNotificationCount'

const DRAWER_WIDTH = 220

export default function Layout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout, hasRole } = useAuthStore()
  const unreadCount = useNotificationCount()
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  const handleLogout = async () => {
    const rt = localStorage.getItem('refreshToken')
    if (rt) await apiLogout(rt).catch(() => {})
    logout()
    navigate('/login')
  }

  const navItems = [
    { label: 'Проекты', icon: <FolderIcon />, path: '/projects' },
    { label: 'Уведомления', icon: <Badge badgeContent={unreadCount} color="error"><NotificationsIcon /></Badge>, path: '/notifications' },
    { label: 'Отчёты', icon: <BarChartIcon />, path: '/reports' },
    { label: 'Анализ', icon: <PsychologyIcon />, path: '/intelligence' },
    ...(hasRole('ADMIN') ? [{ label: 'Пользователи', icon: <PeopleIcon />, path: '/users' }] : []),
  ]

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Управление проектами</Typography>
          <IconButton color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', fontSize: 14 }}>
              {user?.email?.[0]?.toUpperCase()}
            </Avatar>
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
            <MenuItem disabled><Typography variant="body2">{user?.email}</Typography></MenuItem>
            <MenuItem disabled><Typography variant="caption">{user?.roles.join(', ')}</Typography></MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}><ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>Выйти</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Drawer variant="permanent" sx={{ width: DRAWER_WIDTH, flexShrink: 0, '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' } }}>
        <Toolbar />
        <List>
          {navItems.map((item) => (
            <ListItemButton
              key={item.path}
              selected={location.pathname.startsWith(item.path)}
              onClick={() => navigate(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, p: 3, mt: 8 }}>
        <Outlet />
      </Box>
    </Box>
  )
}
