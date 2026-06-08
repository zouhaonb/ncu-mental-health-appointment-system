import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/view/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/view/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/student/home'
  },
  {
    path: '/student',
    component: () => import('@/view/student/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      { path: 'home', name: 'StudentHome', component: () => import('@/view/student/Home.vue') },
      { path: 'profile', name: 'StudentProfile', component: () => import('@/view/student/Profile.vue') },
      { path: 'assessment', name: 'Assessment', component: () => import('@/view/student/Assessment.vue') },
      { path: 'assessment-history', name: 'AssessmentHistory', component: () => import('@/view/student/AssessmentHistory.vue') },
      { path: 'counselors', name: 'CounselorList', component: () => import('@/view/student/CounselorList.vue') },
      { path: 'appointment/:counselorId', name: 'BookAppointment', component: () => import('@/view/student/BookAppointment.vue') },
      { path: 'my-appointments', name: 'MyAppointments', component: () => import('@/view/student/MyAppointments.vue') },
      { path: 'anonymous-message', name: 'AnonymousMessage', component: () => import('@/view/student/AnonymousMessage.vue') },
      { path: 'articles', name: 'StudentArticles', component: () => import('@/view/shared/Articles.vue') }
    ]
  },
  {
    path: '/counselor',
    component: () => import('@/view/counselor/CounselorLayout.vue'),
    meta: { role: 'COUNSELOR' },
    children: [
      { path: '', redirect: '/counselor/home' },
      { path: 'home', component: () => import('@/view/counselor/Home.vue') },
      { path: 'profile', component: () => import('@/view/counselor/Profile.vue') },
      { path: 'time-slots', component: () => import('@/view/counselor/TimeSlots.vue') },
      { path: 'appointments', component: () => import('@/view/counselor/Appointments.vue') },
      { path: 'consultation-record', component: () => import('@/view/counselor/ConsultationRecord.vue') },
      { path: 'anonymous-messages', component: () => import('@/view/counselor/AnonymousMessages.vue') },
      { path: 'referral-suggestions', component: () => import('@/view/counselor/ReferralSuggestions.vue') },
      { path: 'articles', component: () => import('@/view/shared/Articles.vue') }
    ]
  },
  {
    path: '/advisor',
    component: () => import('@/view/advisor/AdvisorLayout.vue'),
    meta: { requiresAuth: true, role: 'ADVISOR' },
    children: [
      { path: 'home', name: 'AdvisorHome', component: () => import('@/view/advisor/Home.vue') },
      { path: 'alerts', name: 'RiskAlerts', component: () => import('@/view/advisor/RiskAlerts.vue') },
      { path: 'concern-students', name: 'ConcernStudents', component: () => import('@/view/advisor/ConcernStudents.vue') },
      { path: 'referrals', name: 'Referrals', component: () => import('@/view/advisor/Referrals.vue') },
      { path: 'follow-ups', name: 'FollowUps', component: () => import('@/view/advisor/FollowUps.vue') },
      { path: 'statistics', name: 'AdvisorStatistics', component: () => import('@/view/advisor/Statistics.vue') },
      { path: 'articles', name: 'AdvisorArticles', component: () => import('@/view/shared/Articles.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/view/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/users' },
      { path: 'users', name: 'UserManagement', component: () => import('@/view/admin/UserManagement.vue') },
      { path: 'counselors', name: 'CounselorApproval', component: () => import('@/view/admin/CounselorApproval.vue') },
      { path: 'articles', name: 'ArticleManagement', component: () => import('@/view/admin/ArticleManagement.vue') },
      { path: 'configs', name: 'SystemConfig', component: () => import('@/view/admin/SystemConfig.vue') },
      { path: 'statistics', name: 'AdminStatistics', component: () => import('@/view/admin/Statistics.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.role && userStore.role !== to.meta.role) {
    next('/login')
  } else {
    next()
  }
})

export default router
