import { useEffect, useState } from 'react'
import {
  Box, Button, Card, CardContent, Chip, CircularProgress, FormControl,
  Grid, InputLabel, LinearProgress, MenuItem, Paper, Select, Tab, Tabs,
  Typography, List, ListItem, ListItemText, Divider,
} from '@mui/material'
import PsychologyIcon from '@mui/icons-material/Psychology'
import {
  getProjects, getProjectRisk, runProjectRiskAssessment,
  getProjectRiskHistory, getProjectRecommendations,
} from '../api/services'
import { useAuthStore } from '../store/authStore'
import type { Project, ProjectRiskAssessment } from '../types'

const RISK_COLORS: Record<string, 'success' | 'warning' | 'error'> = {
  LOW: 'success', MEDIUM: 'warning', HIGH: 'error',
}
const RISK_LABELS: Record<string, string> = {
  LOW: 'Низкий', MEDIUM: 'Средний', HIGH: 'Высокий',
}

export default function IntelligencePage() {
  const { hasRole } = useAuthStore()
  const canRun = hasRole('ADMIN') || hasRole('PROJECT_MANAGER')

  const [projects, setProjects] = useState<Project[]>([])
  const [selectedProject, setSelectedProject] = useState('')
  const [tab, setTab] = useState(0)
  const [risk, setRisk] = useState<ProjectRiskAssessment | null>(null)
  const [history, setHistory] = useState<ProjectRiskAssessment[]>([])
  const [recommendations, setRecommendations] = useState<string[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    getProjects().then(p => {
      setProjects(p)
      if (p.length > 0) setSelectedProject(p[0].id)
    }).catch(() => {})
  }, [])

  useEffect(() => {
    if (!selectedProject) return
    setLoading(true)
    Promise.all([
      getProjectRisk(selectedProject).then(setRisk).catch(() => setRisk(null)),
      getProjectRiskHistory(selectedProject).then(setHistory).catch(() => setHistory([])),
      getProjectRecommendations(selectedProject).then(setRecommendations).catch(() => setRecommendations([])),
    ]).finally(() => setLoading(false))
  }, [selectedProject])

  const handleRunAssessment = async () => {
    if (!selectedProject) return
    setLoading(true)
    try {
      const r = await runProjectRiskAssessment(selectedProject)
      setRisk(r)
      const h = await getProjectRiskHistory(selectedProject)
      setHistory(h)
      const rec = await getProjectRecommendations(selectedProject)
      setRecommendations(rec)
    } finally { setLoading(false) }
  }

  return (
    <Box>
      <Typography variant="h5" mb={3}>Интеллектуальный анализ</Typography>

      <Box display="flex" gap={2} alignItems="center" mb={3}>
        <FormControl size="small" sx={{ minWidth: 280 }}>
          <InputLabel>Проект</InputLabel>
          <Select value={selectedProject} label="Проект" onChange={e => setSelectedProject(e.target.value)}>
            {projects.map(p => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
          </Select>
        </FormControl>
        {canRun && (
          <Button variant="contained" startIcon={<PsychologyIcon />} onClick={handleRunAssessment} disabled={!selectedProject || loading}>
            Запустить оценку риска
          </Button>
        )}
      </Box>

      {loading && <LinearProgress sx={{ mb: 2 }} />}

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Текущая оценка" />
        <Tab label="История оценок" />
        <Tab label="Рекомендации" />
      </Tabs>

      {tab === 0 && (
        risk ? (
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" color="text.secondary">Уровень риска</Typography>
                  <Box display="flex" alignItems="center" gap={2} mt={1}>
                    <Typography variant="h2" color={`${RISK_COLORS[risk.riskLevel]}.main`}>
                      {risk.riskScore.toFixed(1)}
                    </Typography>
                    <Chip label={RISK_LABELS[risk.riskLevel]} color={RISK_COLORS[risk.riskLevel]} />
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" color="text.secondary">Вероятность нарушения сроков</Typography>
                  <Typography variant="h3" mt={1}>{(risk.delayProbability * 100).toFixed(1)}%</Typography>
                  <LinearProgress
                    variant="determinate" value={risk.delayProbability * 100}
                    color={risk.delayProbability > 0.7 ? 'error' : risk.delayProbability > 0.4 ? 'warning' : 'success'}
                    sx={{ mt: 1 }}
                  />
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="subtitle2" color="text.secondary">Прогнозируемая дата завершения</Typography>
                  <Typography variant="h5" mt={1}>
                    {new Date(risk.predictedCompletionDate).toLocaleDateString('ru-RU')}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Оценка: {new Date(risk.assessedAt).toLocaleString('ru-RU')}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            {risk.riskFactors && risk.riskFactors.length > 0 && (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" mb={2}>Факторы риска</Typography>
                    {risk.riskFactors.map(f => (
                      <Box key={f.id} mb={1.5}>
                        <Box display="flex" justifyContent="space-between" alignItems="center">
                          <Typography variant="body2">{f.description || f.factorType}</Typography>
                          <Chip label={`Влияние: ${f.impactScore.toFixed(2)}`} size="small" />
                        </Box>
                        <LinearProgress variant="determinate" value={Math.min(f.factorValue * 100, 100)} sx={{ mt: 0.5 }} />
                      </Box>
                    ))}
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        ) : (
          <Typography color="text.secondary">Оценка риска ещё не выполнялась для данного проекта</Typography>
        )
      )}

      {tab === 1 && (
        <Box>
          {history.length === 0 ? (
            <Typography color="text.secondary">История оценок отсутствует</Typography>
          ) : history.map(h => (
            <Paper key={h.id} sx={{ p: 2, mb: 1 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Box>
                  <Typography variant="body2">
                    Балл риска: <strong>{h.riskScore.toFixed(2)}</strong> · Вероятность задержки: <strong>{(h.delayProbability * 100).toFixed(1)}%</strong>
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(h.assessedAt).toLocaleString('ru-RU')} · Модель: {h.modelVersion}
                  </Typography>
                </Box>
                <Chip label={RISK_LABELS[h.riskLevel]} color={RISK_COLORS[h.riskLevel]} size="small" />
              </Box>
            </Paper>
          ))}
        </Box>
      )}

      {tab === 2 && (
        <Box>
          {recommendations.length === 0 ? (
            <Typography color="text.secondary">Рекомендации отсутствуют</Typography>
          ) : (
            <Paper>
              <List>
                {recommendations.map((r, i) => (
                  <Box key={i}>
                    {i > 0 && <Divider />}
                    <ListItem>
                      <ListItemText primary={r} />
                    </ListItem>
                  </Box>
                ))}
              </List>
            </Paper>
          )}
        </Box>
      )}
    </Box>
  )
}
