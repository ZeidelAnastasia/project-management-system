import { useState } from 'react'
import { useNavigate, Link as RouterLink } from 'react-router-dom'
import { Box, Button, TextField, Typography, Paper, Alert, Link } from '@mui/material'
import { register } from '../api/services'
import { useAuthStore } from '../store/authStore'

export default function RegisterPage() {
  const navigate = useNavigate()
  const fetchMe = useAuthStore(s => s.fetchMe)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const tokens = await register({ email, password })
      localStorage.setItem('accessToken', tokens.accessToken)
      localStorage.setItem('refreshToken', tokens.refreshToken)
      await fetchMe()
      navigate('/projects')
    } catch {
      setError('Ошибка регистрации. Возможно, email уже занят.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" bgcolor="grey.100">
      <Paper sx={{ p: 4, width: 360 }}>
        <Typography variant="h5" mb={3} textAlign="center">Регистрация</Typography>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box component="form" onSubmit={handleSubmit} display="flex" flexDirection="column" gap={2}>
          <TextField label="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} required fullWidth />
          <TextField label="Пароль" type="password" value={password} onChange={e => setPassword(e.target.value)} required fullWidth inputProps={{ minLength: 6 }} />
          <Button type="submit" variant="contained" fullWidth disabled={loading}>
            {loading ? 'Регистрация...' : 'Зарегистрироваться'}
          </Button>
          <Typography variant="body2" textAlign="center">
            Уже есть аккаунт? <Link component={RouterLink} to="/login">Войти</Link>
          </Typography>
        </Box>
      </Paper>
    </Box>
  )
}
