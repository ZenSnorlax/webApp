<script setup lang="ts">
import { computed, ref} from 'vue';
import SearchIcon from '@/assets/searchIcon.svg';

export interface SearchBoxProps {
  modelValue: string;
  placeholder?: string;
}

const props = defineProps<SearchBoxProps>();
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const inputRef = ref<HTMLInputElement | null>(null);

// 使用 computed 直接双向绑定 v-model
const inputVal = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
});

const focus = () => {
  inputRef.value?.focus();
};

defineExpose({
  input: inputRef,
  focus
});
</script>

<template>
  <div class="search-container">
    <img
      :src="SearchIcon"
      class="search-icon"
      alt="搜索图标"
    />
    <input
      ref="inputRef"
      v-model="inputVal"
      :placeholder="placeholder ?? '请输入关键字'"
      type="text"
      class="search-input"
    />
  </div>
</template>

<style scoped>
.search-container {
  display: flex;
  align-items: center;
  width: 100%;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 0 10px;
  box-sizing: border-box;
  transition: border-color 0.3s;
  background-color: #fff;
}

.search-container:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 4px rgba(64, 158, 255, 0.3);
}

.search-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  padding: 10px 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}
</style>
