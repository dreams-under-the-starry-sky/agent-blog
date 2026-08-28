<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MessageReport from '@vicons/tabler/es/MessageReport'
import MessagePlus from '@vicons/tabler/es/MessagePlus'
import Copy from '@vicons/tabler/es/Copy'
import Check from '@vicons/tabler/es/Check'
import { frontApi } from '@/api/front'
import type { Friend } from '@/api/types'
import { PAGE_IDS, siteConfig } from '@/config'
import { mediaUrl } from '@/utils/format'
import { usePageReady } from '@/utils/pageReady'
import PageComments from '@/components/PageComments.vue'

const list = ref<Friend[]>([])
const loaded = ref(false)
const copied = ref(false)
const beginReady = usePageReady()
let copyTimer = 0

const siteInfo = [
  `名称：${siteConfig.title}`,
  '地址：https://crossroads.net.cn',
  '头像：https://myqc.net.cn/blog/head-picture.jpg',
  `描述：${siteConfig.bio}`,
].join('\n')

async function copySiteInfo() {
  try {
    await navigator.clipboard.writeText(siteInfo)
    copied.value = true
    ElMessage.success('已复制')
    window.clearTimeout(copyTimer)
    copyTimer = window.setTimeout(() => {
      copied.value = false
    }, 1200)
  } catch {
    ElMessage.error('复制失败')
  }
}

onMounted(async () => {
  const pageReady = beginReady()
  try {
    list.value = await frontApi.friends()
  } finally {
    loaded.value = true
    pageReady()
  }
})

onUnmounted(() => {
  window.clearTimeout(copyTimer)
})
</script>

<template>
  <div>
    <h1 class="page-title">友链</h1>

    <section class="apply">
      <h2>
        <MessageReport class="tabler-icon" />
        申请要求
      </h2>
      <ul>
        <li>不含 <mark>暴力</mark>、<mark>广告</mark>、<mark>违法</mark> 等不良信息</li>
        <li>建站超过 <mark class="num">1</mark> 个月 / 文章数超 <mark class="num">10</mark> 篇</li>
        <li>不定期清理无法访问等链接</li>
        <li>遵循互加交友，好友排名不分先后顺序，留言参考下面格式即可</li>
      </ul>
      <h2>
        <MessagePlus class="tabler-icon" />
        申请格式
      </h2>
      <div class="site-code-block">
        <button
          class="copy-btn"
          type="button"
          title="复制"
          :class="{ copied }"
          @click="copySiteInfo"
        >
          <Check v-if="copied" class="tabler-icon" />
          <Copy v-else class="tabler-icon" />
        </button>
        <pre class="site-code"><code>{{ siteInfo }}</code></pre>
      </div>
    </section>

    <div class="grid">
      <a v-for="item in list" :key="item.id" :href="item.href" target="_blank" rel="noreferrer" class="card card-base">
        <img v-if="item.logo" :src="mediaUrl(item.logo)" alt="" />
        <div>
          <strong>{{ item.name }}</strong>
          <p>{{ item.description }}</p>
        </div>
      </a>
    </div>
    <el-empty v-if="loaded && !list.length" />
    <PageComments :page-id="PAGE_IDS.friends" />
  </div>
</template>

<style scoped lang="scss">
.apply {
  margin: 0 0 1.75rem;
}

h2 {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin: 1.1rem 0 0.65rem;
  font-size: calc(1.35rem + 2px);
  color: var(--btn-content);
}

ul {
  margin: 0;
  padding-left: 1.6rem;
  line-height: 1.85;
  color: var(--c-text-1);
}

li {
  list-style: disc;
}

mark {
  background: transparent;
  color: var(--primary);
  font-weight: 700;
}

.site-code-block {
  position: relative;
}

.site-code-block:hover .copy-btn,
.copy-btn.copied {
  opacity: 1;
  pointer-events: auto;
}

.copy-btn {
  position: absolute;
  top: 0.55rem;
  right: 0.55rem;
  z-index: 1;
  width: 2rem;
  height: 2rem;
  padding: 0;
  border: none;
  border-radius: 0.45rem;
  background: var(--card-bg);
  color: var(--c-text-1);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease, color 0.15s ease;
}

.copy-btn:hover,
.copy-btn.copied {
  color: var(--btn-content);
}

.site-code {
  margin: 0;
  padding: 14px 3rem 14px 16px;
  overflow: auto;
  background: var(--btn-regular-bg);
  border-radius: 12px;
  color: var(--c-text-1);
  font-family: inherit;
  font-size: inherit;
  line-height: 1.8;
}

.site-code code {
  font-family: inherit;
  font-size: inherit;
  white-space: pre-wrap;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(15rem, 1fr));
  gap: 0.75rem;
}

.card {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid transparent;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: var(--primary);
  }

  img {
    flex-shrink: 0;
    width: 3rem;
    height: 3rem;
    border-radius: 0.5rem;
    object-fit: cover;
  }

  > div {
    min-width: 0;
    flex: 1;
  }

  strong {
    color: var(--c-text-1);
  }

  p {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-word;
    margin: 0.35rem 0 0;
    color: var(--c-text-2);
    font-size: calc(0.85rem + 2px);
    line-height: 1.45;
  }
}
</style>
