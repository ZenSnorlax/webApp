import { defineStore } from "pinia";
import { ref, computed } from "vue";

export const useUserStore = defineStore('user', () => {
    const loginStatus = ref(false);

    function setLoginStatus(status: boolean) {
        loginStatus.value = status;
    }

    const getLoginStatus = computed(() => {
        return loginStatus 
    });

    return {
        setLoginStatus,
        getLoginStatus
    }
});
