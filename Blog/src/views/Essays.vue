<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
import { frontApi } from '@/api/front'
import type { Essay, PageResult } from '@/api/types'
import FrameBtn from '@/components/FrameBtn.vue'
import Calendar from '@vicons/tabler/es/Calendar'
import { formatDate, mediaUrl } from '@/utils/format'
import { usePageReady } from '@/utils/pageReady'

const PAGE_SIZE = 10
const page = ref(1)
const data = ref<PageResult<Essay>>({ total: 0, list: [] })
const waterfallRef = ref<{ renderer: () => void } | null>(null)
const loadingMore = ref(false)
const beginReady = usePageReady()
const loadedAll = computed(() => data.value.total > 0 && data.value.list.length >= data.value.total)

async function load() {
  const pageReady = beginReady()
  page.value = 1
  try {
    data.value = await frontApi.essays({ page: 1, size: PAGE_SIZE })
  } finally {
    pageReady()
  }
}

async function loadMore() {
  if (loadedAll.value || loadingMore.value) return
  loadingMore.value = true
  const next = page.value + 1
  try {
    const extra = await frontApi.essays({ page: next, size: PAGE_SIZE })
    page.value = next
    data.value = {
      total: extra.total,
      list: [...data.value.list, ...extra.list],
    }
  } finally {
    loadingMore.value = false
  }
}

function relayout() {
  waterfallRef.value?.renderer()
}

function imgSrc(img: { imgUrl?: string; thumbnailUrl?: string }) {
  return mediaUrl(img.thumbnailUrl || img.imgUrl)
}

function previewList(images?: Essay['images']) {
  return (images || []).map(imgSrc)
}

function imgsClass(count: number) {
  if (count === 1) return 'single'
  if (count === 2) return 'pair'
  return 'multi'
}

onMounted(load)
</script>

<template>
  <div>
    <Waterfall
      v-if="data.list.length"
      ref="waterfallRef"
      :list="data.list"
      row-key="id"
      :gutter="12"
      :has-around-gutter="false"
      :lazyload="false"
      :animation-cancel="true"
      align="left"
      background-color="transparent"
      :breakpoints="{
        2400: { rowPerView: 3 },
        700: { rowPerView: 2 },
        480: { rowPerView: 1 },
      }"
    >
      <template #default="{ item }">
        <article class="card card-base">
          <p>{{ item.content }}</p>
          <div
            v-if="item.images?.length"
            class="imgs"
            :class="imgsClass(item.images.length)"
          >
            <el-image
              v-for="(img, i) in item.images"
              :key="i"
              :src="imgSrc(img)"
              :preview-src-list="previewList(item.images)"
              :initial-index="i"
              :infinite=false
              :close-on-press-escape="false"
              :fit="item.images.length === 1 ? 'contain' : 'cover'"
              preview-teleported
              @load="relayout"
            />
          </div>
          <time>
            <Calendar class="tabler-icon" />
            <span>{{ formatDate(item.createTime) }}</span>
          </time>
        </article>
      </template>
    </Waterfall>
    <el-empty v-else description="暂无动态" />
    <div v-if="data.total" class="more-wrap">
      <FrameBtn sweep="ltr" :disabled="loadedAll" :loading="loadingMore" @click="loadMore">
        {{ loadedAll ? '~Bottom~' : 'Get More' }}
      </FrameBtn>
    </div>
  </div>
</template>

<style scoped lang="scss">
.card { padding: 1rem; }
p { margin: 0; color: var(--c-text-1); white-space: pre-wrap; word-break: break-word; }
.imgs {
  margin-top: 0.6rem;
}
.imgs :deep(.el-image) {
  display: block;
  width: 100%;
  border-radius: 0.5rem;
  overflow: hidden;
  cursor: zoom-in;
  background: var(--btn-regular-bg);
}
.imgs.pair,
.imgs.multi {
  display: grid;
  gap: 0.35rem;
}
.imgs.pair {
  grid-template-columns: repeat(2, 1fr);
}
.imgs.multi {
  grid-template-columns: repeat(3, 1fr);
}
.imgs.pair :deep(.el-image),
.imgs.multi :deep(.el-image) {
  aspect-ratio: 1;
}
.imgs :deep(.el-image img) {
  width: 100%;
  height: 100%;
}
time {
  display: flex;
  align-items: center;
  gap: 0.22rem;
  margin-top: 1rem;
  line-height: 1;
  color: var(--c-text-2);
  font-size: calc(0.85rem + 2px);
}

time :deep(svg) {
  width: 1em;
  height: 1em;
  display: block;
}

time span {
  position: relative;
  top: 0.08em;
}
.more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 1.5rem;
}
</style>
