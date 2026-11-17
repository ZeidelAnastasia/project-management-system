import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Button, Card, CardActionArea, CardContent, Chip, Dialog, DialogActions,
  DialogContent, DialogTitle, Grid, MenuItem, Select, TextField, Typography,
  FormControl, InputLabel, InputAdornment, IconButton,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import SearchIcon from '@mui/icons-material/Search'
import { getProjects, createProject } from '../api/services'
import { useAuthStore } from '../store/authStore'
import type { Project, ProjectStatus } from '../types'

const STATUS_LABELS: Record<ProjectStatus, string> = {
  ACTIVE: 'Активный', ARCHIVED: 'Архивный', COMPLETED: 'Завершён',
}
const STATUS_COLORS: Record<ProjectStatus, 'success' | 'default' | 'info'> = {
  ACTIVE: 'success', ARCHIVED: 'default', COMPLETED: 'info',
}

export default function ProjectsPage() {
  const navigate = useNavigate()
  const { hasRole } = useAuthStore()
  const canCreate = hasRole('ADMIN') || hasRole('PROJECT_MANAGER')

  const [projects, setProjects] = useState<Project[]>([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })

  const load = () => getProjects().then(setProjects).catch(() => {})

  useEffect(() => { load() }, [])

  const filtered = projects.filter(p => {
    const matchSearch = p.name.toLowerCase().includes(search.toLowerCase())
    const matchStatus = statusFilter === 'ALL' || p.status === statusFilter
    return matchSearch && matchStatus
  })

  const handleCreate = async () => {
    await createProject(form)
    setOpen(false)
    setForm({ name: '', description: '' })
    load()
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Проекты</Typography>
        {canCreate && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>
            Создать проект
          </Button>
        )}
      </Box>

      <Box display="flex" gap={2} mb={3}>
        <TextField
          size="small" placeholder="Поиск по названию" value={search}
          onChange={e => setSearch(e.target.value)}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
          sx={{ width: 300 }}
        />
        <FormControl size="small" sx={{ width: 180 }}>
          <InputLabel>Статус</InputLabel>
          <Select value={statusFilter} label="Статус" onChange={e => setStatusFilter(e.target.value)}>
            <MenuItem value="ALL">Все</MenuItem>
            <MenuItem value="ACTIVE">Активные</MenuItem>
            <MenuItem value="ARCHIVED">Архивные</MenuItem>
            <MenuItem value="COMPLETED">Завершённые</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <Grid container spacing={2}>
        {filtered.map(p => (
          <Grid item xs={12} sm={6} md={4} key={p.id}>
            <Card>
              <CardActionArea onClick={() => navigate(`/projects/${p.id}`)}>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                    <Typography variant="h6" noWrap sx={{ maxWidth: '70%' }}>{p.name}</Typography>
                    <Chip label={STATUS_LABELS[p.status]} color={STATUS_COLORS[p.status]} size="small" />
                  </Box>
                  <Typography variant="body2" color="text.secondary" mt={1} sx={{ minHeight: 40, overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                    {p.description || 'Без описания'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" mt={1} display="block">
                    Создан: {new Date(p.createdAt).toLocaleDateString('ru-RU')}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
        {filtered.length === 0 && (
          <Grid item xs={12}>
            <Typography color="text.secondary" textAlign="center" mt={4}>Проекты не найдены</Typography>
          </Grid>
        )}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Создать проект</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="Название" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required fullWidth />
            <TextField label="Описание" value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} multiline rows={3} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!form.name}>Создать</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
