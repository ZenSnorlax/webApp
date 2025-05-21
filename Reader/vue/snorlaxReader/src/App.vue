<template>
  <main class="container">
    <router-view />
  </main>
</template>
<script setup lang="ts">
import { useUserStore } from '@/stores/userStore';
import { onMounted } from 'vue';
import { checkCookieValid } from '@/services/authService';

onMounted(async () => {
  try {
    const response = await checkCookieValid();
    if (response.code === 200) {
      useUserStore().setLoginStatus(true);
    }
    else {
      useUserStore().setLoginStatus(false);
      console.log(response.message);
    }
  } catch (error) {
    console.error(error);
  }
});

</script>

<style scoped></style>
