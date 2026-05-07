<script setup>
defineProps({
  modelValue: { type: Number, default: 0 },
  min: { type: Number, default: 0 },
})
defineEmits(['update:modelValue'])
</script>

<template>
  <div class="qty-group compact">
    <button
      class="qty-btn subtle"
      :disabled="modelValue <= min"
      @click="$emit('update:modelValue', modelValue - 1)"
    >-</button>
    <span>{{ modelValue }}</span>
    <button class="qty-btn" @click="$emit('update:modelValue', modelValue + 1)">+</button>
  </div>
</template>
