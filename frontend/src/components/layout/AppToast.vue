<script setup>
import { uiState } from '../../composables/state.js'
</script>

<template>
  <transition name="fade-slide">
    <div v-if="uiState.toastMessage" class="toast-banner">{{ uiState.toastMessage }}</div>
  </transition>
  <div v-if="uiState.errorMessage" class="error-banner">{{ uiState.errorMessage }}</div>
</template>
