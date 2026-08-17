<script setup lang="ts">
import { computed, inject, onMounted, onUnmounted, ref, watch, type Ref } from 'vue'
import { useRoute } from 'vue-router'
import Bookmark from '@vicons/tabler/es/Bookmark'
import Calendar from '@vicons/tabler/es/Calendar'
import { frontApi } from '@/api/front'
import type { Article, Comment } from '@/api/types'
import CommentTree from '@/components/CommentTree.vue'
import MarkdownBody from '@/components/MarkdownBody.vue'
import { formatDate, mediaUrl } from '@/utils/format'
import { renderArticle, type Heading } from '@/utils/markdown'
import { usePageReady } from '@/utils/pageReady'

const route = useRoute()
const article = ref<Article | null>(null)
const comments = ref<Comment[]>([])
const rendered = computed(() => renderArticle(article.value?.content))
const html = computed(() => rendered.value.html)
const tocHeadings = inject<Ref<Heading[]>>('tocHeadings')
const beginReady = usePageReady()

async function load() {
  const pageReady = beginReady()
  try {
    const id = String(route.params.id)
    article.value = await frontApi.article(id)
    comments.value = await frontApi.comments(id)
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
    <img v-if="article.cover" class="cover" :src="mediaUrl(article.cover)" :alt="article.title" />
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
    <CommentTree
      v-if="article.comment === 1"
      :article-id="article.id"
      :comments="comments"
      @refresh="load"
    />
    <el-alert v-else title="本文未开放评论" type="info" :closable="false" />
  </article>
</template>

<style scoped lang="scss">
h1 {
  margin: 0 0 0.75rem;
  font-size: 2.15rem;
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
  font-size: 0.95rem;
}
time,
.cat {
  display: inline-flex;
  align-items: center;
  gap: 0.22rem;
}
</style>
