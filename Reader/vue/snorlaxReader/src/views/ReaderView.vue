<template>
  <div class="reader-container">
    <!-- 目录侧边栏（滑入面板） -->
    <transition name="slide">
      <aside v-if="showTOC" class="toc-panel">
        <header class="toc-header">
          <h2>目录</h2>
          <button class="toc-close-btn" @click="toggleTOC">关闭</button>
        </header>
        <ul class="toc-list">
          <li v-for="item in toc" :key="item.href">
            <div class="toc-item" @click="goTo(item.href)">{{ item.label }}</div>
            <ul v-if="item.subitems && item.subitems.length" class="toc-sublist">
              <li v-for="sub in item.subitems" :key="sub.href">
                <div class="toc-item" @click="goTo(sub.href)">{{ sub.label }}</div>
              </li>
            </ul>
          </li>
        </ul>
      </aside>
    </transition>

    <!-- 遮罩层，点击后关闭目录 -->
    <div v-if="showTOC" class="overlay" @click="toggleTOC"></div>

    <!-- 主内容区域 -->
    <div class="content-wrapper" :class="{ shrink: showTOC }">
      <header class="content-header">
        <button class="toggle-toc-btn" @click="toggleTOC">目录</button>
      </header>
      <main class="reader-content">
        <div id="reader-view" class="reader-view"></div>
        <div v-if="loading" class="loading">加载中...</div>
        <div v-if="error" class="error">{{ error }}</div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Epub from 'epubjs';
import { generateBookTempUrl } from '@/services/bookService';
import { max } from 'lodash-es';

const url = ref('');
const loading = ref(true);
const error = ref('');
const toc = ref([]); // 目录数据
const showTOC = ref(false); // 控制目录侧边栏显示/隐藏

let rendition = null;

/**
 * 获取电子书临时链接
 */
const fetchBookTempUrl = async () => {
  try {
    const response = await generateBookTempUrl(135);
    if (response.code === 200) {
      url.value = response.data;
    } else {
      error.value = '获取电子书链接失败，请稍后重试';
      loading.value = false;
    }
  } catch (err) {
    error.value = '获取电子书链接失败，请稍后重试';
    loading.value = false;
    throw err;
  }
};

/**
 * 跳转到指定章节
 */
const goTo = async (href) => {
  try {
    await rendition.display(href);
    // 跳转后自动关闭目录
    showTOC.value = false;
  } catch (err) {
    console.error('跳转章节失败:', err);
  }
};

/**
 * 切换目录显示/隐藏
 */
const toggleTOC = () => {
  showTOC.value = !showTOC.value;
};

/**
 * 初始化电子书阅读器
 */
const initEpubReader = async () => {
  try {
    if (!url.value) {
      error.value = '初始化电子书阅读器失败';
      loading.value = false;
      return;
    }

    const book = Epub(url.value);
    const element = 'reader-view';

    rendition = book.renderTo(element, {
      width: '100%',
      height: '100%',
      layout: 'fixed',
      manager: 'continuous',
      flow: 'scrolled',
      snap: true
    });

    // 等待书籍元数据加载完成
    await book.ready;

    // 获取目录数据（包括章节信息）
    const nav = await book.loaded.navigation;
    toc.value = nav.toc;

    // 生成位置映射，参数 1600 表示大致生成的字符数分割数
    const locations = await book.locations.generate(1600);
    await rendition.display(locations[0]);

    // 注册自定义主题样式
    rendition.themes.register('default', {
      body: {
        'font-size': '18px !important',
        'line-height': '1.6 !important',
        'padding': '20px !important',
      },
    }
    );
    rendition.themes.default({
      body: {
        'column-count': '1 !important',
        'column-width': '100%',
        'column-gap': '1em',
      },
    });

    rendition.layout();
    rendition.on('rendered', (section) => {
      console.log('章节渲染完成:', section.href);
    });

    rendition.on('relocated', (location) => {
      console.log('当前位置:', location.start.cfi);
    });

    // 解决初始化渲染问题：延迟调用 resize 并触发 window 的 resize 事件
    setTimeout(() => {
      rendition?.resize(window.innerWidth, window.innerHeight);
      window.dispatchEvent(new Event('resize'));
    }, 500);

  } catch (err) {
    error.value = '电子书加载失败：' + err.message;
    console.error('初始化失败：', err);
  } finally {
    loading.value = false;
  }
};

/**
 * 组件挂载后依次获取链接和初始化阅读器
 */
onMounted(async () => {
  try {
    await fetchBookTempUrl();
    await initEpubReader();
  } catch (err) {
    console.error('初始化失败：', err);
  }
});
</script>

<style scoped>
/* 整体容器 */
.reader-container {
  position: relative;
  height: 100vh;
  background-color: #f8f8f8;
  overflow: hidden;
}

/* TOC侧边栏（滑入面板），参考微信读书网页版简洁风格 */
.toc-panel {
  position: fixed;
  top: 0;
  left: 0;
  width: 280px;
  height: 100vh;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 300;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
}

.toc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.toc-header h2 {
  font-size: 20px;
  margin: 0;
}

.toc-close-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #007aff;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  padding: 8px 0;
  cursor: pointer;
  color: #333;
  border-bottom: 1px solid #eee;
}

.toc-item:hover {
  color: #007aff;
}

.toc-sublist {
  list-style: none;
  padding-left: 15px;
  margin-top: 5px;
}

/* 遮罩层 */
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 250;
}

/* 主内容区域 */
.content-wrapper {
  position: relative;
  height: 100%;
  margin: 0 auto;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.content-wrapper.shrink {
  transform: scale(0.95);
  opacity: 0.9;
}

/* 内容头部 */
.content-header {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 100;
}

.toggle-toc-btn {
  padding: 8px 16px;
  background-color: #007aff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 电子书内容卡片 */
.reader-content {
  margin-bottom: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  box-sizing: border-box;
}

/* 电子书展示区域 */
.reader-view {
  width: 100%;
  height: 1000%;
  background-color: #fff;
  box-sizing: border-box;

}

/* 确保内部 iframe 自适应 */
:deep(#reader-view iframe) {
  width: 100% !important;
  height: 100% !important;
  box-sizing: border-box;
}

/* 滑入动画 */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-enter-to {
  transform: translateX(0);
}

.slide-leave-from {
  transform: translateX(0);
}

.slide-leave-to {
  transform: translateX(-100%);
}

/* 加载和错误提示样式 */
.loading,
.error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  border-radius: 8px;
  z-index: 400;
}
</style>
