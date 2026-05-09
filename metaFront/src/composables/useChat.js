import { ref } from 'vue'
import { queryChat } from '../api/chat.js'

const isSupportOpen = ref(false)
const supportInput = ref('')
const isSending = ref(false)
const supportMessages = ref([
  {
    role: 'assistant',
    text: '您好，我是沐枫餐饮智能助手。可以为您推荐菜品、查询订单状态、了解配送信息。',
  },
])

const quickQuestions = ['今日招牌菜推荐', '查询我的订单', '了解配送范围', '预约包厢']

export function useChat() {
  async function askSupport(question) {
    const text = question || supportInput.value.trim()
    if (!text || isSending.value) return

    supportMessages.value.push({ role: 'user', text })
    supportInput.value = ''
    isSupportOpen.value = true

    const orderMatch = text.match(/ORD\d+/i)
    try {
      isSending.value = true
      const response = await queryChat({
        message: text,
        orderNo: orderMatch ? orderMatch[0].toUpperCase() : undefined,
      })
      supportMessages.value.push({
        role: 'assistant',
        text: response.message || '我已经收到您的问题，会尽快为您处理。',
      })
    } catch (error) {
      supportMessages.value.push({
        role: 'assistant',
        text: error.message || '智能客服暂时不可用，请稍后再试或联系店员。',
      })
    } finally {
      isSending.value = false
    }
  }

  function toggleSupport() {
    isSupportOpen.value = !isSupportOpen.value
  }

  function closeSupport() {
    isSupportOpen.value = false
  }

  return { isSupportOpen, supportInput, supportMessages, quickQuestions, isSending, askSupport, toggleSupport, closeSupport }
}
