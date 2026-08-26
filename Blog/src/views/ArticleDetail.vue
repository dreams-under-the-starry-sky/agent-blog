<script setup lang="ts">
import { computed, inject, onMounted, onUnmounted, ref, watch, type Ref } from 'vue'
import { useRoute } from 'vue-router'
import Bookmark from '@vicons/tabler/es/Bookmark'
import Calendar from '@vicons/tabler/es/Calendar'
import { frontApi } from '@/api/front'
import type { Article } from '@/api/types'
import CommentTree from '@/components/CommentTree.vue'
import MarkdownBody from '@/components/MarkdownBody.vue'
import { formatDate, mediaUrl } from '@/utils/format'
import { renderArticle, type Heading } from '@/utils/markdown'
import { usePageReady } from '@/utils/pageReady'

const route = useRoute()
const article = ref<Article | null>(null)
const rendered = computed(() => renderArticle(article.value?.content, article.value?.images))
const html = computed(() => rendered.value.html)
const tocHeadings = inject<Ref<Heading[]>>('tocHeadings')
const beginReady = usePageReady()
const coverSrc = ref('')

watch(
  () => [article.value?.thumbnail, article.value?.cover],
  () => {
    coverSrc.value = mediaUrl(article.value?.thumbnail || article.value?.cover)
  },
  { immediate: true },
)

function onCoverError() {
  const fallback = mediaUrl(article.value?.cover)
  if (fallback && coverSrc.value !== fallback) {
    coverSrc.value = fallback
  }
}

async function load() {
  const pageReady = beginReady()
  try {
    article.value = await frontApi.article(String(route.params.id))
  } finally {
    pageReady()
  }
}

watch(rendered, (value) => {
  if (tocHeadings) tocHeadings.value = value.headings
}, { immediate: true })

watch(() => route.params.id, load)
onMounted(load)
onUnmounted(() => {
  if (tocHeadings) tocHeadings.value = []
})
</script>

<template>
  <article v-if="article" class="post">
    <img
      v-if="article.cover || article.thumbnail"
      class="cover"
      :src="coverSrc"
      :alt="article.title"
      @error="onCoverError"
    />
    <h1>{{ article.title }}</h1>
    <div class="meta">
      <time>
        <Calendar class="tabler-icon" />{{ formatDate(article.createTime) }}
      </time>
      <span v-if="article.categoryName" class="cat">
        <Bookmark class="tabler-icon" />{{ article.categoryName }}
      </span>
    </div>
    <MarkdownBody :html="html" />
    <CommentTree v-if="article.comment === 1" :article-id="article.id" />
    <el-alert v-else title="本文未开放评论" type="info" :closable="false" />
  </article>
</template>

<style scoped lang="scss">
h1 {
  margin: 0 0 0.75rem;
  font-size: calc(2.15rem + 2px);
  text-align: center;
  color: var(--c-text-1);
}
.cover {
  width: 100%;
  max-height: 20rem;
  object-fit: cover;
  border-radius: 0.9rem;
  margin-bottom: 1.25rem;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.85rem;
  align-items: center;
  justify-content: center;
  color: var(--c-text-2);
  margin-bottom: 1.5rem;
  font-size: calc(0.95rem + 2px);
}
time,
.cat {
  display: inline-flex;
  align-items: center;
  gap: 0.22rem;
}
</style>
