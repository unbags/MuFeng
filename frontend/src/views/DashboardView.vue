<script setup>
import { useOrderingStore } from '../composables/useOrderingStore'
import { useDashboard } from '../composables/useDashboard'
import { formatPrice, formatOrderType, formatDateTime, formatOrderStatus } from '../utils/format.js'
import MetricCard from '../components/ui/MetricCard.vue'
import BarChart from '../components/dashboard/BarChart.vue'
import CategoryMeter from '../components/dashboard/CategoryMeter.vue'
import OrderTypeMix from '../components/dashboard/OrderTypeMix.vue'
import RecentOrderCard from '../components/dashboard/RecentOrderCard.vue'

const store = useOrderingStore()
const dashboard = useDashboard()
</script>

<template>
  <section class="screen-page dashboard-page">
    <header class="page-header">
      <div>

        
        <h2>今日经营状态</h2>
      </div>
      <button class="ghost-btn" @click="store.refreshAllData">刷新数据</button>
    </header>

    <section class="metric-strip">
      <article class="metric-card motion-rise">
        <span>今日订单</span>
        <strong>{{ store.admin.dashboard.todayOrderCount }}</strong>
        <small>实时同步下单记录</small>
      </article>
      <article class="metric-card motion-rise">
        <span>今日流水</span>
        <strong>{{ formatPrice(store.admin.dashboard.todayRevenue) }}</strong>
        <small>今日成交金额</small>
      </article>
      <article class="metric-card motion-rise">
        <span>累计流水</span>
        <strong>{{ formatPrice(store.admin.dashboard.totalRevenue) }}</strong>
        <small>历史订单累计</small>
      </article>
      <article class="metric-card motion-rise">
        <span>商品状态</span>
        <strong>{{ store.admin.dashboard.availableDishCount }}/{{ store.admin.dashboard.unavailableDishCount }}</strong>
        <small>上架 / 下架</small>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel chart-panel dashboard-chart-main">
        <div class="panel-head">
          <div>
            <span class="section-label">销售走势</span>
            <h3>订单金额趋势</h3>
          </div>
          <div class="chart-range-toggles">
            <button
              v-for="opt in [{ key: 'week', label: '近一周' }, { key: 'month', label: '近一月' }, { key: 'year', label: '近一年' }]"
              :key="opt.key"
              class="range-toggle-btn"
              :class="{ active: store.state.dashboardTimeRange === opt.key }"
              @click="dashboard.setTimeRange(opt.key)"
            >{{ opt.label }}</button>
          </div>
        </div>
        <BarChart :series="store.revenueSeries" />
      </article>

      <div class="dashboard-side-stack">
        <article class="panel chart-panel dashboard-compact-card dashboard-product-structure">
          <div class="panel-head">
            <div>
              <span class="section-label">商品销量</span>
              <h3>销售排名</h3>
            </div>
          </div>
          <CategoryMeter :series="store.productSalesSeries" />
        </article>

        <article class="panel chart-panel dashboard-compact-card dashboard-order-type">
          <div class="panel-head">
            <div>
              <span class="section-label">订单类型</span>
              <h3>堂食 / 外带</h3>
            </div>
          </div>
          <OrderTypeMix :mix="store.orderTypeMix" />
        </article>
      </div>

      <article class="panel recent-orders-panel dashboard-orders expanded">
        <div class="panel-head">
          <div>
            <span class="section-label">历史订单</span>
            <h3>最近订单</h3>
          </div>
          <small>{{ store.recentOrders.length }} 条</small>
        </div>
        <div v-if="store.recentOrders.length" class="recent-orders-scroll dashboard-recent-scroll">
          <div class="recent-order-list compact roomy dashboard-recent-list">
            <RecentOrderCard
              v-for="order in store.recentOrders"
              :key="order.orderNo"
              :order="order"
              @view-detail="store.openOrderDetail"
            />
          </div>
        </div>
        <div v-else class="empty-card">
          <strong>暂无订单</strong>
          <p>点餐工作台提交订单后会出现在这里。</p>
        </div>
      </article>
    </section>

    <transition name="mask-fade">
      <div v-if="store.state.showOrderDetail" class="modal-mask" @click="store.closeOrderDetail">
        <section class="modal-card checkout-modal order-detail-modal" @click.stop>
          <div class="modal-head checkout-head">
            <div>
              <span class="section-label">订单详情</span>
              <h3>{{ store.state.selectedOrderDetail?.orderNo || '正在加载订单' }}</h3>
            </div>
            <button class="close-btn" @click="store.closeOrderDetail">&times;</button>
          </div>

          <div v-if="store.state.orderDetailLoading" class="order-detail-loading">
            <div class="skeleton-head"></div>
            <div class="skeleton-grid"><span v-for="i in 4" :key="i"></span></div>
          </div>

          <div v-else-if="store.state.orderDetailError" class="empty-card order-detail-error">
            <strong>订单详情加载失败</strong>
            <p>{{ store.state.orderDetailError }}</p>
            <button class="primary-btn" @click="store.closeOrderDetail">关闭</button>
          </div>

          <div v-else-if="store.state.selectedOrderDetail" class="order-detail-layout">
            <section class="checkout-items-panel">
              <div class="checkout-section-head">
                <strong>商品明细</strong>
                <small>{{ store.state.selectedOrderDetail.itemCount }} 件商品</small>
              </div>
              <div class="checkout-item-scroll order-detail-items">
                <article v-for="item in (store.state.selectedOrderDetail.items || [])" :key="`${item.dishId}-${item.name}`" class="checkout-item-row order-detail-item-row">
                  <div class="checkout-item-media"><span>{{ (item.name || '商').slice(0, 1) }}</span></div>
                  <div class="checkout-item-copy">
                    <strong>{{ item.name || '未知商品' }}</strong>
                    <small>{{ formatPrice(item.price) }} &times; {{ item.quantity }}</small>
                  </div>
                  <strong>{{ formatPrice(item.total) }}</strong>
                </article>
              </div>
            </section>

            <aside class="checkout-summary-panel">
              <div class="checkout-section-head">
                <strong>订单信息</strong>
                <small>{{ formatOrderType(store.state.selectedOrderDetail.orderType) }}</small>
              </div>
              <div class="order-detail-meta">
                <article><span>订单状态</span><strong>{{ formatOrderStatus(store.state.selectedOrderDetail.status) }}</strong></article>
                <article v-if="store.state.selectedOrderDetail.tableNumber">
                  <span>桌号</span>
                  <strong>{{ store.state.selectedOrderDetail.tableNumber }}</strong>
                </article>
                <article v-if="store.state.selectedOrderDetail.pickupNumber">
                  <span>取餐号</span>
                  <strong>{{ store.state.selectedOrderDetail.pickupNumber }}</strong>
                </article>
                <article><span>支付状态</span><strong>{{ store.state.selectedOrderDetail.paymentStatus || 'UNPAID' }}</strong></article>
                <article><span>下单时间</span><strong>{{ formatDateTime(store.state.selectedOrderDetail.createdAt) }}</strong></article>
                <article><span>商品小计</span><strong>{{ formatPrice(store.state.selectedOrderDetail.subtotal) }}</strong></article>
                <article v-if="Number(store.state.selectedOrderDetail.deliveryFee || 0) > 0">
                  <span>外带服务费</span>
                  <strong>{{ formatPrice(store.state.selectedOrderDetail.deliveryFee) }}</strong>
                </article>
                <article class="total"><span>实收合计</span><strong>{{ formatPrice(store.state.selectedOrderDetail.totalAmount) }}</strong></article>
              </div>
              <div class="receipt-note modern order-detail-note">
                <span>订单备注</span>
                <p>{{ store.state.selectedOrderDetail.note || '暂无备注' }}</p>
              </div>
              <div class="checkout-actions single">
                <button
                  v-for="action in store.nextOrderActions"
                  :key="action.status"
                  class="primary-btn"
                  :disabled="store.state.actionLoading"
                  @click="store.changeSelectedOrderStatus(action.status, action.reason)"
                >
                  {{ action.label }}
                </button>
                <button class="ghost-btn" @click="store.closeOrderDetail">关闭详情</button>
              </div>
            </aside>
          </div>
        </section>
      </div>
    </transition>
  </section>
</template>
