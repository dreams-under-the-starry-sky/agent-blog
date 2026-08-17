<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MessageCircle from '@vicons/tabler/es/MessageCircle'
import type { Comment } from '@/api/types'
import { frontApi } from '@/api/front'
import CommentForm from '@/components/CommentForm.vue'
import CommentList from '@/components/CommentList.vue'
import { validateCommentInput } from '@/utils/comment'

const props = defineProps<{ articleId: number; comments: Comment[] }>()
const emit = defineEmits<{ refresh: [] }>()
const replyTo = ref<number | null>(null)
const form = reactive({ nickname: '', email: '', website: '', content: '' })
function countVisible(items?: Comment[]): number {
  return (items || []).reduce((sum, item) => {
    if (item.visible === 0) return sum
    return sum + 1 + countVisible(item.children)
  }, 0)
}
const total = computed(() => countVisible(props.comments))

async function submit(parentId?: number | null) {
  const error = validateCommentInput(form)
  if (error) {
    ElMessage.warning(error)
    return
  }
  await frontApi.submitComment({
    articleId: props.articleId,
    parentId: parentId ?? undefined,
    nickname: form.nickname,
    email: form.email,
    website: form.website,
    content: form.content,
  })
  ElMessage.success('评论已提交')
  form.content = ''
  replyTo.value = null
  emit('refresh')
}
</script>

<template>
  <div class="comments">
    <CommentForm v-model="form" @submit="submit()" />
    <h3 class="tk-title">
      <MessageCircle class="tabler-icon" /> 评论 {{ total }}
    </h3>
    <CommentList
      :items="comments"
      :reply-to="replyTo"
      :form="form"
      @reply="replyTo = $event"
      @cancel="replyTo = null"
      @submit="submit"
    />
  </div>
</template>

<style scoped lang="scss">
.comments { margin-top: 2rem; padding-top: 0.5rem; }
.tk-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin: 1.5rem 0 0.9rem;
  font-size: 1.05rem;
  color: var(--c-text-1);
}
</style>
