import { ref } from 'vue'
import { queryChat, streamChat } from '../api/chat.js'
import { fetchCart } from '../api/cart.js'
import { useCart } from './useCart.js'
import { getCartId } from '../utils/cartSession.js'

let sessionId = null
function getSessionId() {
  if (!sessionId) {
    sessionId = sessionStorage.getItem('mufeng_chat_session')
    if (!sessionId) {
      sessionId = 'sess_' + Date.now().toString(36) + '_' + Math.random().toString(36).slice(2, 8)
      sessionStorage.setItem('mufeng_chat_session', sessionId)
    }
  }
  return sessionId
}

const isSupportOpen = ref(false)
const supportInput = ref('')
const isSending = ref(false)
const isStreaming = ref(false)
const streamAbort = ref(null)
const supportMessages = ref([
  {
    role: 'assistant',
    text: '您好，我是沐枫餐饮智能助手。可以为您推荐菜品、查询订单状态、了解外带取餐。',
    source: null,
  },
])

const quickQuestions = ['今日招牌菜推荐', '查询我的订单']

let requestId = 0

export function useChat() {
  const { applyCartSnapshot, syncCartToBackend } = useCart()

  async function askSupport(question) {
    const text = question || supportInput.value.trim()
    if (!text || isSending.value) return

    if (streamAbort.value) {
      streamAbort.value.abort()
      streamAbort.value = null
    }

    const currentId = ++requestId

    supportMessages.value.push({ role: 'user', text })
    supportInput.value = ''
    isSupportOpen.value = true
    isSending.value = true
    isStreaming.value = true

    await syncCartToBackend().catch((error) => {
      console.warn('[metaFront] AI 发送前同步购物车失败:', error.message)
    })

    const orderMatch = text.match(/\bORD\d{10,}\b/i)
    const payload = {
      message: text,
      orderNo: orderMatch ? orderMatch[0].toUpperCase() : undefined,
      conversationId: getSessionId(),
      cartId: getCartId(),
    }

    const assistantIndex = supportMessages.value.push({
      role: 'assistant',
      text: '',
      source: null,
      isStreaming: true,
    }) - 1

    const onChunk = (chunk) => {
      if (currentId !== requestId) return
      supportMessages.value[assistantIndex].text += chunk
    }

    const onDone = () => {
      if (currentId !== requestId) return
      supportMessages.value[assistantIndex].isStreaming = false
      isSending.value = false
      isStreaming.value = false
      streamAbort.value = null
      refreshCartSnapshot()
    }

    const onError = async (_error) => {
      if (currentId !== requestId) return
      supportMessages.value[assistantIndex] = {
        role: 'assistant',
        text: '正在为您查询...',
        source: null,
        isStreaming: true,
      }
      try {
        const response = await queryChat(payload)
        if (currentId !== requestId) return
        supportMessages.value[assistantIndex] = {
          role: 'assistant',
          text: response.message || '我已经收到您的问题。',
          source: response.source || null,
          isStreaming: false,
        }
        if (response.cartSnapshot) {
          applyCartSnapshot(response.cartSnapshot)
        }
      } catch {
        if (currentId !== requestId) return
        supportMessages.value[assistantIndex] = {
          role: 'assistant',
          text: '智能客服暂时不可用，请稍后再试或联系店员。',
          source: null,
          isStreaming: false,
        }
      } finally {
        if (currentId === requestId) {
          isSending.value = false
          isStreaming.value = false
          streamAbort.value = null
        }
      }
    }

    const onAction = (event) => {
      if (event?.cartSnapshot) {
        applyCartSnapshot(event.cartSnapshot)
      }
      if (event?.source && supportMessages.value[assistantIndex]) {
        supportMessages.value[assistantIndex].source = event.source
      }
    }

    streamAbort.value = streamChat(payload, onChunk, onDone, onError, onAction)
  }

  async function refreshCartSnapshot() {
    try {
      const snapshot = await fetchCart()
      applyCartSnapshot(snapshot)
    } catch {
      // Chat text should remain usable even if cart refresh fails.
    }
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
