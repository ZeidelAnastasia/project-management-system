import { useEffect, useState } from 'react'
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, IconButton, InputLabel, MenuItem, Select, Table, TableBody,
  TableCell, TableHead, TableRow, Typography, Tooltip, Alert,
} from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import BlockIcon from '@mui/icons-material/Block'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import { getUsers, updateUserRole, updateUserStatus } from '../api/services'
import { useAuthStore } from '../store/authStore'
import type { User, Role } from '../types'

const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Администратор', PROJECT_MANAGER: 'Менеджер', EMPLOYEE: 'Сотрудник',
}

export default function UsersPage() {
  const { hasRole } = useAuthStore()
  const [users, setUsers] = useState<User[]>([])
  const [editUser, setEditUser] = useState<User | null>(null)
  const [newRole, setNewRole] = useState<Role>('EMPLOYEE')

  if (!hasRole('ADMIN')) return <Alert severity="error">Доступ запрещён</Alert>

  const load = () => getUsers().then(setUsers).catch(() => {})
  useEffect(() => { load() }, [])

  const handleEditRole = async () => {
    if (!editUser) return
    await updateUserRole(editUser.id, newRole)
    setEditUser(null)
    load()
  }

  const handleToggleStatus = async (user: User) => {
    await updateUserStatus(user.id, !user.isActive)
    load()
  }

  return (
    <Box>
      <Typography variant="h5" mb={3}>Управление пользователями</Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Email</TableCell>
            <TableCell>Роли</TableCell>
            <TableCell>Статус</TableCell>
            <TableCell>Дата регистрации</TableCell>
            <TableCell>Действия</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {users.map(u => (
            <TableRow key={u.id}>
              <TableCell>{u.email}</TableCell>
              <TableCell>
                <Box display="flex" gap={0.5} flexWrap="wrap">
                  {u.roles.map(r => <Chip key={r} label={ROLE_LABELS[r]} size="small" color={r === 'ADMIN' ? 'error' : r === 'PROJECT_MANAGER' ? 'primary' : 'default'} />)}
                </Box>
              </TableCell>
              <TableCell>
                <Chip label={u.isActive ? 'Активен' : 'Деактивирован'} color={u.isActive ? 'success' : 'default'} size="small" />
              </TableCell>
              <TableCell>{new Date(u.createdAt).toLocaleDateString('ru-RU')}</TableCell>
              <TableCell>
                <Tooltip title="Изменить роль">
                  <IconButton size="small" onClick={() => { setEditUser(u); setNewRole(u.roles[0] || 'EMPLOYEE') }}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title={u.isActive ? 'Деактивировать' : 'Активировать'}>
                  <IconButton size="small" color={u.isActive ? 'error' : 'success'} onClick={() => handleToggleStatus(u)}>
                    {u.isActive ? <BlockIcon fontSize="small" /> : <CheckCircleIcon fontSize="small" />}
                  </IconButton>
                </Tooltip>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={Boolean(editUser)} onClose={() => setEditUser(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Изменить роль: {editUser?.email}</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>Роль</InputLabel>
            <Select value={newRole} label="Роль" onChange={e => setNewRole(e.target.value as Role)}>
              <MenuItem value="ADMIN">Администратор</MenuItem>
              <MenuItem value="PROJECT_MANAGER">Менеджер проекта</MenuItem>
              <MenuItem value="EMPLOYEE">Сотрудник</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditUser(null)}>Отмена</Button>
          <Button variant="contained" onClick={handleEditRole}>Сохранить</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
