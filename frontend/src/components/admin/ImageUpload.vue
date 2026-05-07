<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { uiState } from '../../composables/state.js'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue', 'upload'])

const localPreview = ref('')
const selectedFile = ref(null)

watch(() => props.modelValue, (val) => {
  if (!val) localPreview.value = ''
})

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  if (localPreview.value) {
    URL.revokeObjectURL(localPreview.value)
    localPreview.value = ''
  }

  selectedFile.value = file
  localPreview.value = URL.createObjectURL(file)
  emit('upload', file)
}

onUnmounted(() => {
  if (localPreview.value) URL.revokeObjectURL(localPreview.value)
})
</script>

<template>
  <div class="editor-image-frame">
    <img v-if="localPreview || modelValue" :src="localPreview || modelValue" alt="商品图片预览" />
    <div v-else>
      <strong>商品图片</strong>
      <span>上传后会显示在点餐台</span>
    </div>
  </div>
  <label class="upload-drop">
    <span>{{ uiState.imageUploading ? '上传中...' : '上传商品图片' }}</span>
    <input type="file" accept="image/png,image/jpeg,image/webp" :disabled="uiState.imageUploading" @change="onFileChange" />
  </label>
  <p>支持 PNG、JPG、WEBP。图片会保存到后台并用于商品卡片展示。</p>
</template>
