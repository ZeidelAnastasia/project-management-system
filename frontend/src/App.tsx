import { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider, createTheme, CssBaseline, CircularProgress, Box } from '@mui/material'
import { LocalizationProvider } from '@mui/x-date-pickers'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { useAuthStore } from './store/authStore'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ProjectsPage from './pages/ProjectsPage'
import ProjectDetailPage from './pages/ProjectDetailPage'
import TaskDetailPage from './pages/TaskDetailPage'
import NotificationsPage from './pages/NotificationsPage'
import ReportsPage from './pages/ReportsPage'
import IntelligencePage from './pages/IntelligencePage'
import UsersPage from './pages/UsersPage'

const theme = createTheme({
  palette: {
    primary: { main: '#1976d2' },
    secondary: { main: '#dc004e' },
  },
})

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuthStore()
  if (loading) return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>
  if (!user) return <Navigate to="/login" replace />
  return <>{children}</>
}

export default function App() {
  const fetchMe = useAuthStore(s => s.fetchMe)

  useEffect(() => {
    if (localStorage.getItem('accessToken')) fetchMe()
    else useAuthStore.setState({ loading: false })
  }, [fetchMe])

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<RequireAuth><Layout /></RequireAuth>}>
              <Route index element={<Navigate to="/projects" replace />} />
              <Route path="projects" element={<ProjectsPage />} />
              <Route path="projects/:id" element={<ProjectDetailPage />} />
              <Route path="tasks/:id" element={<TaskDetailPage />} />
              <Route path="notifications" element={<NotificationsPage />} />
              <Route path="reports" element={<ReportsPage />} />
              <Route path="intelligence" element={<IntelligencePage />} />
              <Route path="users" element={<UsersPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </LocalizationProvider>
    </ThemeProvider>
  )
}
