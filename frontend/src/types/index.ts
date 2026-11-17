export type Role = 'ADMIN' | 'PROJECT_MANAGER' | 'EMPLOYEE'
export type ProjectStatus = 'ACTIVE' | 'ARCHIVED' | 'COMPLETED'
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type ComplexityLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type ProjectRole = 'MANAGER' | 'MEMBER'

export interface User {
  id: string
  email: string
  isActive: boolean
  roles: Role[]
  createdAt: string
}

export interface Project {
  id: string
  name: string
  description: string
  status: ProjectStatus
  createdBy: string
  createdAt: string
  updatedAt: string
  archivedAt?: string
  completedAt?: string
}

export interface ProjectMember {
  projectId: string
  userId: string
  projectRole: ProjectRole
  joinedAt: string
  email?: string
}

export interface Label {
  id: string
  name: string
  color?: string
}

export interface Task {
  id: string
  projectId: string
  title: string
  description: string
  assigneeId?: string
  createdBy: string
  status: TaskStatus
  priority: TaskPriority
  dueDate?: string
  estimatedEffort?: number
  actualEffort?: number
  labels?: Label[]
  createdAt: string
  updatedAt: string
}

export interface TaskHistory {
  id: string
  taskId: string
  changedBy: string
  changeType: 'STATUS' | 'ASSIGNEE' | 'DEADLINE'
  oldValue: string
  newValue: string
  changedAt: string
}

export interface Comment {
  id: string
  taskId: string
  projectId: string
  authorId: string
  body: string
  createdAt: string
  updatedAt: string
}

export interface Notification {
  id: string
  recipientId: string
  type: string
  title: string
  message: string
  taskId?: string
  projectId?: string
  createdAt: string
  readAt?: string
}

export interface TaskAnalysis {
  id: string
  taskId: string
  complexityScore: number
  complexityLevel: ComplexityLevel
  predictedEffort: number
  analysisDetails?: Record<string, unknown>
  analyzedAt: string
  modelVersion: string
}

export interface RiskFactor {
  id: string
  assessmentId: string
  factorType: string
  factorValue: number
  impactScore: number
  description: string
}

export interface ProjectRiskAssessment {
  id: string
  projectId: string
  riskScore: number
  riskLevel: RiskLevel
  delayProbability: number
  predictedCompletionDate: string
  assessmentDetails?: Record<string, unknown>
  assessedAt: string
  modelVersion: string
  riskFactors?: RiskFactor[]
}

export interface ReportOverview {
  totalProjects: number
  activeProjects: number
  totalTasks: number
  completedTasks: number
  overdueTasks: number
}

export interface ProjectReport {
  projectId: string
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  todoTasks: number
  overdueTasks: number
  completionRate: number
}

export interface CompletionDynamics {
  date: string
  created: number
  completed: number
}

export interface EmployeeWorkload {
  userId: string
  email: string
  assignedTasks: number
  completedTasks: number
  overdueTasks: number
}

export interface ProjectComparison {
  projectId: string
  projectName: string
  totalTasks: number
  completedTasks: number
  completionRate: number
  overdueTasks: number
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
