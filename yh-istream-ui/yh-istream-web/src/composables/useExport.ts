/**
 * 文件导出组合式函数
 * 统一处理 Blob 响应的下载逻辑，消除各页面重复的导出代码
 * 正确处理 axios 拦截器对 blob 响应的包装（返回 AxiosResponse 而非 Blob）
 */
import { useMessage } from 'naive-ui'

export function useExport() {
  const message = useMessage()

  async function downloadExcel(
    apiCall: () => Promise<unknown>,
    filename: string,
  ) {
    try {
      const res = await apiCall()
      const blob = res instanceof Blob
        ? res
        : new Blob([(res as { data: BlobPart }).data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      triggerDownload(blob, filename)
      message.success('导出成功')
    } catch (e: unknown) {
      message.error((e as Error).message || '导出失败')
    }
  }

  async function downloadFile(
    apiCall: () => Promise<unknown>,
    filename: string,
    contentType?: string,
  ) {
    try {
      const res = await apiCall()
      const blob = res instanceof Blob
        ? res
        : new Blob([(res as { data: BlobPart }).data], { type: contentType ?? 'application/octet-stream' })
      triggerDownload(blob, filename)
    } catch (e: unknown) {
      message.error((e as Error).message || '下载失败')
    }
  }

  function triggerDownload(blob: Blob, filename: string) {
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
  }

  return {
    downloadExcel,
    downloadFile,
  }
}