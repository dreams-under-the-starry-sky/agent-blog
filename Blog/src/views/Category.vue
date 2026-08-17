<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArrowLeft from '@vicons/tabler/es/ArrowLeft'
import { frontApi } from '@/api/front'
import type { Article, Category, PageResult } from '@/api/types'
import ArticleCard from '@/components/ArticleCard.vue'
import { PAGE_SIZE } from '@/config'
import { usePageReady } from '@/utils/pageReady'

const route = useRoute()
const router = useRouter()
const page = ref(1)
const cats = ref<Category[]>([])
const data = ref<PageResult<Article>>({ total: 0, list: [] })

const selectedId = computed(() => {
  const id = Number(route.params.id)
  return Number.isFinite(id) && id > 0 ? id : null
})
const current = computed(() => cats.value.find((item) => item.id === selectedId.value) || null)
const visibleCats = computed(() => cats.value.filter((item) => (item.count || 0) > 0))
const beginReady = usePageReady()

async function loadCats() {
  cats.value = await frontApi.categories()
}

async function loadArticles() {
  if (!selectedId.value) {
    data.value = { total: 0, list: [] }
    return
  }
  data.value = await frontApi.articles({
    page: page.value,
    size: PAGE_SIZE,
    categoryId: selectedId.value,
  })
}

watch(
  () => route.params.id,
  async () => {
    const pageReady = beginReady()
    page.value = 1
    try {
      await loadArticles()
    } finally {
      pageReady()
    }
  },
)

onMounted(async () => {
  const pageReady = beginReady()
  try {
    await loadCats()
    await loadArticles()
  } finally {
    pageReady()
  }
})
</script>

<template>
  <div class="category-page">
    <div class="card-base cat-box" :class="{ selected: current }">
      <template v-if="current">
        <button class="back" type="button" title="返回" @click="router.push('/category')">
          <ArrowLeft />
        </button>
        <h2>{{ current.name }}</h2>
      </template>
      <template v-else>
        <button
          v-for="item in visibleCats"
          :key="item.id"
          class="chip"
          type="button"
          @click="router.push(`/category/${item.id}`)"
        >
          <span class="name">{{ item.name }}</span>
          <em>{{ item.count || 0 }}</em>
        </button>
        <el-empty v-if="!visibleCats.length" description="暂无分类" />
      </template>
    </div>

    <template v-if="current">
      <div class="article-list">
        <ArticleCard v-for="item in data.list" :key="item.id" :article="item" />
      </div>
      <el-empty v-if="!data.list.length" description="该分类暂无文章" />
      <el-pagination
        v-if="data.total > PAGE_SIZE"
        class="pager"
        background
        layout="prev, pager, next"
        :total="data.total"
        :page-size="PAGE_SIZE"
        v-model:current-page="page"
        @current-change="loadArticles"
      />
    </template>
  </div>
</template>

<style scoped lang="scss">
.category-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cat-box {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  padding: 1.1rem 1.25rem;
}

.cat-box.selected {
  display: grid;
  grid-template-columns: 2.6rem 1fr 2.6rem;
  align-items: center;
  min-height: 4.25rem;
}

.back {
  width: 2.4rem;
  height: 2.4rem;
  border: none;
  border-radius: 0.7rem;
  background: var(--btn-regular-bg);
  color: var(--btn-content);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  :deep(svg) { width: 1.2rem; height: 1.2rem; }
}

.back:hover {
  background: var(--btn-content);
  color: #fff;
}

h2 {
  margin: 0;
  text-align: center;
  font-size: 1.75rem;
  color: var(--c-text-1);
}

.chip {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.7rem 0.7rem 0.7rem 1rem;
  border: none;
  border-radius: 0.7rem;
  background: transparent;
  color: var(--c-text-1);
  font: inherit;
  font-size: 1.15rem;
  cursor: pointer;
  text-align: left;
}

.chip .name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: transform 0.2s ease;
}

.chip:hover {
  background: var(--btn-regular-bg);
  color: var(--btn-content);
}

.chip:hover .name {
  transform: translateX(0.4rem);
}

em {
  font-style: normal;
  width: 1.85rem;
  height: 1.85rem;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.4rem;
  border: 1px solid var(--line-divider);
  background: var(--card-bg);
  color: var(--btn-content);
  font-size: 1rem;
  line-height: 1;
}

.chip:hover em {
  background: var(--btn-content);
  border-color: var(--btn-content);
  color: #fff;
}
</style>
