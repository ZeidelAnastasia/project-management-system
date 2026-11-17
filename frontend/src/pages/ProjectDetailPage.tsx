import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Box, Button, Chip, Tab, Tabs, Typography, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, Select, FormControl, InputLabel,
  Table, TableBody, TableCell, TableHead, TableRow, IconButton, Alert,
  CircularProgress, Tooltip,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import {
  getProject, updateProject, archiveProject, restoreProject, completeProject,
  getProjectMembers, addProjectMember, removeProjectMember,
  getProjectTasks, createTask, deleteTask,
  getUsers,
} from '../api/services'
import { useAuthStore } from '../store/authStore'
import type { Project, ProjectMember, Task, User, TaskPriority } from '../types'

const PRIORITY_LABELS: Record<TaskPriority, string> = { LOW: 'Низкий', MEDIUM: 'Средний', HIGH: 'Высокий' }
const PRIORITY_COLORS: Record<TaskPriority, 'default' | 'warning' | 'error'> = { LOW: 'default', MEDIUM: 'warning', HIGH: 'error' }

export default function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user, hasRole } = useAuthStore()

  const [project, setProject] = useState<Project | null>(null)
  const [members, setMembers] = useState<ProjectMember[]>([])
  const [tasks, setTasks] = useState<Task[]>([])
  const [allUsers, setAllUsers] = useState<User[]>([])
  const [tab, setTab] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [editOpen, setEditOpen] = useState(false)
  const [editForm, setEditForm] = useState({ name: '', description: '' })

  const [taskOpen, setTaskOpen] = useState(false)
  const [taskForm, setTaskForm] = useState({ title: '', description: '', priority: 'MEDIUM' as TaskPriority, dueDate: '', estimatedEffort: '' })

  const [memberOpen, setMemberOpen] = useState(false)
  const [memberUserId, setMemberUserId] = useState('')
  const [memberRole, setMemberRole] = useState('MEMBER')

  const [confirmComplete, setConfirmComplete] = useState(false)

  const isAdmin = hasRole('ADMIN')
  const isManager = members.some(m => m.userId === user?.id && m.projectRole === 'MANAGER')
  const canManage = isAdmin || isManager

  const load = async () => {
    if (!id) return
    setLoading(true)
    try {
      const [p, m, t] = await Promise.all([getProject(id), getProjectMembers(id), getProjectTasks(id)])
      setProject(p)
      setMembers(m)
      setTasks(t)
      setEditForm({ name: p.name, description: p.description })
    } catch { setError('Ошибка загрузки проекта') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [id])
  useEffect(() => { if (canManage) getUsers().then(setAllUsers).catch(() => {}) }, [canManage])

  if (loading) return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>
  if (error || !project) return <Alert severity="error">{error || 'Проект не найден'}</Alert>

  const handleEdit = async () => {
    await updateProject(id!, editForm)
    setEditOpen(false)
    load()
  }

  const handleArchive = async () => { await archiveProject(id!); load() }
  const handleRestore = async () => { await restoreProject(id!); load() }
  const handleComplete = async (force = false) => {
    await completeProject(id!, force)
    setConfirmComplete(false)
    load()
  }

  const handleCreateTask = async () => {
    await createTask(id!, { ...taskForm, estimatedEffort: taskForm.estimatedEffort ? Number(taskForm.estimatedEffort) : undefined, dueDate: taskForm.dueDate || undefined })
    setTaskOpen(false)
    setTaskForm({ title: '', description: '', priority: 'MEDIUM', dueDate: '', estimatedEffort: '' })
    load()
  }

  const handleDeleteTask = async (taskId: string) => {
    await deleteTask(taskId)
    load()
  }

  const handleAddMember = async () => {
    await addProjectMember(id!, memberUserId, memberRole)
    setMemberOpen(false)
    setMemberUserId('')
    load()
  }

  const handleRemoveMember = async (userId: string) => {
    await removeProjectMember(id!, userId)
    load()
  }

  const statusLabel: Record<string, string> = { ACTIVE: 'Активный', ARCHIVED: 'Архивный', COMPLETED: 'Завершён' }
  const statusColor: Record<string, 'success' | 'default' | 'info'> = { ACTIVE: 'success', ARCHIVED: 'default', COMPLETED: 'info' }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
        <Box>
          <Box display="flex" alignItems="center" gap={2}>
            <Typography variant="h5">{project.name}</Typography>
            <Chip label={statusLabel[project.status]} color={statusColor[project.status]} size="small" />
          </Box>
          <Typography variant="body2" color="text.secondary" mt={0.5}>{project.description}</Typography>
        </Box>
        {canManage && (
          <Box display="flex" gap={1} flexWrap="wrap" justifyContent="flex-end">
            <Button size="small" startIcon={<EditIcon />} onClick={() => setEditOpen(true)}>Редактировать</Button>
            {project.status === 'ACTIVE' && <Button size="small" onClick={handleArchive}>Архивировать</Button>}
            {project.status === 'ARCHIVED' && <Button size="small" onClick={handleRestore}>Восстановить</Button>}
            {project.status === 'ACTIVE' && isManager && (
              <Button size="small" color="success" onClick={() => handleComplete(false)}>Завершить</Button>
            )}
            {project.status === 'ACTIVE' && isAdmin && (
              <Button size="small" color="warning" onClick={() => setConfirmComplete(true)}>Завершить принудительно</Button>
            )}
          </Box>
        )}
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label={`Задачи (${tasks.length})`} />
        <Tab label={`Участники (${members.length})`} />
      </Tabs>

      {tab === 0 && (
        <Box>
          {canManage && project.status === 'ACTIVE' && (
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setTaskOpen(true)} sx={{ mb: 2 }}>
              Создать задачу
            </Button>
          )}
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Название</TableCell>
                <TableCell>Статус</TableCell>
                <TableCell>Приоритет</TableCell>
                <TableCell>Срок</TableCell>
                {canManage && <TableCell>Действия</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {tasks.map(t => (
                <TableRow key={t.id} hover sx={{ cursor: 'pointer' }}>
                  <TableCell onClick={() => navigate(`/tasks/${t.id}`)}>{t.title}</TableCell>
                  <TableCell onClick={() => navigate(`/tasks/${t.id}`)}>
                    <Chip label={t.status} size="small" color={t.status === 'DONE' ? 'success' : t.status === 'IN_PROGRESS' ? 'warning' : 'default'} />
                  </TableCell>
                  <TableCell onClick={() => navigate(`/tasks/${t.id}`)}>
                    <Chip label={PRIORITY_LABELS[t.priority]} size="small" color={PRIORITY_COLORS[t.priority]} />
                  </TableCell>
                  <TableCell onClick={() => navigate(`/tasks/${t.id}`)}>
                    {t.dueDate ? new Date(t.dueDate).toLocaleDateString('ru-RU') : '—'}
                  </TableCell>
                  {canManage && (
                    <TableCell>
                      <Tooltip title="Удалить">
                        <IconButton size="small" color="error" onClick={() => handleDeleteTask(t.id)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  )}
                </TableRow>
              ))}
              {tasks.length === 0 && (
                <TableRow><TableCell colSpan={5} align="center">Задачи отсутствуют</TableCell></TableRow>
              )}
            </TableBody>
          </Table>
        </Box>
      )}

      {tab === 1 && (
        <Box>
          {canManage && (
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setMemberOpen(true)} sx={{ mb: 2 }}>
              Добавить участника
            </Button>
          )}
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID пользователя</TableCell>
                <TableCell>Роль в проекте</TableCell>
                <TableCell>Добавлен</TableCell>
                {canManage && <TableCell>Действия</TableCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {members.map(m => (
                <TableRow key={m.userId}>
                  <TableCell>{m.email || m.userId}</TableCell>
                  <TableCell><Chip label={m.projectRole === 'MANAGER' ? 'Менеджер' : 'Участник'} size="small" color={m.projectRole === 'MANAGER' ? 'primary' : 'default'} /></TableCell>
                  <TableCell>{new Date(m.joinedAt).toLocaleDateString('ru-RU')}</TableCell>
                  {canManage && (
                    <TableCell>
                      <Tooltip title="Исключить">
                        <IconButton size="small" color="error" onClick={() => handleRemoveMember(m.userId)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>
      )}

      {/* Edit Project Dialog */}
      <Dialog open={editOpen} onClose={() => setEditOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Редактировать проект</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="Название" value={editForm.name} onChange={e => setEditForm(f => ({ ...f, name: e.target.value }))} fullWidth />
            <TextField label="Описание" value={editForm.description} onChange={e => setEditForm(f => ({ ...f, description: e.target.value }))} multiline rows={3} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditOpen(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleEdit}>Сохранить</Button>
        </DialogActions>
      </Dialog>

      {/* Create Task Dialog */}
      <Dialog open={taskOpen} onClose={() => setTaskOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Создать задачу</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="Название" value={taskForm.title} onChange={e => setTaskForm(f => ({ ...f, title: e.target.value }))} required fullWidth />
            <TextField label="Описание" value={taskForm.description} onChange={e => setTaskForm(f => ({ ...f, description: e.target.value }))} multiline rows={2} fullWidth />
            <FormControl fullWidth>
              <InputLabel>Приоритет</InputLabel>
              <Select value={taskForm.priority} label="Приоритет" onChange={e => setTaskForm(f => ({ ...f, priority: e.target.value as TaskPriority }))}>
                <MenuItem value="LOW">Низкий</MenuItem>
                <MenuItem value="MEDIUM">Средний</MenuItem>
                <MenuItem value="HIGH">Высокий</MenuItem>
              </Select>
            </FormControl>
            <TextField label="Срок выполнения" type="date" value={taskForm.dueDate} onChange={e => setTaskForm(f => ({ ...f, dueDate: e.target.value }))} InputLabelProps={{ shrink: true }} fullWidth />
            <TextField label="Плановые трудозатраты (ч)" type="number" value={taskForm.estimatedEffort} onChange={e => setTaskForm(f => ({ ...f, estimatedEffort: e.target.value }))} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTaskOpen(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleCreateTask} disabled={!taskForm.title}>Создать</Button>
        </DialogActions>
      </Dialog>

      {/* Add Member Dialog */}
      <Dialog open={memberOpen} onClose={() => setMemberOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Добавить участника</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <FormControl fullWidth>
              <InputLabel>Пользователь</InputLabel>
              <Select value={memberUserId} label="Пользователь" onChange={e => setMemberUserId(e.target.value)}>
                {allUsers.filter(u => !members.some(m => m.userId === u.id)).map(u => (
                  <MenuItem key={u.id} value={u.id}>{u.email}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel>Роль</InputLabel>
              <Select value={memberRole} label="Роль" onChange={e => setMemberRole(e.target.value)}>
                <MenuItem value="MEMBER">Участник</MenuItem>
                <MenuItem value="MANAGER">Менеджер</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMemberOpen(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleAddMember} disabled={!memberUserId}>Добавить</Button>
        </DialogActions>
      </Dialog>

      {/* Force Complete Confirm */}
      <Dialog open={confirmComplete} onClose={() => setConfirmComplete(false)}>
        <DialogTitle>Принудительное завершение</DialogTitle>
        <DialogContent>
          <Typography>В проекте есть незавершённые задачи. Завершить проект принудительно?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmComplete(false)}>Отмена</Button>
          <Button variant="contained" color="warning" onClick={() => handleComplete(true)}>Завершить</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
