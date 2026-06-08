import request from '@/utils/request'

export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  logout: () => request.post('/auth/logout')
}

export const studentAPI = {
  getInfo: () => request.get('/student/info'),
  updateInfo: (data) => request.put('/student/info', data),
  changePassword: (data) => request.put('/student/password', data),
  submitAssessment: (data) => request.post('/student/assessment', data),
  getAssessmentHistory: (studentId) => request.get(`/student/assessment/history/${studentId}`),
  getCounselors: () => request.get('/student/counselors'),
  getAvailableSlots: (counselorId) => request.get(`/student/slots/${counselorId}`),
  createAppointment: (data) => request.post('/student/appointment', data),
  getMyAppointments: (studentId) => request.get(`/student/appointments/${studentId}`),
  cancelAppointment: (id, data) => request.put(`/student/appointment/${id}/cancel`, data),
  sendAnonymousMessage: (data) => request.post('/student/anonymous-message', data),
  getMyAnonymousMessages: (studentId) => request.get(`/student/anonymous-messages/${studentId}`)
}

export const counselorAPI = {
  getInfo: () => request.get('/counselor/info'),
  updateInfo: (data) => request.put('/counselor/info', data),
  uploadAvatar: (data) => request.post('/counselor/avatar/upload', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  addTimeSlot(data) {
    return request.post('/counselor/slot', data)
  },
  
  batchAddTimeSlots(data) {
    return request.post('/counselor/slots/batch', data)
  },
  
  getMyTimeSlots(counselorId) {
    return request.get(`/counselor/slots/${counselorId}`)
  },
  deleteTimeSlot: (id) => request.delete(`/counselor/slot/${id}`),
  updateTimeSlot: (id, data) => request.put(`/counselor/slot/${id}`, data),
  toggleTimeSlotStatus: (id) => request.put(`/counselor/slot/${id}/toggle-status`),
  getAppointments: (counselorId, status) => request.get(`/counselor/appointments/${counselorId}?status=${status || ''}`),
  confirmAppointment: (id) => request.put(`/counselor/appointment/${id}/confirm`),
  rejectAppointment: (id, data) => request.put(`/counselor/appointment/${id}/reject`, data),
  completeAppointment: (id) => request.put(`/counselor/appointment/${id}/complete`),
  saveConsultationRecord: (data) => request.post('/counselor/record', data),
  getConsultationRecords: (counselorId) => request.get(`/counselor/records/${counselorId}`),
  getAnonymousMessages: () => request.get('/counselor/anonymous-messages'),
  replyAnonymousMessage: (id, data) => request.put(`/counselor/anonymous-message/${id}/reply`, data),
  getList: () => request.get('/student/counselors'),
  getReferralSuggestions: () => request.get('/counselor/referral-suggestions'),
  handleReferralSuggestion: (id, action) => request.put(`/counselor/referral-suggestion/${id}/handle?action=${action}`),
  acceptReferralWithTimeSlot: (id, timeSlotId) => request.put(`/counselor/referral-suggestion/${id}/accept?timeSlotId=${timeSlotId}`)
}

export const advisorAPI = {
  getInfo: () => request.get('/advisor/info'),
  getRiskAlerts: (advisorId) => request.get(`/advisor/alerts/${advisorId}`),
  getUnreadAlertCount: (advisorId) => request.get(`/advisor/alerts/unread/${advisorId}`),
  markAlertAsRead: (id) => request.put(`/advisor/alert/${id}/read`),
  addKeyConcernStudent: (data) => request.post('/advisor/concern', data),
  getKeyConcernStudents: (advisorId) => request.get(`/advisor/concerns/${advisorId}`),
  removeKeyConcernStudent: (id) => request.delete(`/advisor/concern/${id}`),
  createReferralSuggestion: (data) => request.post('/advisor/referral', data),
  cancelReferralSuggestion: (id) => request.put(`/advisor/referral/${id}/cancel`),
  getReferralSuggestions: (advisorId) => request.get(`/advisor/referrals/${advisorId}`),
  createFollowUpTask: (data) => request.post('/advisor/follow-up', data),
  getFollowUpTasks: (advisorId) => request.get(`/advisor/follow-ups/${advisorId}`),
  completeFollowUpTask: (id, feedback) => request.put(`/advisor/follow-up/${id}/complete?feedback=${feedback}`),
  getStudentConsultations: (studentId) => request.get(`/advisor/student/consultations/${studentId}`),
  getStudentAppointments: (studentId) => request.get(`/advisor/student/appointments/${studentId}`),
  getCollegeStatistics: (advisorId) => request.get(`/advisor/statistics/${advisorId}`)
}

export const adminAPI = {
  getUserList: (role) => request.get(`/admin/users?role=${role || ''}`),
  getCollegeList: () => request.get('/admin/colleges'),
  createUser: (data) => request.post('/admin/user', data),
  updateUser: (data) => request.put('/admin/user', data),
  deleteUser: (id) => request.delete(`/admin/user/${id}`),
  toggleUserStatus: (id) => request.put(`/admin/user/${id}/toggle-status`),
  getCounselorList: (params) => request.get('/admin/counselors', { params }),
  getCounselorStatistics: () => request.get('/admin/counselors/statistics'),
  approveCounselor: (id) => request.put(`/admin/counselor/${id}/approve`),
  rejectCounselor: (id, data) => request.put(`/admin/counselor/${id}/reject`, data),
  updateCounselor: (data) => request.put('/admin/counselor', data),
  toggleCounselorStatus: (id) => request.put(`/admin/counselor/${id}/toggle-status`),
  createCounselor: (data) => request.post('/admin/counselor', data),
  getArticleList: () => request.get('/admin/articles'),
  createArticle: (data) => request.post('/admin/article', data),
  updateArticle: (data) => request.put('/admin/article', data),
  deleteArticle: (id) => request.delete(`/admin/article/${id}`),
  getConfigList: () => request.get('/admin/configs'),
  updateConfig: (data) => request.put('/admin/config', data),
  getSystemStatistics: () => request.get('/admin/statistics'),
  getHistoryStatistics: (params) => request.get('/admin/statistics/history', { params })
}

export const articleAPI = {
  getArticles: (type) => request.get(`/articles?type=${type || ''}`),
  getArticleById: (id) => request.get(`/articles/${id}`)
}
