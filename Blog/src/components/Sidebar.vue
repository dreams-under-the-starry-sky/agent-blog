<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import BrandGithub from '@vicons/tabler/es/BrandGithub'
import MusicIcon from '@vicons/tabler/es/Music'
import { frontApi } from '@/api/front'
import { siteConfig } from '@/config'
import { formatDate } from '@/utils/format'
import avatarImg from '@/assets/avatar.jpg'
import MusicPlayer from '@/components/MusicPlayer.vue'

const articleTotal = ref(0)
const lastUpdate = ref('')

const runDays = computed(() => {
  const start = new Date(`${siteConfig.startDate}T00:00:00`).getTime()
  return Math.max(0, Math.floor((Date.now() - start) / 86400000))
})

onMounted(async () => {
  try {
    const page = await frontApi.articles({ page: 1, size: 1 })
    articleTotal.value = page.total
    lastUpdate.value = formatDate(page.list[0]?.createTime)
  } catch {
    /* keep empty widgets if the API is down */
  }
})
</script>

<template>
  <div class="stack">
    <section class="card-base block profile">
      <img class="avatar" :src="avatarImg" alt="" />
      <h2>{{ siteConfig.author }}</h2>
      <p class="bio">{{ siteConfig.bio }}</p>
      <a class="github" :href="siteConfig.github" target="_blank" rel="noreferrer">
        <BrandGithub class="tabler-icon" /> GitHub
      </a>
    </section>

    <section class="card-base block music">
      <h3><MusicIcon class="tabler-icon" /> 音乐</h3>
      <MusicPlayer />
    </section>

    <section class="card-base block">
      <h3>站点状态</h3>
      <div class="stat"><span>文章数目</span><b>{{ articleTotal }}</b></div>
      <div class="stat"><span>运行天数</span><b>{{ runDays }} 天</b></div>
      <div class="stat"><span>最后更新</span><b>{{ lastUpdate || '—' }}</b></div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.stack { display: flex; flex-direction: column; gap: 1rem; }
.block { padding: 0.9rem; }
.profile { text-align: center; }
.avatar {
  width: 5.5rem;
  height: 5.5rem;
  border-radius: 50%;
  object-fit: cover;
  margin: 0.25rem auto 0.75rem;
  display: block;
}
h2 { margin: 0; font-size: 1.2rem; color: var(--c-text-1); }
.bio { margin: 0.5rem 0 0.75rem; color: var(--c-text-2); font-size: 0.85rem; }
.github {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.8rem;
  border-radius: 999px;
  background: var(--btn-regular-bg);
  color: var(--btn-content);
  font-size: 0.85rem;
  transition: background 0.18s ease, color 0.18s ease;
}

.github:hover {
  background: var(--btn-content);
  color: #fff;
}
h3 {
  margin: 0 0 0.75rem;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--c-text-1);
}
.music { overflow: visible; }
.music h3 {
  font-size: 1.05rem;
  :deep(.tabler-icon) { width: 1.3rem; height: 1.3rem; }
}
.stat {
  display: flex;
  justify-content: space-between;
  padding: 0.4rem 0;
  font-size: 0.9rem;
  color: var(--c-text-2);
}
</style>
