<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchOrder } from '../api/orders.js'

const route = useRoute()
const order = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')
let pollTimer = null

const statusText = computed(() => {
  const map = {
    PENDING: '待接单',
    PREPARING: '制作中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[order.value?.status] || order.value?.status || '--'
})

const paymentStatusText = computed(() => {
  const map = {
    PAID: '已支付',
    UNPAID: '未支付',
  }
  return map[order.value?.paymentStatus] || order.value?.paymentStatus || '未支付'
})

async function loadOrder() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    order.value = await fetchOrder(route.params.orderNo)
  } catch (error) {
    errorMessage.value = error.message || '订单加载失败'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadOrder()
  pollTimer = setInterval(loadOrder, 15000)
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<template>
  <section class="order-page">
    <div class="order-card glass-panel">
      <p class="section-kicker">订单状态</p>
      <h2>{{ route.params.orderNo }}</h2>

      <p v-if="isLoading" class="state-text">订单加载中...</p>
      <p v-else-if="errorMessage" class="state-text error">{{ errorMessage }}</p>

      <template v-else-if="order">
        <div class="status-pill">{{ statusText }}</div>
        <dl>
          <div>
            <dt>用餐方式</dt>
            <dd>{{ order.orderType === 'dine_in' ? '堂食' : '外带' }}</dd>
          </div>
          <div v-if="order.tableNumber">
            <dt>桌号</dt>
            <dd>{{ order.tableNumber }}</dd>
          </div>
          <div v-if="order.pickupNumber">
            <dt>取餐号</dt>
            <dd>{{ order.pickupNumber }}</dd>
          </div>
          <div>
            <dt>支付状态</dt>
            <dd>{{ paymentStatusText }}</dd>
          </div>
          <div>
            <dt>合计</dt>
            <dd>¥{{ Number(order.totalAmount || 0).toLocaleString('zh-CN') }}</dd>
          </div>
        </dl>

        <div class="items">
          <article v-for="item in order.items || []" :key="item.dishId">
            <span>{{ item.name }}</span>
            <strong>x{{ item.quantity }}</strong>
          </article>
        </div>
      </template>

      <router-link class="back-link" to="/menu">返回菜单</router-link>
    </div>
  </section>
</template>

<style scoped>
.order-page {
  padding: 48px 0 104px;
}

.order-card {
  max-width: 760px;
  margin: 0 auto;
  padding: clamp(22px, 5vw, 54px);
}

.glass-panel {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

.section-kicker {
  margin: 0 0 18px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

h2 {
  margin: 0 0 20px;
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(34px, 5vw, 62px);
  font-weight: 400;
}

.status-pill {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  padding: 0 16px;
  margin-bottom: 24px;
  background: var(--ink);
  color: #fff;
  font-weight: 800;
}

dl {
  display: grid;
  gap: 0;
  margin: 0 0 24px;
  border-top: 1px solid var(--line);
}

dl div,
.items article {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 15px 0;
  border-bottom: 1px solid var(--line);
}

dt {
  color: var(--soft);
}

dd {
  margin: 0;
  font-weight: 800;
}

.items {
  margin-bottom: 24px;
}

.state-text {
  color: var(--muted);
}

.state-text.error {
  color: #9b2c2c;
}

.back-link {
  color: var(--muted);
  text-decoration: none;
}
</style>
