<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { frontApi } from '@/api/front'
import type { Article, PageResult } from '@/api/types'
import ArticleCard from '@/components/ArticleCard.vue'
import FramePager from '@/components/FramePager.vue'
import { usePageReady } from '@/utils/pageReady'

const PAGE_SIZE = 10
const page = ref(1)
const data = ref<PageResult<Article>>({ total: 0, list: [] })
const loaded = ref(false)
const paging = ref<'prev' | 'next' | null>(null)
const beginReady = usePageReady()
const hasPrev = computed(() => page.value > 1)
const hasNext = computed(() => page.value * PAGE_SIZE < data.value.total)

async function load() {
  const pageReady = beginReady()
  try {
    data.value = await frontApi.articles({ page: page.value, size: PAGE_SIZE })
  } finally {
    loaded.value = true
    pageReady()
  }
}

async function go(nextPage: number, dir: 'prev' | 'next') {
  if (paging.value) return
  paging.value = dir
  try {
    const res = await frontApi.articles({ page: nextPage, size: PAGE_SIZE })
    page.value = nextPage
    data.value = res
  } finally {
    paging.value = null
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="article-list">
      <ArticleCard v-for="item in data.list" :key="item.id" :article="item" />
    </div>
    <el-empty v-if="loaded && !data.list.length" description="暂无文章" />
    <FramePager
      :has-prev="hasPrev"
      :has-next="hasNext"
      :paging="paging"
      @prev="go(page - 1, 'prev')"
      @next="go(page + 1, 'next')"
    />
  </div>
</template>
