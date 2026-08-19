<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { frontApi } from '@/api/front'
import type { WebUpdateLog } from '@/api/types'
import { formatTime } from '@/utils/format'
import { usePageReady } from '@/utils/pageReady'

const list = ref<WebUpdateLog[]>([])
const loaded = ref(false)
const beginReady = usePageReady()

onMounted(async () => {
  const pageReady = beginReady()
  try {
    list.value = await frontApi.webUpdateLogs()
  } finally {
    loaded.value = true
    pageReady()
  }
})
</script>

<template>
  <div>
    <h1 class="page-title">功能更新日志</h1>
    <div class="log-list">
      <article v-for="item in list" :key="item.id" class="item">
        <div class="head">
          <h2>{{ item.title }}</h2>
          <time>{{ formatTime(item.createTime) }}</time>
        </div>
        <p v-if="item.description">{{ item.description }}</p>
      </article>
    </div>
    <el-empty v-if="loaded && !list.length" description="暂无更新记录" />
  </div>
</template>

<style scoped lang="scss">
.log-list {
  display: flex;
  flex-direction: column;
  gap: 1.15rem;
}
.item {
  padding-bottom: 1.1rem;
  border-bottom: 1px dashed var(--line-divider);
}
.item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.head {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1rem;
  align-items: baseline;
  justify-content: space-between;
}
h2 {
  margin: 0;
  font-size: calc(1.15rem + 2px);
  color: var(--c-text-1);
}
time {
  color: var(--c-text-3);
  font-size: calc(0.85rem + 2px);
}
p {
  margin: 0.55rem 0 0;
  color: var(--c-text-2);
  line-height: 1.75;
  white-space: pre-wrap;
}
</style>
