import { ref } from 'vue'
import { queryChat, streamChat } from '../api/chat.js'

const isSupportOpen = ref(false)
const supportInput = ref('')
const isSending = ref(false)
const isStreaming = ref(false)
const streamAbort = ref(null)
const supportMessages = ref([
  {
    role: 'assistant',
    text: '您好，我是沐枫餐饮智能助手。可以为您推荐菜品、查询订单状态、了解配送信息。',
    source: null,
  },
])

const quickQuestions = ['今日招牌菜推荐', '查询我的订单', '了解配送范围', '预约包厢']

export function useChat() {
  async function askSupport(question) {
    const text = question || supportInput.value.trim()
    if (!text || isSending.value) return

    if (streamAbort.value) {
      streamAbort.value.abort()
      streamAbort.value = null
    }

    supportMessages.value.push({ role: 'user', text })
    supportInput.value = ''
    isSupportOpen.value = true
    isSending.value = true
    isStreaming.value = true

    const orderMatch = text.match(/ORD\d+/i)
    const payload = {
      message: text,
      orderNo: orderMatch ? orderMatch[0].toUpperCase() : undefined,
    }

    const assistantIndex = supportMessages.value.push({
      role: 'assistant',
      text: '',
      source: null,
      isStreaming: true,
    }) - 1

    const onChunk = (chunk) => {
      supportMessages.value[assistantIndex].text += chunk
    }

    const onDone = () => {
      supportMessages.value[assistantIndex].isStreaming = false
      isSending.value = false
      isStreaming.value = false
      streamAbort.value = null
    }

    const onError = async (_error) => {
      try {
        const response = await queryChat(payload)
        supportMessages.value[assistantIndex] = {
          role: 'assistant',
          text: response.message || '我已经收到您的问题。',
          source: response.source || null,
          isStreaming: false,
        }
      } catch {
        supportMessages.value[assistantIndex] = {
          role: 'assistant',
          text: '智能客服暂时不可用，请稍后再试或联系店员。',
          source: null,
          isStreaming: false,
        }
      } finally {
        isSending.value = false
        isStreaming.value = false
        streamAbort.value = null
      }
    }

    streamAbort.value = streamChat(payload, onChunk, onDone, onError)
  }

  function toggleSupport() {
    isSupportOpen.value = !isSupportOpen.value
  }

  function closeSupport() {
    isSupportOpen.value = false
  }

  return {
    isSupportOpen,
    supportInput,
    supportMessages,
    quickQuestions,
    isSending,
    isStreaming,
    askSupport,
    toggleSupport,
    closeSupport,
  }
}
