<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MessageCircle from '@vicons/tabler/es/MessageCircle'
import { frontApi } from '@/api/front'
import type { Message } from '@/api/types'
import CommentForm from '@/components/CommentForm.vue'
import CommentList from '@/components/CommentList.vue'
import { validateCommentInput } from '@/utils/comment'
import { usePageReady } from '@/utils/pageReady'

const list = ref<Message[]>([])
const replyTo = ref<number | null>(null)
const form = reactive({ nickname: '', email: '', website: '', content: '' })
function countVisible(items?: Message[]): number {
  return (items || []).reduce((sum, item) => {
    if (item.visible === 0) return sum
    return sum + 1 + countVisible(item.children)
  }, 0)
}
const total = computed(() => countVisible(list.value))
const beginReady = usePageReady()

async function load() {
  const pageReady = beginReady()
  try {
    list.value = await frontApi.messages()
  } finally {
    pageReady()
  }
}

async function submit(parentId?: number | null) {
  const error = validateCommentInput(form)
  if (error) {
    ElMessage.warning(error)
    return
  }
  await frontApi.submitMessage({
    parentId: parentId ?? undefined,
    nickname: form.nickname,
    email: form.email,
    website: form.website,
    content: form.content,
  })
  ElMessage.success('留言成功')
  form.content = ''
  replyTo.value = null
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h1 class="page-title">留言板</h1>
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
  </div>
</template>

<style scoped lang="scss">
.tk-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin: 1.5rem 0 0.9rem;
  font-size: 1.05rem;
  color: var(--c-text-1);
}
</style>
