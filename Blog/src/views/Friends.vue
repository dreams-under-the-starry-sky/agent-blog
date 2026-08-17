<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { frontApi } from '@/api/front'
import type { Friend, FriendCategory } from '@/api/types'
import { mediaUrl } from '@/utils/format'
import { usePageReady } from '@/utils/pageReady'

const list = ref<Friend[]>([])
const cats = ref<FriendCategory[]>([])
const loaded = ref(false)
const beginReady = usePageReady()

const groups = computed(() => {
  const byId = new Map<number, Friend[]>()
  const uncategorized: Friend[] = []
  for (const item of list.value) {
    if (item.categoryId) {
      const bucket = byId.get(item.categoryId) || []
      bucket.push(item)
      byId.set(item.categoryId, bucket)
    } else {
      uncategorized.push(item)
    }
  }
  const result: Array<FriendCategory & { friends: Friend[] }> = cats.value
    .map((cat) => ({ ...cat, friends: byId.get(cat.id) || [] }))
    .filter((group) => group.friends.length)
  if (uncategorized.length) result.push({ id: 0, name: '未分类', friends: uncategorized })
  return result
})

onMounted(async () => {
  const pageReady = beginReady()
  try {
    const [friends, categories] = await Promise.all([frontApi.friends(), frontApi.friendCategories()])
    list.value = friends
    cats.value = categories
  } finally {
    loaded.value = true
    pageReady()
  }
})
</script>

<template>
  <div>
    <h1 class="page-title">友链</h1>
    <section v-for="group in groups" :key="group.id" class="group">
      <h2>{{ group.name }}</h2>
      <p v-if="group.description" class="desc">{{ group.description }}</p>
      <div class="grid">
        <a v-for="item in group.friends" :key="item.id" :href="item.href" target="_blank" class="card card-base">
          <img v-if="item.logo" :src="mediaUrl(item.logo)" alt="" />
          <div>
            <strong>{{ item.name }}</strong>
            <p>{{ item.description }}</p>
          </div>
        </a>
      </div>
    </section>
    <el-empty v-if="loaded && !list.length" />
  </div>
</template>

<style scoped lang="scss">
.group { margin-bottom: 1.75rem; }
h2 { margin: 0 0 0.5rem; font-size: 1.1rem; color: var(--btn-content); }
.desc { margin: 0 0 0.75rem; color: var(--c-text-2); font-size: 0.85rem; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(15rem, 1fr)); gap: 0.75rem; }
.card {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  img { width: 3rem; height: 3rem; border-radius: 0.5rem; }
  strong { color: var(--c-text-1); }
  p { margin: 0.35rem 0 0; color: var(--c-text-2); font-size: 0.85rem; }
}
</style>
