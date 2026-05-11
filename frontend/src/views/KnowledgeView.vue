<script setup>
import { computed } from 'vue'
import { useKnowledge, knowledgeState } from '../composables/useKnowledge.js'

const k = useKnowledge()

const dragOver = { value: false }

const ALLOWED_EXTENSIONS = '.pdf,.docx,.doc,.md,.txt,.markdown,.html,.htm,.csv'

const pendingCount = computed(() =>
  knowledgeState.files.filter((f) => f.status === 'pending').length,
)

const hasCompleted = computed(() =>
  knowledgeState.files.some((f) => f.status === 'success' || f.status === 'error'),
)

const FILE_TYPE_ICONS = {
  PDF: 'PDF', Word: 'DOC', Markdown: 'MD', '文本': 'TXT', HTML: 'H', CSV: 'CSV',
}

function onDragOver(e) {
  e.preventDefault()
  dragOver.value = true
}

function onDragLeave() {
  dragOver.value = false
}

function onDrop(e) {
  e.preventDefault()
  dragOver.value = false
  if (e.dataTransfer?.files?.length) {
    k.addFiles(e.dataTransfer.files)
  }
}

function onFilePick(e) {
  if (e.target.files?.length) {
    k.addFiles(e.target.files)
    e.target.value = ''
  }
}
</script>

<template>
  <section class="screen-page admin-page knowledge-page">
    <section class="page-body knowledge-body">
      <article class="panel knowledge-upload-panel">
        <div class="admin-action-row">
          <div>
            <span class="section-label">知识库管理</span>
            <h3>文档上传与索引</h3>
          </div>
          <button
            class="primary-btn"
            :disabled="knowledgeState.uploading || !pendingCount"
            @click="k.uploadAll"
          >
            {{ knowledgeState.uploading ? '上传中...' : `上传 (${pendingCount})` }}
          </button>
        </div>

        <div
          class="knowledge-drop-zone"
          :class="{ 'drag-over': dragOver.value }"
          @dragover="onDragOver"
          @dragleave="onDragLeave"
          @drop="onDrop"
        >
          <div class="knowledge-drop-content">
            <strong>{{ dragOver.value ? '释放以上传文件' : '拖拽文件到此处' }}</strong>
            <span>或点击下方按钮选择文件</span>
            <label class="primary-btn knowledge-file-btn">
              选择文件
              <input
                type="file"
                multiple
                :accept="ALLOWED_EXTENSIONS"
                class="knowledge-file-input"
                :disabled="knowledgeState.uploading"
                @change="onFilePick"
              />
            </label>
          </div>
          <p class="knowledge-drop-hint">
            支持 PDF、Word (DOCX/DOC)、Markdown (MD)、纯文本 (TXT)、HTML、CSV 格式。单文件不超过 10MB。
          </p>
        </div>

        <div v-if="knowledgeState.files.length" class="knowledge-file-list">
          <div class="knowledge-file-head">
            <span>文件列表（{{ knowledgeState.files.length }} 个）</span>
            <button
              v-if="hasCompleted"
              class="ghost-btn tiny"
              @click="k.clearCompleted"
            >清除已完成</button>
          </div>

          <div
            v-for="file in knowledgeState.files"
            :key="file.id"
            class="knowledge-file-row"
            :class="`status-${file.status}`"
          >
            <div class="knowledge-file-icon">
              {{ FILE_TYPE_ICONS[file.type] || '?' }}
            </div>
            <div class="knowledge-file-info">
              <strong>{{ file.name }}</strong>
              <div class="knowledge-file-meta">
                <span>{{ file.sizeLabel }}</span>
                <span class="knowledge-file-type-pill">{{ file.type }}</span>
                <span v-if="file.status === 'success'" class="knowledge-chunk-pill">
                  {{ file.chunkCount }} 个分块
                </span>
              </div>
              <p v-if="file.status === 'error'" class="knowledge-file-error">
                {{ file.errorMessage }}
              </p>
            </div>
            <div class="knowledge-file-status">
              <span v-if="file.status === 'pending'" class="knowledge-status-pill pending">待上传</span>
              <span v-else-if="file.status === 'uploading'" class="knowledge-status-pill uploading">
                <span class="knowledge-spinner"></span>上传中
              </span>
              <span v-else-if="file.status === 'success'" class="knowledge-status-pill success">已索引</span>
              <span v-else class="knowledge-status-pill error">失败</span>
            </div>
            <button
              v-if="file.status === 'pending'"
              class="knowledge-remove-btn"
              :disabled="knowledgeState.uploading"
              @click="k.removeFile(file.id)"
              title="移除"
            >&times;</button>
          </div>
        </div>

        <div v-else class="knowledge-empty">
          <strong>还没有添加文件</strong>
          <span>通过拖拽或点击按钮添加要索引的文档</span>
        </div>
      </article>
    </section>
  </section>
</template>
