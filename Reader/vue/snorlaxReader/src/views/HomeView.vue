<template>
  <div class="common-layout">
    <el-container>
      <!-- 头部区域 -->
      <el-header class="header">
        <!-- 用户信息区域 -->
        <div v-if="isLogin" class="userCenter">
          <div class="user-center" ref="userCenterRef" @click.stop="toggleDropdown">
            <div class="avatar-wrapper">
              <img class="avatar" :src="avatarSrc" alt="用户头像" />
            </div>
            <transition name="fade">
              <ul v-if="dropdownVisible" class="dropdown-menu" @click.stop>
                <li @click="goToMyShelf">
                  <el-link>我的书架</el-link>
                </li>
                <li @click="goToUpBook">
                  <el-link>上传图书</el-link>
                </li>
                <li @click="logout">
                  <el-link>退出登录</el-link>
                </li>
              </ul>
            </transition>
          </div>
        </div>
        <!-- 未登录时显示 -->
        <div v-else class="header__auth">
          <el-link @click="goToLogin" target="_blank">登录</el-link>
          <el-link @click="goToRegister" target="_blank">注册</el-link>
        </div>
        <!-- Logo 和搜索框 -->
        <div class="header__main">
          <img :src="logoIcon" alt="Logo" class="header__logo" />
          <SearchBox
            ref="mainSearchRef"
            v-model:modelValue="keywords"
            placeholder="搜索书籍..."
            class="header__search-input"
            @keyup.enter="handleSearch"
          />
        </div>
      </el-header>

      <!-- 主体内容区域 -->
      <el-main class="main">
        <div :class="['main__content', { 'main__content--blurred': showOverlay }]">
          <!-- 其他页面内容 -->
        </div>
        <!-- 搜索遮罩层 -->
        <div v-if="showOverlay" class="search-overlay">
          <div class="search-overlay__content">
            <div class="search-overlay__header">
              <BackButton @click="clearSearch" />
              <SearchBox
                ref="floatingSearchRef"
                v-model:modelValue="keywords"
                placeholder="搜索书籍..."
                class="search-overlay__input"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="search-overlay__results">
              <div v-if="searchResults.length" class="results__list">
                <div
                  v-for="book in searchResults"
                  :key="book.id"
                  class="results__item"
                >
                  <img
                    :src="book.coverUrl"
                    :alt="book.title"
                    class="results__cover"
                  />
                  <div class="results__info">
                    <h3 class="results__title">{{ book.title }}</h3>
                    <p class="results__author">{{ book.author }}</p>
                  </div>
                </div>
                <!-- 当有剩余数据时，显示剩余数量和加载更多按钮 -->
                <div class="results__load-more" v-if="remainingCount > 0">
                  <span>剩余 {{ remainingCount }} 条</span>
                  <el-button type="primary" @click="loadMore">
                    加载更多
                  </el-button>
                </div>
              </div>
              <div v-else class="results__empty">
                <p v-if="keywords">没有找到相关书籍</p>
                <p v-else>输入关键词开始搜索</p>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRouter } from 'vue-router';
import debounce from 'debounce';
import SearchBox from '@/components/SearchBox.vue';
import BackButton from '@/components/BackButton.vue';
import logoIcon from '@/assets/logoIcon.svg';
import { searchBook, BookInfo } from '@/services/bookService.ts';
import { useUserStore } from "@/stores/userStore.ts";

const isLogin = useUserStore().getLoginStatus;
const router = useRouter();

/* 用户中心相关逻辑 */
const dropdownVisible = ref(false);
const avatarSrc = 'https://thirdwx.qlogo.cn/mmopen/vi_32/ohECaicuECw2WW0qiaL9rYpmFGrTXiaNvCAwSL7aW21HCLljfia4bcM8gWsXt1Wib0AEvnyM3hQ0psZfvx1OZSzFIeVeehWn57ay3dVj52wjuiazk/46';

const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value;
};

const goToMyShelf = () => {
  router.push({ name: 'myShelf' });
};

const goToUpBook = () => {
  router.push({ name: 'upBook' });
};

const logout = async () => {
  try {
    alert('退出登录成功');
    document.cookie = 'token=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/';
    useUserStore().setLoginStatus(false);
    console.log('退出登录');
  } catch (error) {
    console.error('退出登录时发生错误:', error);
  }
};

const userCenterRef = ref<HTMLElement | null>(null);
const handleClickOutside = (event: MouseEvent) => {
  if (userCenterRef.value && !userCenterRef.value.contains(event.target as Node)) {
    dropdownVisible.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
});

/* 搜索相关逻辑 */
const keywords = ref<string>('');
const searchResults = ref<BookInfo[]>([]);
const showOverlay = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const floatingSearchRef = ref<InstanceType<typeof SearchBox>>();
const mainSearchRef = ref<InstanceType<typeof SearchBox>>();

const fetchSearchResults = async (append = false) => {
  if (!keywords.value.trim()) {
    searchResults.value = [];
    total.value = 0;
    return;
  }
  try {
    const response = await searchBook(keywords.value.trim(), currentPage.value, pageSize.value);
    if (response && response.data) {
      if (append) {
        searchResults.value = [...searchResults.value, ...response.data.bookInfos];
      } else {
        searchResults.value = response.data.bookInfos;
      }
      total.value = response.data.total;
    } else {
      alert('搜索失败');
      console.error('搜索失败:', response);
    }
  } catch (error) {
    alert('搜索失败');
    console.error('搜索失败:', error);
  }
};

const debouncedSearch = debounce(() => fetchSearchResults(false), 300);

watch(keywords, (newVal, oldVal) => {
  if (showOverlay.value && newVal.trim() !== oldVal.trim()) {
    currentPage.value = 1; // 关键字变化时重置页码
    debouncedSearch();
  }
});

const handleSearch = (event: Event) => {
  event.preventDefault();
  if (keywords.value.trim()) {
    showOverlay.value = true;
    currentPage.value = 1;
    fetchSearchResults(false);
  }
};

const clearSearch = () => {
  keywords.value = '';
  searchResults.value = [];
  total.value = 0;
  showOverlay.value = false;
  nextTick(() => {
    mainSearchRef.value?.focus();
  });
};

watch(showOverlay, async (newVal) => {
  if (newVal) {
    await nextTick();
    floatingSearchRef.value?.focus();
  }
});

// 计算剩余数据条数
const remainingCount = computed(() => total.value - searchResults.value.length);

// 加载更多，分页时将新数据追加
const loadMore = async () => {
  currentPage.value += 1;
  await fetchSearchResults(true);
};

const goToLogin = () => router.push({ name: 'login' });
const goToRegister = () => router.push({ name: 'register' });
</script>

<style scoped>
:root {
  --primary-color: #646cff;
  --bg-color: #ffffff;
  --text-color: #333;
  --subtext-color: #666;
  --shadow-color: rgba(0, 0, 0, 0.1);
}

/* 公共布局 */
.common-layout {
  min-height: 100vh;
  background-color: var(--bg-color);
}

/* 头部区域 */
.header {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  box-shadow: 0 2px 8px var(--shadow-color);
  position: relative;
}

/* 用户信息区域 */
.userCenter {
  position: absolute;
  top: 1rem;
  right: 1rem;
}

/* 用户中心组件 */
.user-center {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--primary-color);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dropdown-menu {
  position: absolute;
  top: 60px;
  right: 0;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  padding: 0.5rem 0;
  list-style: none;
  z-index: 100;
  min-width: 120px;
}

.dropdown-menu li {
  padding: 0.5rem 1rem;
}

.dropdown-menu li:hover {
  background-color: #f9f9f9;
}

/* 未登录区域 */
.header__auth {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  gap: 1rem;
}

.header__auth .el-link {
  font-size: 16px;
  font-weight: 500;
  color: var(--primary-color);
  padding: 0.5rem 1rem;
  border: 1px solid var(--primary-color);
  border-radius: 5px;
  transition: background-color 0.2s, color 0.2s;
}

.header__auth .el-link:hover {
  background-color: var(--primary-color);
  color: #fff;
}

/* 主头部区域 */
.header__main {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 3rem;
  gap: 0.8rem;
}

.header__logo {
  height: 30px;
  margin-bottom: 0.5rem;
}

.header__search-input {
  width: 100%;
  max-width: 400px;
}

/* 主体区域 */
.main {
  position: relative;
  padding: 2rem;
}

.main__content {
  transition: filter 0.3s ease-out;
}

.main__content--blurred {
  filter: blur(6px);
}

/* 搜索遮罩层 */
.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  z-index: 1000;
  overflow-y: auto;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.search-overlay__content {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.search-overlay__header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.search-overlay__input {
  flex: 1;
  padding: 0.2rem 1rem;
  border-radius: 30px;
  border: 2px solid var(--primary-color);
  box-shadow: 0 4px 12px rgba(100, 108, 255, 0.2);
  font-size: 1.1rem;
  transition: border-color 0.3s;
}

.search-overlay__input:focus-within {
  border-color: var(--primary-color);
}

.search-overlay__results {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.results__list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.results__item {
  display: flex;
  align-items: center;
  background: var(--bg-color);
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px var(--shadow-color);
  transition: transform 0.2s ease;
}

.results__item:hover {
  transform: translateY(-3px);
}

.results__cover {
  height: 120px;
  border-radius: 4px;
  margin-right: 1.5rem;
  object-fit: cover;
  flex-shrink: 0;
}

.results__info {
  flex: 1;
}

.results__title {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-color);
}

.results__author {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: var(--subtext-color);
}

.results__empty {
  text-align: center;
  padding: 2rem;
  color: var(--subtext-color);
}

/* 加载更多区域 */
.results__load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 1rem 0;
  border-top: 1px solid #f0f0f0;
}
</style>
