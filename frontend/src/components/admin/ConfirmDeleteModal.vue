<script setup>
import { uiState } from '../../composables/state.js'

const props = defineProps({
  title: { type: String, default: '删除确认' },
  message: { type: String, default: '确认执行此操作吗？' },
  detail: { type: String, default: '' },
  itemName: { type: String, default: '' },
  itemMeta: { type: String, default: '' },
})
const emit = defineEmits(['cancel', 'confirm'])
</script>

<template>
  <Teleport to="body">
    <transition name="mask-fade">
      <div class="modal-mask" @click="$emit('cancel')">
        <section class="modal-card confirm-modal" @click.stop>
          <div class="modal-head">
            <span class="section-label">{{ title }}</span>
            <button class="close-btn" @click="$emit('cancel')">&times;</button>
          </div>

          <h3>{{ message }}</h3>
          <p v-if="detail" class="detail-copy">{{ detail }}</p>

          <div v-if="itemName" class="confirm-card">
            <strong>{{ itemName }}</strong>
            <p v-if="itemMeta">{{ itemMeta }}</p>
          </div>

          <div class="modal-footer">
            <span></span>
            <div class="modal-actions">
              <button class="ghost-btn" @click="$emit('cancel')">取消</button>
              <button class="danger-btn" :disabled="uiState.actionLoading" @click="$emit('confirm')">
                {{ uiState.actionLoading ? '删除中...' : '确认删除' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </transition>
  </Teleport>
</template>
