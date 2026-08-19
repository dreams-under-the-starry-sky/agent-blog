<script setup lang="ts">
import { computed } from 'vue'
import { formatTime } from '@/utils/format'

const open = defineModel<boolean>({ default: false })
const reply = defineModel<string>('reply', { default: '' })

const props = defineProps<{
  row: any | null
  kind: 'comment' | 'message'
}>()

const emit = defineEmits<{ submit: [] }>()

const hidden = computed(() => props.row?.visible === 0)

const title = computed(() => {
  const name = props.kind === 'comment' ? '评论' : '留言'
  if (hidden.value) return `查看${name}`
  return props.row?.handle === 1 ? `查看${name}` : `回复${name}`
})

function yesNo(value?: number) {
  return value === 1 ? '是' : '否'
}

function region(row: any) {
  return [row.province, row.city, row.district].filter(Boolean).join(' ') || '—'
}
</script>

<template>
  <el-dialog v-model="open" :title="title" width="680px" align-center destroy-on-close>
    <el-descriptions v-if="row" :column="2" border>
      <el-descriptions-item label="昵称">{{ row.nickname || '—' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ row.email || '—' }}</el-descriptions-item>
      <el-descriptions-item v-if="kind === 'comment'" label="文章">{{ row.articleTitle || '—' }}</el-descriptions-item>
      <el-descriptions-item label="回复对象" :span="kind === 'comment' ? 1 : 2">{{ row.parentNickname || '—' }}</el-descriptions-item>
      <el-descriptions-item label="时间" :span="2">{{ formatTime(row.createTime) || '—' }}</el-descriptions-item>
      <el-descriptions-item label="IP">{{ row.ip || '—' }}</el-descriptions-item>
      <el-descriptions-item label="地区">{{ region(row) }}</el-descriptions-item>
      <el-descriptions-item label="浏览器">{{ row.browser || '—' }}</el-descriptions-item>
      <el-descriptions-item label="系统">{{ row.systemInfo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="是否处理">{{ yesNo(row.handle) }}</el-descriptions-item>
      <el-descriptions-item label="是否删除">{{ row.visible === 0 ? '是' : '否' }}</el-descriptions-item>
      <el-descriptions-item label="发送邮件">{{ yesNo(row.notice) }}</el-descriptions-item>
      <el-descriptions-item label="邮件是否发送">{{ yesNo(row.send) }}</el-descriptions-item>
      <el-descriptions-item label="内容" :span="2">
        <span class="content">{{ row.content || '—' }}</span>
      </el-descriptions-item>
    </el-descriptions>
    <div v-if="!hidden" class="reply-box">
      <p class="label">回复内容</p>
      <el-input v-model="reply" type="textarea" :rows="4" placeholder="请输入回复内容" />
    </div>
    <template #footer>
      <el-button @click="open = false">{{ hidden ? '关闭' : '取消' }}</el-button>
      <el-button v-if="!hidden" type="primary" @click="emit('submit')">发送</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.reply-box {
  margin-top: 16px;
}
.label {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}
:deep(.el-descriptions__table) {
  table-layout: fixed;
}
:deep(.el-descriptions__label),
:deep(.el-descriptions__cell.is-bordered-label) {
  width: 120px;
  min-width: 120px;
}
.content {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
