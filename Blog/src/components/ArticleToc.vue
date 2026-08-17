<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import type { Heading } from '@/utils/markdown'

const props = defineProps<{ headings: Heading[] }>()
const activeId = ref('')

function onScroll() {
  const items = props.headings
    .map((item) => document.getElementById(item.id))
    .filter((el): el is HTMLElement => !!el)
  if (!items.length) {
    activeId.value = ''
    return
  }
  const threshold = 120
  let current = items[0].id
  for (const el of items) {
    if (el.getBoundingClientRect().top <= threshold) current = el.id
  }
  activeId.value = current
}

watch(() => props.headings, onScroll, { deep: true })

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <section class="card-base toc">
    <h3>目录</h3>
    <nav v-if="headings.length">
      <a
        v-for="item in headings"
        :key="item.id"
        :href="`#${item.id}`"
        class="link"
        :class="{ on: activeId === item.id, [`lv-${item.level}`]: true }"
      >
        {{ item.text }}
      </a>
    </nav>
    <p v-else class="empty">暂无目录</p>
  </section>
</template>

<style scoped lang="scss">
.toc {
  padding: 0.9rem;
}
h3 {
  margin: 0 0 0.75rem;
  font-size: 0.9rem;
  color: var(--c-text-1);
}
nav {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}
.link {
  display: block;
  padding: 0.4rem 0.5rem;
  border-radius: 0.5rem;
  color: var(--c-text-2);
  font-size: 0.85rem;
  line-height: 1.45;
}
.link:hover,
.link.on {
  background: var(--btn-plain-bg-hover);
  color: var(--btn-content);
}
.lv-3 { padding-left: 1rem; }
.lv-4 { padding-left: 1.6rem; }
.empty {
  margin: 0;
  color: var(--c-text-3);
  font-size: 0.85rem;
}
</style>
