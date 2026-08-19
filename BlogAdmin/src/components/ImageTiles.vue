<script setup lang="ts">
import { computed, ref } from 'vue'
import Plus from '@vicons/tabler/es/Plus'
import Trash from '@vicons/tabler/es/Trash'
import ZoomIn from '@vicons/tabler/es/ZoomIn'
import { upload, deleteUpload } from '@/api/http'
import { mediaUrl } from '@/utils/format'

export interface ImageItem {
  imgUrl: string
  thumbnailUrl?: string
}

const images = defineModel<ImageItem[]>({ default: () => [] })
const previewIndex = ref(-1)

const previewList = computed(() => images.value.map((item) => mediaUrl(item.thumbnailUrl || item.imgUrl)))
const tileSrc = (item: ImageItem) => mediaUrl(item.thumbnailUrl || item.imgUrl)

async function addImage(file: File) {
  const res = await upload(file)
  images.value.push({ imgUrl: res.url, thumbnailUrl: res.thumbnailUrl || res.url })
  return false
}

async function removeAt(index: number) {
  const item = images.value[index]
  try {
    await deleteUpload(item.imgUrl, item.thumbnailUrl)
    images.value.splice(index, 1)
  } catch {
    // 错误已由请求拦截器提示
  }
}
</script>

<template>
  <div class="image-grid">
    <div v-for="(item, index) in images" :key="(item.imgUrl || '') + index" class="cover-box has-image">
      <img :src="tileSrc(item)" alt="" />
      <div class="cover-mask">
        <button type="button" title="预览" @click="previewIndex = index"><ZoomIn /></button>
        <button type="button" title="删除" @click="removeAt(index)"><Trash /></button>
      </div>
    </div>
    <el-upload :show-file-list="false" accept="image/*" multiple :before-upload="addImage">
      <div class="cover-box">
        <Plus class="cover-icon" />
        <span>上传图片</span>
      </div>
    </el-upload>
    <el-image-viewer
      v-if="previewIndex >= 0"
      :url-list="previewList"
      :initial-index="previewIndex"
      @close="previewIndex = -1"
    />
  </div>
</template>

<style scoped lang="scss">
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.cover-box {
  width: 148px;
  height: 148px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  overflow: hidden;
  position: relative;
}

.cover-box.has-image {
  border-style: solid;
  padding: 0;
}

.cover-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-icon {
  width: 28px;
  height: 28px;
}

.cover-mask {
  position: absolute;
  inset: 0;
  display: none;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: rgba(0, 0, 0, 0.45);
}

.cover-box.has-image:hover .cover-mask {
  display: flex;
}

.cover-mask button {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.cover-mask :deep(svg) {
  width: 18px;
  height: 18px;
}
</style>
