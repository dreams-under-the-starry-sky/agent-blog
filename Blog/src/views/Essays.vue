<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Waterfall } from 'vue-waterfall-plugin-next'
import 'vue-waterfall-plugin-next/dist/style.css'
import { frontApi } from '@/api/front'
import type { Essay, PageResult } from '@/api/types'
import { formatTime, mediaUrl } from '@/utils/format'
import { usePageReady } from '@/utils/pageReady'

const page = ref(1)
const data = ref<PageResult<Essay>>({ total: 0, list: [] })
const waterfallRef = ref<{ renderer: () => void } | null>(null)
const beginReady = usePageReady()

async function load() {
  const pageReady = beginReady()
  try {
    data.value = await frontApi.essays({ page: page.value, size: 12 })
  } finally {
    pageReady()
  }
}

function relayout() {
  waterfallRef.value?.renderer()
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
          <div v-if="item.images?.length" class="imgs">
            <img
              v-for="(img, i) in item.images"
              :key="i"
              :src="mediaUrl(img.thumbnailUrl || img.imgUrl)"
              alt=""
              @load="relayout"
            />
          </div>
          <time>{{ formatTime(item.createTime) }}</time>
        </article>
      </template>
    </Waterfall>
    <el-empty v-else description="暂无动态" />
    <el-pagination
      v-if="data.total > 12"
      class="pager"
      background
      layout="prev, pager, next"
      :total="data.total"
      :page-size="12"
      v-model:current-page="page"
      @current-change="load"
    />
  </div>
</template>

<style scoped lang="scss">
.card { padding: 1rem; }
p { margin: 0; color: var(--c-text-1); white-space: pre-wrap; word-break: break-word; }
.imgs {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.6rem;
}
.imgs img {
  width: 100%;
  border-radius: 0.5rem;
  display: block;
}
time { display: inline-block; margin-top: 0.6rem; color: var(--c-text-2); font-size: 0.85rem; }
</style>
