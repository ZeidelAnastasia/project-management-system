import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Box, Button, Chip, Tab, Tabs, Typography, TextField, Select, MenuItem,
  FormControl, InputLabel, CircularProgress, Alert, Paper, Divider,
  IconButton, Tooltip, Dialog, DialogTitle, DialogContent, DialogActions,
} from '@mui/material'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import SendIcon from '@mui/icons-material/Send'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import PsychologyIcon from '@mui/icons-material/Psychology'
import {
  getTask, updateTaskStatus, updateTaskAssignee, updateTaskDeadline, updateTaskEffort,
  getTaskHistory, getComments, createComment, updateComment, deleteComment,
  getTaskAnalysis, runTaskAnalysis, getProjectMembers,
} from '../api/services'
import { useAuthStore } from '../store/authStore'
import type { Task, Comment, TaskHistory, TaskAnalysis, TaskStatus } from '../types'

const STATUS_OPTIONS: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE']
const STATUS_LABELS: Record<TaskStatus, string> = { TODO: 'К выполнению', IN_PROGRESS: 'В работе', DONE: 'Завершена' }

export default function TaskDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user, hasRole } = useAuthStore()

  const [task, setTask] = useState<Task | null>(null)
  const [comments, setComments] = useState<Comment[]>([])
  const [history, setHistory] = useState<TaskHistory[]>([])
  const [analysis, setAnalysis] = useState<TaskAnalysis | null>(null)
  const [tab, setTab] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [newComment, setNewComment] = useState('')
  const [editCommentId, setEditCommentId] = useState<string | null>(null)
  const [editCommentBody, setEditCommentBody] = useState('')
  const [projectMembers, setProjectMembers] = useState<{ userId: string; email?: string }[]>([])

  const [assigneeDialog, setAssigneeDialog] = useState(false)
  const [deadlineDialog, setDeadlineDialog] = useState(false)
  const [effortDialog, setEffortDialog] = useState(false)
  const [newAssignee, setNewAssignee] = useState('')
  const [newDeadline, setNewDeadline] = useState('')
  const [newEstimated, setNewEstimated] = useState('')
  const [newActual, setNewActual] = useState('')

  const isAdmin = hasRole('ADMIN')
  const isAssignee = task?.assigneeId === user?.id

  const load = async () => {
    if (!id) return
    setLoading(true)
    try {
      const t = await getTask(id)
      setTask(t)
      const [c, h] = await Promise.all([getComments(id), getTaskHistory(id)])
      setComments(c)
      setHistory(h)
      if (t.projectId) {
        getProjectMembers(t.projectId).then(setProjectMembers).catch(() => {})
      }
    } catch { setError('Ошибка загрузки задачи') }
    finally { setLoading(false) }
  }

  const loadAnalysis = () => {
    if (!id) return
    getTaskAnalysis(id).then(setAnalysis).catch(() => setAnalysis(null))
  }

  useEffect(() => { load() }, [id])
  useEffect(() => { if (tab === 2) loadAnalysis() }, [tab])

  if (loading) return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>
  if (error || !task) return <Alert severity="error">{error || 'Задача не найдена'}</Alert>

  const canChangeStatus = isAdmin || isAssignee
  const canManageTask = isAdmin // simplified; real check needs project membership

  const handleStatusChange = async (status: string) => {
    await updateTaskStatus(id!, status)
    load()
  }

  const handleSendComment = async () => {
    if (!newComment.trim()) return
    await createComment(id!, newComment)
    setNewComment('')
    const c = await getComments(id!)
    setComments(c)
  }

  const handleEditComment = async (commentId: string) => {
    await updateComment(commentId, editCommentBody)
    setEditCommentId(null)
    const c = await getComments(id!)
    setComments(c)
  }

  const handleDeleteComment = async (commentId: string) => {
    await deleteComment(commentId)
    const c = await getComments(id!)
    setComments(c)
  }

  const handleRunAnalysis = async () => {
    const a = await runTaskAnalysis(id!)
    setAnalysis(a)
  }

  const handleUpdateAssignee = async () => {
    await updateTaskAssignee(id!, newAssignee || null)
    setAssigneeDialog(false)
    load()
  }

  const handleUpdateDeadline = async () => {
    await updateTaskDeadline(id!, newDeadline || null)
    setDeadlineDialog(false)
    load()
  }

  const handleUpdateEffort = async () => {
    await updateTaskEffort(id!, newEstimated ? Number(newEstimated) : undefined, newActual ? Number(newActual) : undefined)
    setEffortDialog(false)
    load()
  }

  const complexityColor: Record<string, 'success' | 'warning' | 'error'> = { LOW: 'success', MEDIUM: 'warning', HIGH: 'error' }

  return (
    <Box>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(`/projects/${task.projectId}`)} sx={{ mb: 2 }}>
        Назад к проекту
      </Button>

      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
        <Box flex={1}>
          <Typography variant="h5">{task.title}</Typography>
          <Typography variant="body2" color="text.secondary" mt={0.5}>{task.description}</Typography>
        </Box>
        <Box display="flex" gap={1} flexWrap="wrap" justifyContent="flex-end" ml={2}>
          {canChangeStatus && (
            <FormControl size="small" sx={{ minWidth: 150 }}>
              <InputLabel>Статус</InputLabel>
              <Select value={task.status} label="Статус" onChange={e => handleStatusChange(e.target.value)}>
                {STATUS_OPTIONS.map(s => <MenuItem key={s} value={s}>{STATUS_LABELS[s]}</MenuItem>)}
              </Select>
            </FormControl>
          )}
          {canManageTask && (
            <>
              <Button size="small" onClick={() => { setNewAssignee(task.assigneeId || ''); setAssigneeDialog(true) }}>Исполнитель</Button>
              <Button size="small" onClick={() => { setNewDeadline(task.dueDate || ''); setDeadlineDialog(true) }}>Срок</Button>
              <Button size="small" onClick={() => { setNewEstimated(String(task.estimatedEffort || '')); setNewActual(String(task.actualEffort || '')); setEffortDialog(true) }}>Трудозатраты</Button>
            </>
          )}
        </Box>
      </Box>

      <Box display="flex" gap={2} mb={3} flexWrap="wrap">
        <Chip label={`Приоритет: ${task.priority === 'LOW' ? 'Низкий' : task.priority === 'MEDIUM' ? 'Средний' : 'Высокий'}`} size="small" />
        {task.dueDate && <Chip label={`Срок: ${new Date(task.dueDate).toLocaleDateString('ru-RU')}`} size="small" />}
        {task.estimatedEffort && <Chip label={`Плановые: ${task.estimatedEffort} ч`} size="small" />}
        {task.actualEffort && <Chip label={`Фактические: ${task.actualEffort} ч`} size="small" />}
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label={`Комментарии (${comments.length})`} />
        <Tab label="История изменений" />
        <Tab label="Анализ задачи" />
      </Tabs>

      {tab === 0 && (
        <Box>
          {comments.map(c => (
            <Paper key={c.id} sx={{ p: 2, mb: 1 }}>
              <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                <Box flex={1}>
                  <Typography variant="caption" color="text.secondary">
                    {c.authorId} · {new Date(c.createdAt).toLocaleString('ru-RU')}
                  </Typography>
                  {editCommentId === c.id ? (
                    <Box display="flex" gap={1} mt={1}>
                      <TextField size="small" value={editCommentBody} onChange={e => setEditCommentBody(e.target.value)} fullWidth multiline />
                      <Button size="small" variant="contained" onClick={() => handleEditComment(c.id)}>Сохранить</Button>
                      <Button size="small" onClick={() => setEditCommentId(null)}>Отмена</Button>
                    </Box>
                  ) : (
                    <Typography variant="body2" mt={0.5}>{c.body}</Typography>
                  )}
                </Box>
                {(c.authorId === user?.id || isAdmin) && editCommentId !== c.id && (
                  <Box>
                    <Tooltip title="Редактировать">
                      <IconButton size="small" onClick={() => { setEditCommentId(c.id); setEditCommentBody(c.body) }}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Удалить">
                      <IconButton size="small" color="error" onClick={() => handleDeleteComment(c.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                )}
              </Box>
            </Paper>
          ))}
          <Box display="flex" gap={1} mt={2}>
            <TextField
              size="small" placeholder="Написать комментарий..." value={newComment}
              onChange={e => setNewComment(e.target.value)} fullWidth multiline maxRows={4}
              onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSendComment() } }}
            />
            <IconButton color="primary" onClick={handleSendComment} disabled={!newComment.trim()}>
              <SendIcon />
            </IconButton>
          </Box>
        </Box>
      )}

      {tab === 1 && (
        <Box>
          {history.length === 0 ? (
            <Typography color="text.secondary">История изменений отсутствует</Typography>
          ) : history.map(h => (
            <Paper key={h.id} sx={{ p: 1.5, mb: 1 }}>
              <Typography variant="caption" color="text.secondary">
                {new Date(h.changedAt).toLocaleString('ru-RU')} · {h.changedBy}
              </Typography>
              <Typography variant="body2">
                <strong>{h.changeType === 'STATUS' ? 'Статус' : h.changeType === 'ASSIGNEE' ? 'Исполнитель' : 'Срок'}:</strong>{' '}
                {h.oldValue || '—'} → {h.newValue || '—'}
              </Typography>
            </Paper>
          ))}
        </Box>
      )}

      {tab === 2 && (
        <Box>
          <Button variant="contained" startIcon={<PsychologyIcon />} onClick={handleRunAnalysis} sx={{ mb: 2 }}>
            Запустить анализ
          </Button>
          {analysis ? (
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" mb={2}>Результаты анализа</Typography>
              <Box display="flex" gap={2} flexWrap="wrap" mb={2}>
                <Box>
                  <Typography variant="caption" color="text.secondary">Оценка сложности</Typography>
                  <Typography variant="h4">{analysis.complexityScore.toFixed(2)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Категория сложности</Typography>
                  <Box mt={0.5}>
                    <Chip label={analysis.complexityLevel} color={complexityColor[analysis.complexityLevel]} />
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Прогноз трудозатрат</Typography>
                  <Typography variant="h4">{analysis.predictedEffort.toFixed(1)} ч</Typography>
                </Box>
              </Box>
              <Typography variant="caption" color="text.secondary">
                Версия модели: {analysis.modelVersion} · Анализ: {new Date(analysis.analyzedAt).toLocaleString('ru-RU')}
              </Typography>
            </Paper>
          ) : (
            <Typography color="text.secondary">Анализ ещё не выполнялся</Typography>
          )}
        </Box>
      )}

      {/* Assignee Dialog */}
      <Dialog open={assigneeDialog} onClose={() => setAssigneeDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Изменить исполнителя</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>Исполнитель</InputLabel>
            <Select value={newAssignee} label="Исполнитель" onChange={e => setNewAssignee(e.target.value)}>
              <MenuItem value="">Не назначен</MenuItem>
              {projectMembers.map(m => <MenuItem key={m.userId} value={m.userId}>{m.email || m.userId}</MenuItem>)}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAssigneeDialog(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleUpdateAssignee}>Сохранить</Button>
        </DialogActions>
      </Dialog>

      {/* Deadline Dialog */}
      <Dialog open={deadlineDialog} onClose={() => setDeadlineDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Изменить срок</DialogTitle>
        <DialogContent>
          <TextField type="date" value={newDeadline} onChange={e => setNewDeadline(e.target.value)} fullWidth sx={{ mt: 1 }} InputLabelProps={{ shrink: true }} label="Срок выполнения" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeadlineDialog(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleUpdateDeadline}>Сохранить</Button>
        </DialogActions>
      </Dialog>

      {/* Effort Dialog */}
      <Dialog open={effortDialog} onClose={() => setEffortDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Трудозатраты</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="Плановые (ч)" type="number" value={newEstimated} onChange={e => setNewEstimated(e.target.value)} fullWidth />
            <TextField label="Фактические (ч)" type="number" value={newActual} onChange={e => setNewActual(e.target.value)} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEffortDialog(false)}>Отмена</Button>
          <Button variant="contained" onClick={handleUpdateEffort}>Сохранить</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
