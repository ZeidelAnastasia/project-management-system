import api from './client'
import type {
  AuthTokens, LoginRequest, RegisterRequest, User,
  Project, ProjectMember,
  Task, TaskHistory, Label,
  Comment, Notification,
  TaskAnalysis, ProjectRiskAssessment,
  ReportOverview, ProjectReport, CompletionDynamics, EmployeeWorkload, ProjectComparison,
} from '../types'

// Auth
export const login = (data: LoginRequest) =>
  api.post<AuthTokens>('/auth/login', data).then(r => r.data)

export const register = (data: RegisterRequest) =>
  api.post<AuthTokens>('/auth/register', data).then(r => r.data)

export const logout = (refreshToken: string) =>
  api.post('/auth/logout', { refreshToken })

export const getMe = () =>
  api.get<User>('/auth/me').then(r => r.data)

// Users
export const getUsers = () =>
  api.get<User[]>('/users').then(r => r.data)

export const getUser = (id: string) =>
  api.get<User>(`/users/${id}`).then(r => r.data)

export const updateUserRole = (id: string, role: string) =>
  api.patch(`/users/${id}/role`, { role }).then(r => r.data)

export const updateUserStatus = (id: string, isActive: boolean) =>
  api.patch(`/users/${id}/status`, { isActive }).then(r => r.data)

// Projects
export const getProjects = (params?: Record<string, string>) =>
  api.get<Project[]>('/projects', { params }).then(r => r.data)

export const getProject = (id: string) =>
  api.get<Project>(`/projects/${id}`).then(r => r.data)

export const createProject = (data: Partial<Project>) =>
  api.post<Project>('/projects', data).then(r => r.data)

export const updateProject = (id: string, data: Partial<Project>) =>
  api.put<Project>(`/projects/${id}`, data).then(r => r.data)

export const archiveProject = (id: string) =>
  api.patch(`/projects/${id}/archive`).then(r => r.data)

export const restoreProject = (id: string) =>
  api.patch(`/projects/${id}/restore`).then(r => r.data)

export const completeProject = (id: string, force?: boolean) =>
  api.patch(`/projects/${id}/complete`, { force }).then(r => r.data)

export const getProjectMembers = (id: string) =>
  api.get<ProjectMember[]>(`/projects/${id}/members`).then(r => r.data)

export const addProjectMember = (id: string, userId: string, projectRole: string) =>
  api.post(`/projects/${id}/members`, { userId, projectRole }).then(r => r.data)

export const removeProjectMember = (id: string, userId: string) =>
  api.delete(`/projects/${id}/members/${userId}`).then(r => r.data)

// Tasks
export const getProjectTasks = (projectId: string, params?: Record<string, string>) =>
  api.get<Task[]>(`/projects/${projectId}/tasks`, { params }).then(r => r.data)

export const getTask = (id: string) =>
  api.get<Task>(`/tasks/${id}`).then(r => r.data)

export const createTask = (projectId: string, data: Partial<Task>) =>
  api.post<Task>(`/projects/${projectId}/tasks`, data).then(r => r.data)

export const updateTask = (id: string, data: Partial<Task>) =>
  api.put<Task>(`/tasks/${id}`, data).then(r => r.data)

export const deleteTask = (id: string) =>
  api.delete(`/tasks/${id}`)

export const updateTaskStatus = (id: string, status: string) =>
  api.patch(`/tasks/${id}/status`, { status }).then(r => r.data)

export const updateTaskAssignee = (id: string, assigneeId: string | null) =>
  api.patch(`/tasks/${id}/assignee`, { assigneeId }).then(r => r.data)

export const updateTaskDeadline = (id: string, dueDate: string | null) =>
  api.patch(`/tasks/${id}/deadline`, { dueDate }).then(r => r.data)

export const updateTaskEffort = (id: string, estimatedEffort?: number, actualEffort?: number) =>
  api.patch(`/tasks/${id}/effort`, { estimatedEffort, actualEffort }).then(r => r.data)

export const getTaskHistory = (id: string) =>
  api.get<TaskHistory[]>(`/tasks/${id}/history`).then(r => r.data)

export const searchTasks = (params: Record<string, string>) =>
  api.get<Task[]>('/tasks/search', { params }).then(r => r.data)

export const getLabels = () =>
  api.get<Label[]>('/labels').then(r => r.data)

// Comments
export const getComments = (taskId: string) =>
  api.get<Comment[]>(`/tasks/${taskId}/comments`).then(r => r.data)

export const createComment = (taskId: string, body: string) =>
  api.post<Comment>(`/tasks/${taskId}/comments`, { body }).then(r => r.data)

export const updateComment = (id: string, body: string) =>
  api.put<Comment>(`/comments/${id}`, { body }).then(r => r.data)

export const deleteComment = (id: string) =>
  api.delete(`/comments/${id}`)

// Notifications
export const getNotifications = () =>
  api.get<Notification[]>('/notifications').then(r => r.data)

export const getUnreadNotifications = () =>
  api.get<Notification[]>('/notifications/unread').then(r => r.data)

export const markNotificationRead = (id: string) =>
  api.patch(`/notifications/${id}/read`).then(r => r.data)

export const markAllNotificationsRead = () =>
  api.patch('/notifications/read-all').then(r => r.data)

// Reports
export const getReportOverview = () =>
  api.get<ReportOverview>('/reports/overview').then(r => r.data)

export const getProjectReport = (projectId: string) =>
  api.get<ProjectReport>(`/reports/projects/${projectId}`).then(r => r.data)

export const getOverdueTasks = () =>
  api.get<Task[]>('/reports/overdue-tasks').then(r => r.data)

export const getCompletionDynamics = (params?: Record<string, string>) =>
  api.get<CompletionDynamics[]>('/reports/completion-dynamics', { params }).then(r => r.data)

export const getEmployeeWorkload = () =>
  api.get<EmployeeWorkload[]>('/reports/employee-workload').then(r => r.data)

export const getProjectComparison = () =>
  api.get<ProjectComparison[]>('/reports/project-comparison').then(r => r.data)

// Intelligence
export const getTaskAnalysis = (taskId: string) =>
  api.get<TaskAnalysis>(`/intelligence/tasks/${taskId}/analysis`).then(r => r.data)

export const runTaskAnalysis = (taskId: string) =>
  api.post<TaskAnalysis>(`/intelligence/tasks/${taskId}/analysis`).then(r => r.data)

export const getProjectRisk = (projectId: string) =>
  api.get<ProjectRiskAssessment>(`/intelligence/projects/${projectId}/risk`).then(r => r.data)

export const runProjectRiskAssessment = (projectId: string) =>
  api.post<ProjectRiskAssessment>(`/intelligence/projects/${projectId}/risk-assessment`).then(r => r.data)

export const getProjectRiskHistory = (projectId: string) =>
  api.get<ProjectRiskAssessment[]>(`/intelligence/projects/${projectId}/risk-history`).then(r => r.data)

export const getProjectRecommendations = (projectId: string) =>
  api.get<string[]>(`/intelligence/projects/${projectId}/recommendations`).then(r => r.data)
