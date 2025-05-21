<!-- BackButton.vue -->
<template>
  <button class="back-button" @click="$emit('click')" aria-label="返回">
    <img :src="backIcon" alt="返回" />
  </button>
</template>

<script setup lang="ts">
import backIcon from '@/assets/backIcon.svg';
</script>

<style scoped>
.back-button {
  border: none;
  background: none;
  padding: 0.5rem;
  cursor: pointer;
  color: var(--subtext-color);
  transition: color 0.2s;
}
.back-button:hover {
  color: var(--text-color);
}
</style>
