<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { frontApi } from '@/api/front'
import type { Article, PageResult, Tag } from '@/api/types'
import ArticleCard from '@/components/ArticleCard.vue'
import { PAGE_SIZE } from '@/config'
import { usePageReady } from '@/utils/pageReady'

const route = useRoute()
const page = ref(1)
const title = ref('标签')
const data = ref<PageResult<Article>>({ total: 0, list: [] })
const loaded = ref(false)
const beginReady = usePageReady()

async function load() {
  const pageReady = beginReady()
  try {
    const id = Number(route.params.id)
    const tags = await frontApi.tags()
    title.value = tags.find((t: Tag) => t.id === id)?.name || '标签'
    data.value = await frontApi.articles({ page: page.value, size: PAGE_SIZE, tagId: id })
  } finally {
    loaded.value = true
    pageReady()
  }
}

watch(() => route.params.id, () => { page.value = 1; load() })
onMounted(load)
</script>

<template>
  <div>
    <h1 class="page-title"># {{ title }}</h1>
    <div class="article-list">
      <ArticleCard v-for="item in data.list" :key="item.id" :article="item" />
    </div>
    <el-empty v-if="loaded && !data.list.length" />
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
