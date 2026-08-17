<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { frontApi } from '@/api/front'
import type { Article, PageResult } from '@/api/types'
import ArticleCard from '@/components/ArticleCard.vue'
import { PAGE_SIZE } from '@/config'
import { usePageReady } from '@/utils/pageReady'

const page = ref(1)
const data = ref<PageResult<Article>>({ total: 0, list: [] })
const loaded = ref(false)
const beginReady = usePageReady()

async function load() {
  const pageReady = beginReady()
  try {
    data.value = await frontApi.articles({ page: page.value, size: PAGE_SIZE })
  } finally {
    loaded.value = true
    pageReady()
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
    <el-pagination
      v-if="data.total > PAGE_SIZE"
      class="pager"
      background
      layout="prev, pager, next"
      :total="data.total"
      :page-size="PAGE_SIZE"
      v-model:current-page="page"
      @current-change="load"
    />
  </div>
</template>
