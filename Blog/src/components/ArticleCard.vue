<script setup lang="ts">
import Calendar from '@vicons/tabler/es/Calendar'
import Bookmark from '@vicons/tabler/es/Bookmark'
import type { Article } from '@/api/types'
import { formatDate, mediaUrl } from '@/utils/format'

defineProps<{ article: Article }>()
</script>

<template>
  <RouterLink :to="`/article/${article.id}`" class="card card-base">
    <div class="body">
      <h2>{{ article.title }}</h2>
      <div class="meta">
        <time>
          <Calendar class="tabler-icon" />{{ formatDate(article.createTime) }}
        </time>
        <span v-if="article.categoryName" class="cat">
          <Bookmark class="tabler-icon" />{{ article.categoryName }}
        </span>
      </div>
      <p class="excerpt">{{ article.description }}</p>
    </div>
    <div v-if="article.cover || article.thumbnail" class="cover">
      <img :src="mediaUrl(article.thumbnail || article.cover)" :alt="article.title" />
    </div>
  </RouterLink>
</template>

<style scoped lang="scss">
.card {
  display: flex;
  overflow: hidden;
  min-height: 11rem;
  transition: background-color 0.2s ease;
}
.card:hover { background: var(--btn-plain-bg-hover); }
.body {
  flex: 1;
  min-width: 0;
  padding: 1.35rem 1.5rem;
  display: flex;
  flex-direction: column;
}
h2 {
  margin: 0 0 0.6rem;
  font-size: 1.35rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--c-text-1);
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  color: var(--c-text-2);
  font-size: 0.85rem;
}
time,
.cat {
  display: inline-flex;
  align-items: center;
  gap: 0.22rem;
}
.excerpt {
  margin: 0.7rem 0 0;
  color: var(--c-text-2);
  font-size: 0.9rem;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}
.cover {
  position: relative;
  flex: 0 0 28%;
  width: 28%;
  min-width: 10.5rem;
  max-width: 14.5rem;
  margin: 0.75rem 0.75rem 0.75rem 0.45rem;
  padding: 0;
  box-sizing: border-box;
  border-radius: 0.7rem;
  overflow: hidden;
  align-self: stretch;
  min-height: 0;
}
.cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}
.card:hover .cover img { transform: scale(1.06); }

@media (max-width: 700px) {
  .card { flex-direction: column-reverse; }
  .cover {
    flex: none;
    width: auto;
    min-width: 0;
    max-width: none;
    height: 10rem;
    margin: 0.75rem 0.75rem 0;
  }
}
</style>
