export interface ApiResponse<T = unknown> {
  code: number
  data: T
  msg: string
}

export interface PageData<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

export interface PageParams {
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

export type PageResult<T> = ApiResponse<PageData<T>>