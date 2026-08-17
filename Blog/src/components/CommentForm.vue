<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { CONTENT_MAX, NICKNAME_MAX, validateCommentInput } from '@/utils/comment'

let form = defineModel<{
  nickname: string
  email: string
  website: string
  content: string
}>({ required: true })

withDefaults(defineProps<{
  placeholder?: string
  submitText?: string
  cancelable?: boolean
}>(), {
  placeholder: '说点什么...',
  submitText: '发送',
  cancelable: false,
})

const emit = defineEmits<{ submit: []; cancel: [] }>()

function onSubmit() {
  const error = validateCommentInput(form.value)
  if (error) {
    ElMessage.warning(error)
    return
  }
  emit('submit')
}
</script>

<template>
  <form class="tk-box" novalidate @submit.prevent="onSubmit">
    <textarea
      v-model="form.content"
      class="tk-textarea"
      :placeholder="placeholder"
      :maxlength="CONTENT_MAX"
      rows="5"
    />
    <div class="tk-meta">
      <label>
        <span>昵称</span>
        <input v-model="form.nickname" type="text" placeholder="必填" :maxlength="NICKNAME_MAX" />
      </label>
      <label>
        <span>邮箱</span>
        <input v-model="form.email" type="text" placeholder="选填" />
      </label>
      <label>
        <span>网站</span>
        <input v-model="form.website" type="text" placeholder="选填" />
      </label>
    </div>
    <div class="tk-actions">
      <button v-if="cancelable" class="tk-cancel" type="button" @click="emit('cancel')">取消</button>
      <button class="tk-send" type="submit">{{ submitText }}</button>
    </div>
  </form>
</template>

<style scoped lang="scss">
.tk-box {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.tk-textarea {
  width: 100%;
  min-height: 9.5rem;
  padding: 1rem 1.25rem;
  border: 1px solid var(--line-divider);
  border-radius: var(--radius-large);
  background: var(--card-bg);
  color: var(--c-text-1);
  resize: vertical;
  outline: none;
}
.tk-textarea:focus {
  border-color: var(--primary);
}
.tk-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.6rem;
}
.tk-meta label {
  display: flex;
  min-height: 2.5rem;
  border: 1px solid var(--line-divider);
  border-radius: 0.7rem;
  overflow: hidden;
  background: var(--btn-regular-bg);
  transition: border-color 0.15s ease;
}
.tk-meta label:focus-within {
  border-color: var(--primary);
}
.tk-meta span {
  flex: 0 0 3.2rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  color: var(--c-text-2);
}
.tk-meta input {
  flex: 1;
  min-width: 0;
  border: none;
  background: var(--card-bg);
  color: var(--c-text-1);
  padding: 0 0.75rem;
  outline: none;
}
.tk-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
.tk-send,
.tk-cancel {
  height: 2rem;
  padding: 0 0.9rem;
  cursor: pointer;
  font-size: 0.875rem;
  border-radius: 0.55rem;
}
.tk-send {
  border: none;
  background: var(--btn-regular-bg);
  color: var(--btn-content);
}
.tk-send:hover { background: var(--btn-plain-bg-hover); }
.tk-cancel {
  border: 1px solid var(--line-divider);
  background: transparent;
  color: var(--c-text-2);
}
.tk-cancel:hover {
  border-color: var(--btn-content);
  color: var(--btn-content);
}
@media (max-width: 700px) {
  .tk-meta { grid-template-columns: 1fr; }
}
</style>
