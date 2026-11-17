import { useEffect, useState } from 'react'
import {
  Box, Card, CardContent, FormControl, Grid, InputLabel, MenuItem,
  Select, Tab, Tabs, Typography, CircularProgress,
} from '@mui/material'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  LineChart, Line, PieChart, Pie, Cell, ResponsiveContainer,
} from 'recharts'
import {
  getReportOverview, getCompletionDynamics, getEmployeeWorkload,
  getProjectComparison, getProjects,
} from '../api/services'
import type { ReportOverview, CompletionDynamics, EmployeeWorkload, ProjectComparison, Project } from '../types'

const COLORS = ['#1976d2', '#dc004e', '#ff9800', '#4caf50', '#9c27b0']

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <Card>
      <CardContent>
        <Typography variant="h3" color="primary">{value}</Typography>
        <Typography variant="body2" color="text.secondary">{label}</Typography>
      </CardContent>
    </Card>
  )
}

export default function ReportsPage() {
  const [tab, setTab] = useState(0)
  const [overview, setOverview] = useState<ReportOverview | null>(null)
  const [dynamics, setDynamics] = useState<CompletionDynamics[]>([])
  const [workload, setWorkload] = useState<EmployeeWorkload[]>([])
  const [comparison, setComparison] = useState<ProjectComparison[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      getReportOverview().then(setOverview).catch(() => {}),
      getCompletionDynamics().then(setDynamics).catch(() => {}),
      getEmployeeWorkload().then(setWorkload).catch(() => {}),
      getProjectComparison().then(setComparison).catch(() => {}),
      getProjects().then(setProjects).catch(() => {}),
    ]).finally(() => setLoading(false))
  }, [])

  if (loading) return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>

  const pieData = overview ? [
    { name: 'Завершены', value: overview.completedTasks },
    { name: 'Просрочены', value: overview.overdueTasks },
    { name: 'Остальные', value: overview.totalTasks - overview.completedTasks - overview.overdueTasks },
  ] : []

  return (
    <Box>
      <Typography variant="h5" mb={3}>Аналитические отчёты</Typography>

      {overview && (
        <Grid container spacing={2} mb={3}>
          <Grid item xs={6} sm={4} md={2}><StatCard label="Всего проектов" value={overview.totalProjects} /></Grid>
          <Grid item xs={6} sm={4} md={2}><StatCard label="Активных" value={overview.activeProjects} /></Grid>
          <Grid item xs={6} sm={4} md={2}><StatCard label="Всего задач" value={overview.totalTasks} /></Grid>
          <Grid item xs={6} sm={4} md={2}><StatCard label="Завершено" value={overview.completedTasks} /></Grid>
          <Grid item xs={6} sm={4} md={2}><StatCard label="Просрочено" value={overview.overdueTasks} /></Grid>
        </Grid>
      )}

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Динамика выполнения" />
        <Tab label="Загрузка сотрудников" />
        <Tab label="Сравнение проектов" />
        <Tab label="Распределение задач" />
      </Tabs>

      {tab === 0 && (
        <Card>
          <CardContent>
            <Typography variant="h6" mb={2}>Динамика создания и завершения задач</Typography>
            <ResponsiveContainer width="100%" height={350}>
              <LineChart data={dynamics}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tickFormatter={d => new Date(d).toLocaleDateString('ru-RU', { month: 'short', day: 'numeric' })} />
                <YAxis />
                <Tooltip labelFormatter={d => new Date(d).toLocaleDateString('ru-RU')} />
                <Legend />
                <Line type="monotone" dataKey="created" name="Создано" stroke="#1976d2" />
                <Line type="monotone" dataKey="completed" name="Завершено" stroke="#4caf50" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {tab === 1 && (
        <Card>
          <CardContent>
            <Typography variant="h6" mb={2}>Загрузка сотрудников</Typography>
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={workload}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="email" tick={{ fontSize: 11 }} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="assignedTasks" name="Назначено" fill="#1976d2" />
                <Bar dataKey="completedTasks" name="Завершено" fill="#4caf50" />
                <Bar dataKey="overdueTasks" name="Просрочено" fill="#dc004e" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {tab === 2 && (
        <Card>
          <CardContent>
            <Typography variant="h6" mb={2}>Сравнение проектов</Typography>
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={comparison}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="projectName" tick={{ fontSize: 11 }} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="totalTasks" name="Всего задач" fill="#1976d2" />
                <Bar dataKey="completedTasks" name="Завершено" fill="#4caf50" />
                <Bar dataKey="overdueTasks" name="Просрочено" fill="#dc004e" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {tab === 3 && overview && (
        <Card>
          <CardContent>
            <Typography variant="h6" mb={2}>Распределение задач</Typography>
            <ResponsiveContainer width="100%" height={350}>
              <PieChart>
                <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={130} label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}>
                  {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}
    </Box>
  )
}
