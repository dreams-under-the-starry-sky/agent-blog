<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import MessageCircle from '@vicons/tabler/es/MessageCircle'
import { frontApi } from '@/api/front'
import type { Message } from '@/api/types'
import CommentForm from '@/components/CommentForm.vue'
import CommentList from '@/components/CommentList.vue'
import FramePager from '@/components/FramePager.vue'
import { clearCommentForm, validateCommentInput } from '@/utils/comment'
import { useLazyVisible } from '@/utils/lazyVisible'

const PAGE_SIZE = 10
const props = defineProps<{ pageId: number }>()

const { target, visible } = useLazyVisible()
const page = ref(1)
const rootTotal = ref(0)
const list = ref<Message[]>([])
const loaded = ref(false)
const loading = ref(false)
const paging = ref<'prev' | 'next' | null>(null)
const replyTo = ref<number | null>(null)
const form = reactive({ nickname: '', email: '', website: '', content: '' })

function countVisible(items?: Message[]): number {
  return (items || []).reduce((sum, item) => {
    if (item.visible === 0) return sum
    return sum + 1 + countVisible(item.children)
  }, 0)
}

const total = computed(() => countVisible(list.value))
const hasPrev = computed(() => page.value > 1)
const hasNext = computed(() => page.value * PAGE_SIZE < rootTotal.value)

async function fetchPage() {
  const data = await frontApi.messages(props.pageId, { page: page.value, size: PAGE_SIZE })
  list.value = data.list
  rootTotal.value = data.total
  loaded.value = true
}

async function load() {
  if (loading.value) return
  loading.value = true
  try {
    await fetchPage()
  } finally {
    loading.value = false
  }
}

async function go(nextPage: number, dir: 'prev' | 'next') {
  if (paging.value || loading.value) return
  paging.value = dir
  try {
    const data = await frontApi.messages(props.pageId, { page: nextPage, size: PAGE_SIZE })
    page.value = nextPage
    list.value = data.list
    rootTotal.value = data.total
  } finally {
    paging.value = null
  }
}

async function submit(parentId?: number | null) {
  const error = validateCommentInput(form)
  if (error) {
    ElMessage.warning(error)
    return
  }
  await frontApi.submitMessage({
    pageId: props.pageId,
    parentId: parentId ?? undefined,
    nickname: form.nickname,
    email: form.email,
    website: form.website,
    content: form.content,
  })
  ElMessage.success('留言成功')
  clearCommentForm(form)
  replyTo.value = null
  await load()
}

watch(
  visible,
  (value) => {
    if (value) load()
  },
  { immediate: true },
)

watch(
  () => props.pageId,
  () => {
    list.value = []
    loaded.value = false
    page.value = 1
    rootTotal.value = 0
    if (visible.value) load()
  },
)
</script>

<template>
  <div ref="target" class="page-comments">
    <template v-if="loaded">
      <CommentForm v-model="form" placeholder="留下足迹" @submit="submit()" />
      <h3 class="tk-title">
        <MessageCircle class="tabler-icon" /> 评论 {{ total }}
      </h3>
      <CommentList
        :items="list"
        :reply-to="replyTo"
        :form="form"
        @reply="replyTo = $event"
        @cancel="replyTo = null"
        @submit="submit"
      />
      <FramePager
        :has-prev="hasPrev"
        :has-next="hasNext"
        :paging="paging"
        @prev="go(page - 1, 'prev')"
        @next="go(page + 1, 'next')"
      />
    </template>
    <div v-else class="lazy-placeholder">
      {{ loading || visible ? '评论加载中…' : '向下滚动加载评论' }}
    </div>
  </div>
</template>

<style scoped lang="scss">
.page-comments {
  margin-top: 1.5rem;
}
.tk-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin: 1.5rem 0 0.9rem;
  font-size: calc(1.05rem + 2px);
  color: var(--c-text-1);
}
.lazy-placeholder {
  min-height: 8rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c-text-3);
  font-size: calc(0.9rem + 2px);
}
</style>
