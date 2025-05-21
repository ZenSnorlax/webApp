<template>
  <div class="main-container">
    <!-- 左侧已上传书籍列表 -->
    <div class="left-sidebar">
      <h2 class="sidebar-title">已上传书籍</h2>
      <!-- 书籍列表 -->
      <div class="up-list">
        <div v-for="book in upListBooks" :key="book.id" class="up-item">
          <img
            :src="book.coverUrl || '/default-cover.png'"
            alt="封面"
            class="up-cover"
            @error="handleCoverError"
          />
          <div class="up-info">
            <p class="up-title">{{ book.title || '未知书名' }}</p>
            <p class="up-author">{{ book.author || '未知作者' }}</p>
          </div>
        </div>
      </div>
      <!-- 下方分页控件 -->
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="upPageNum"
        :page-size="upPageSize"
        :total="upTotal"
        @current-change="handleUpPageChange"
        class="up-pagination bottom-pagination"
      />
    </div>

    <!-- 右侧上传模块 -->
    <div class="upload-container">
      <el-upload
        class="custom-upload"
        drag
        :multiple="false"
        :before-upload="beforeUpload"
        :show-file-list="false"
        :auto-upload="true"
      >
        <div class="drag-area" :class="{ 'dragover': isDragover }">
          <el-icon class="upload-icon" :class="{ 'pulse': isUploading }">
            <upload-filled />
          </el-icon>
          <div class="upload-text">
            <h3 class="title">拖拽 EPUB 文件到这里</h3>
            <p class="subtitle">支持单个或批量上传</p>
            <p class="limit">文件大小不超过 100MB</p>
          </div>
        </div>
        <template #tip>
          <div class="status-tip">
            <transition name="el-fade-in">
              <div v-if="isUploading" class="upload-status">
                <el-progress
                  :percentage="progress"
                  :stroke-width="16"
                  :show-text="false"
                  status="success"
                />
                <span class="uploading-text">正在上传中... {{ progress }}%</span>
              </div>
            </transition>
          </div>
        </template>
      </el-upload>
      <!-- 上传成功后的书籍信息展示 -->
      <transition name="el-zoom-in-top">
        <div v-if="bookInfo.id" class="book-info-card">
          <div class="cover-container">
            <img
              :src="bookInfo.coverUrl || '/default-cover.png'"
              alt="书籍封面"
              class="book-cover"
              @error="handleCoverError"
            >
          </div>
          <div class="meta-info">
            <h2 class="title">{{ bookInfo.title || '未知书名' }}</h2>
            <div class="meta-item">
              <span class="label">作者：</span>
              <span class="content">{{ bookInfo.author || '未知作者' }}</span>
            </div>
            <div class="meta-item">
              <span class="label">出版社：</span>
              <span class="content">{{ bookInfo.publisher || '未知出版社' }}</span>
            </div>
            <div class="meta-item">
              <span class="label">出版日期：</span>
              <span class="content">{{ formatDate(bookInfo.publishDate) }}</span>
            </div>
            <div class="meta-item description">
              <span class="label">内容简介：</span>
              <p class="content">{{ bookInfo.description || '暂无简介' }}</p>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { UploadFilled } from '@element-plus/icons-vue';
import { uploadBook, getUpListBook, type BookInfo } from "@/services/bookService";
import { ElNotification } from 'element-plus';

// 左侧已上传书籍列表数据
const upListBooks = ref<BookInfo[]>([]);
const upPageNum = ref(1);
const upPageSize = ref(5);
const upTotal = ref(0);

const fetchUpListBooks = async () => {
  try {
    const response = await getUpListBook(upPageNum.value, upPageSize.value);
    if (response && response.data) {
      upListBooks.value = response.data.bookInfos;
      upTotal.value = response.data.total;
    } else {
      console.error("获取已上传书籍数据失败:", response);
    }
  } catch (error) {
    console.error("获取已上传书籍数据异常:", error);
  }
};

const handleUpPageChange = (page: number) => {
  upPageNum.value = page;
  fetchUpListBooks();
};

onMounted(() => {
  fetchUpListBooks();
});

// 通用封面错误处理
const handleCoverError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  img.src = '/default-cover.png';
};

// 右侧上传模块相关数据
const isDragover = ref(false);
const isUploading = ref(false);
const progress = ref(0);
const bookInfo = reactive<BookInfo>({
  id: 0,
  coverUrl: "",
  title: "",
  author: "",
  publisher: "",
  publishDate: null,
  description: "",
  language: ""
});

const beforeUpload = async (file: File) => {
  if (!validateFileType(file)) {
    ElNotification({
      title: '文件类型错误',
      message: '仅支持 EPUB 格式文件',
      type: 'error',
      duration: 3000
    });
    return false;
  }

  isUploading.value = true;
  try {
    const response = await uploadBook(file);
    if (response.code === 200) {
      Object.assign(bookInfo, response.data);
      ElNotification({
        title: '上传成功',
        message: `${file.name} 已成功上传`,
        type: 'success',
        duration: 2500
      });
      // 上传成功后刷新左侧上传列表
      fetchUpListBooks();
    }
  } catch (error: any) {
    ElNotification({
      title: '上传失败',
      message: error.message || '文件上传过程中发生错误',
      type: 'error',
      duration: 4000
    });
  } finally {
    isUploading.value = false;
    progress.value = 0;
  }
  return false; // 阻止默认上传行为
};

const validateFileType = (file: File) => {
  return file.type === 'application/epub+zip' || file.name.toLowerCase().endsWith('.epub');
};

//格式化publishDate
const formatDate = (date: Date | string | null) => {
  if (!date) return "";
  const d = new Date(date);
  if (isNaN(d.getTime())) {
    return "";
  }
  return d.toLocaleDateString();
};

</script>

<style lang="scss" scoped>
.main-container {
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
}

/* 左侧已上传书籍列表 */
.left-sidebar {
  width: 300px;
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .sidebar-title {
    font-size: 1.4rem;
    margin-bottom: 1rem;
    text-align: center;
  }

  .up-pagination {
    margin-bottom: 1rem;
  }

  .up-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .up-item {
    display: flex;
    align-items: center;
    gap: 0.8rem;
    padding: 0.5rem;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .up-cover {
    width: 50px;
    height: 70px;
    object-fit: cover;
    border-radius: 4px;
  }

  .up-info {
    flex: 1;
    .up-title {
      font-size: 1rem;
      color: #333;
      margin: 0;
      font-weight: 500;
    }
    .up-author {
      font-size: 0.9rem;
      color: #666;
      margin: 0;
    }
  }
}

/* 右侧上传模块 */
.upload-container {
  flex: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 1.5rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  .custom-upload {
    :deep(.el-upload) {
      width: 100%;
      .el-upload-dragger {
        padding: 2.5rem;
        border: 2px dashed var(--el-color-primary-light-5);
        background: rgba(var(--el-color-primary-rgb), 0.03);
        transition: all 0.3s ease;
        &.dragover {
          border-color: var(--el-color-primary);
          background: rgba(var(--el-color-primary-rgb), 0.08);
          transform: translateY(-2px);
        }
      }
    }
  }

  .drag-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.2rem;
    padding: 3rem 2rem;
    transition: background 0.3s ease;

    .upload-icon {
      font-size: 3.5rem;
      color: var(--el-color-primary);
      margin-bottom: 1rem;
      transition: transform 0.3s ease;
      &.pulse {
        animation: pulse 1.5s infinite;
      }
    }

    .upload-text {
      text-align: center;
      .title {
        margin: 0;
        font-size: 1.4rem;
        color: var(--el-text-color-primary);
      }
      .subtitle {
        margin: 0.5rem 0;
        font-size: 1rem;
        color: var(--el-text-color-secondary);
      }
      .limit {
        margin: 0;
        font-size: 0.9rem;
        color: var(--el-text-color-placeholder);
      }
    }
  }

  .status-tip {
    margin-top: 1.5rem;
    .upload-status {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      background: rgba(var(--el-color-primary-rgb), 0.05);
      border-radius: 8px;
      :deep(.el-progress) {
        flex: 1;
      }
      .uploading-text {
        font-size: 0.9rem;
        color: var(--el-color-primary);
      }
    }
  }

  .book-info-card {
    margin-top: 2rem;
    padding: 1.5rem;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    display: flex;
    gap: 2rem;
    animation-duration: 0.3s;

    .cover-container {
      flex: 0 0 200px;
      .book-cover {
        width: 100%;
        height: 100%;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }

    .meta-info {
      flex: 1;
      .title {
        margin: 0 0 1rem;
        font-size: 1.8rem;
        color: var(--el-text-color-primary);
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        line-height: 1.4;
        max-height: 3.2em;
        line-clamp: 2; // 标准属性，用于兼容性
        @supports not (-webkit-line-clamp: 2) {
          white-space: normal;
          word-break: break-word;
          max-height: 3.2em;
        }
      }
      .meta-item {
        margin-bottom: 0.8rem;
        font-size: 0.95rem;
        &.description {
          .content {
            line-height: 1.6;
            color: var(--el-text-color-secondary);
          }
        }
        .label {
          color: var(--el-text-color-regular);
          font-weight: 500;
          min-width: 80px;
          display: inline-block;
        }
        .content {
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .book-info-card {
    flex-direction: column;
    .cover-container {
      flex: none;
      max-width: 150px;
      margin: 0 auto;
    }
  }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.08); }
  100% { transform: scale(1); }
}
</style>
