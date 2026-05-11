import { reactive } from 'vue'
import { uploadKnowledgeFiles } from '../api/knowledge.js'

export const knowledgeState = reactive({
  files: [],
  uploading: false,
})

const _fileObjects = new Map()

let _fileIdCounter = 0

const MAX_FILE_SIZE = 10 * 1024 * 1024
const ALLOWED_EXTENSIONS = new Set(['pdf', 'docx', 'doc', 'md', 'txt', 'markdown', 'html', 'htm', 'csv'])

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function getFileTypeLabel(name) {
  const ext = (name || '').split('.').pop()?.toLowerCase()
  const map = { pdf: 'PDF', docx: 'Word', doc: 'Word', md: 'Markdown', txt: '文本', markdown: 'Markdown', html: 'HTML', htm: 'HTML', csv: 'CSV' }
  return map[ext] || ext?.toUpperCase() || '未知'
}

function addFiles(fileList) {
  for (const file of fileList) {
    const id = `f_${Date.now()}_${++_fileIdCounter}`
    const ext = (file.name || '').split('.').pop()?.toLowerCase()
    const errorMessage = validateFile(file, ext)
    if (!errorMessage) {
      _fileObjects.set(id, file)
    }
    knowledgeState.files.push({
      id,
      name: file.name,
      size: file.size,
      sizeLabel: formatFileSize(file.size),
      type: getFileTypeLabel(file.name),
      status: errorMessage ? 'error' : 'pending',
      chunkCount: 0,
      errorMessage,
    })
  }
}

function validateFile(file, ext) {
  if (!ext || !ALLOWED_EXTENSIONS.has(ext)) {
    return '不支持的文件格式'
  }
  if (file.size > MAX_FILE_SIZE) {
    return '文件大小超过限制，单文件最大支持 10MB'
  }
  return ''
}

function removeFile(id) {
  _fileObjects.delete(id)
  const idx = knowledgeState.files.findIndex((f) => f.id === id)
  if (idx !== -1) knowledgeState.files.splice(idx, 1)
}

async function uploadAll() {
  const pending = knowledgeState.files.filter((f) => f.status === 'pending')
  if (!pending.length) return

  knowledgeState.uploading = true
  pending.forEach((f) => { f.status = 'uploading' })

  const filesToUpload = pending.map((f) => _fileObjects.get(f.id)).filter(Boolean)

  try {
    const results = await uploadKnowledgeFiles(filesToUpload)
    for (let i = 0; i < results.length; i++) {
      const result = results[i]
      const fileState = pending[i]
      if (!fileState) continue
      fileState.status = result.success ? 'success' : 'error'
      fileState.chunkCount = result.chunkCount || 0
      fileState.errorMessage = result.errorMessage || ''
    }
  } catch (error) {
    pending.forEach((f) => {
      f.status = 'error'
      f.errorMessage = error.message || '上传失败'
    })
  } finally {
    knowledgeState.uploading = false
  }
}

function clearCompleted() {
  const idsToRemove = knowledgeState.files
    .filter((f) => f.status === 'success' || f.status === 'error')
    .map((f) => f.id)
  idsToRemove.forEach((id) => _fileObjects.delete(id))
  knowledgeState.files = knowledgeState.files.filter(
    (f) => f.status === 'pending' || f.status === 'uploading',
  )
}

export function useKnowledge() {
  return {
    knowledgeState,
    addFiles,
    removeFile,
    uploadAll,
    clearCompleted,
    formatFileSize,
    getFileTypeLabel,
  }
}
