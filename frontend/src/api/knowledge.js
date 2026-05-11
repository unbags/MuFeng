import { request } from './index.js'

export function uploadKnowledgeFiles(files) {
  const formData = new FormData()
  for (const file of files) {
    formData.append('files', file)
  }
  return request('/admin/knowledge/upload', {
    method: 'POST',
    body: formData,
  })
}
