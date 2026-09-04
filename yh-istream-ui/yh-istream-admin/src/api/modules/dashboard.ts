import {get} from '@/api/request'

export function getDashboardStats() {
  return get('/dashboard/stats')
}