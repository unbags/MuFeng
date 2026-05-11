<script setup>
import { computed, watch, nextTick, ref } from 'vue'
import { useChat } from '../../composables/useChat.js'
import { useCart } from '../../composables/useCart.js'

const {
  isSupportOpen,
  supportInput,
  supportMessages,
  isSending,
  isStreaming,
  askSupport,
  closeSupport,
} = useChat()
const { isCartOpen } = useCart()

const panelRight = computed(() => (isCartOpen.value ? '484px' : '28px'))

const messagesEl = ref(null)

function scrollToBottom() {
  nextTick(() => {
    const el = messagesEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const scrollTrigger = computed(() => {
  const msgs = supportMessages.value
  const last = msgs[msgs.length - 1]
  return msgs.length + (last?.text?.length ?? 0)
})

watch(scrollTrigger, () => scrollToBottom())
</script>

<template>
  <section class="support-widget" :style="{ right: panelRight }" aria-label="智能客服">
    <div v-if="isSupportOpen" class="support-panel">
      <div class="support-head">
        <div>
          <span>{{ isStreaming ? '正在输入' : '在线' }}</span>
          <h2>沐枫点餐助手</h2>
        </div>
        <button type="button" @click="closeSupport">关闭</button>
      </div>

      <div ref="messagesEl" class="support-messages">
        <article
          v-for="(message, index) in supportMessages"
          :key="`${message.role}-${index}`"
          :class="['support-message', message.role]"
        >
          {{ message.text }}
          <span v-if="message.isStreaming" class="stream-cursor" aria-hidden="true">|</span>
          <span
            v-if="message.role === 'assistant' && !message.isStreaming && (message.source === 'AI' || message.source === 'AI-RAG')"
            class="source-badge"
          >
            由 AI 生成
          </span>
        </article>
      </div>

      <form class="support-input" @submit.prevent="askSupport()">
        <input
          v-model="supportInput"
          type="text"
          placeholder="输入关于菜品、口味或配送的问题"
          :disabled="isSending"
        />
        <button type="submit" :disabled="isSending">
          {{ isSending ? '发送中' : '发送' }}
        </button>
      </form>
    </div>
  </section>
</template>

<style scoped>
.support-widget {
  position: fixed;
  bottom: 28px;
  z-index: 50;
  pointer-events: none;
  transition: right 560ms var(--spring);
}

.support-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  width: min(390px, calc(100vw - 32px));
  max-height: min(620px, calc(100vh - 112px));
  padding: 18px;
  pointer-events: auto;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16);
}

.support-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.support-head span {
  color: var(--accent);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.14em;
}

.support-head h2 {
  margin: 4px 0 0;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 28px;
  font-weight: 400;
}

.support-head button {
  border: 0;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  font-family: inherit;
}

.support-messages {
  display: grid;
  min-height: 0;
  gap: 10px;
  overflow-y: auto;
  padding: 4px 2px 12px;
  scroll-behavior: smooth;
}

.support-message {
  width: fit-content;
  max-width: 88%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  line-height: 1.65;
  white-space: pre-wrap;
}

.support-message.assistant {
  background: rgba(255, 255, 255, 0.68);
  color: var(--muted);
}

.support-message.user {
  justify-self: end;
  background: var(--ink);
  color: #fff;
}

.stream-cursor {
  animation: blink 0.8s step-end infinite;
  color: var(--accent);
  font-weight: 200;
}

.source-badge {
  display: block;
  margin-top: 6px;
  width: fit-content;
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: var(--accent);
  border: 1px solid var(--accent);
  opacity: 0.7;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.support-input {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.support-input input {
  min-width: 0;
  min-height: 44px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.58);
  color: var(--ink);
  outline: 0;
  padding: 0 12px;
  font-family: inherit;
}

.support-input input:disabled {
  opacity: 0.5;
  cursor: default;
}

.support-input button {
  min-height: 44px;
  border: 1px solid var(--ink);
  background: var(--ink);
  color: #fff;
  padding: 0 16px;
  font-weight: 800;
  cursor: pointer;
  font-family: inherit;
}

.support-input button:disabled {
  opacity: 0.6;
  cursor: default;
}

@media (max-width: 560px) {
  .support-panel {
    width: calc(100vw - 20px);
    max-height: calc(100vh - 92px);
  }
  .support-input {
    grid-template-columns: 1fr;
  }
}
</style>
