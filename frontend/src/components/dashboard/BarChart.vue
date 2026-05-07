<script setup>
import { computed } from 'vue'
import { formatPrice } from '../../utils/format.js'

const props = defineProps({ series: { type: Array, required: true } })

const maxValue = computed(() => Math.max(1, ...props.series.map((b) => b.value)))

const columns = computed(() => `repeat(${props.series.length}, minmax(0, 1fr))`)

function barColor(value) {
  const ratio = maxValue.value > 0 ? value / maxValue.value : 0
  const h = Math.round(173 - ratio * 168)
  const s = Math.round(73 + ratio * 9)
  const l = Math.round(38 + ratio * 26)
  return `hsl(${h}, ${s}%, ${l}%)`
}
</script>

<template>
  <div class="bar-chart modern-chart" :style="{ '--bar-columns': columns }">
    <div v-for="bar in series" :key="`${bar.label}-${bar.value}`" class="bar-item">
      <strong class="bar-value">{{ formatPrice(bar.value) }}</strong>
      <div class="bar-track">
        <span
          class="bar-fill"
          :class="{ delivery: bar.type === 'delivery' }"
          :style="{ height: `${bar.height}%`, background: bar.type === 'delivery' ? `linear-gradient(180deg, ${barColor(bar.value)}, var(--coral))` : `linear-gradient(180deg, ${barColor(bar.value)}, #7dcfc5)` }"
        />
      </div>
      <small>{{ bar.label }}</small>
    </div>
  </div>
</template>
