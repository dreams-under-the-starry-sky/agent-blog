<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import ReplyDetailDialog from '@/components/ReplyDetailDialog.vue'
import { PAGE_SIZE } from '@/utils/page'
import { tableTime } from '@/utils/format'

const page = ref(1)
const handle = ref<number | undefined>(0)
const deleted = ref<number | undefined>()
const notice = ref<number | undefined>()
const send = ref<number | undefined>()
const data = ref({ total: 0, list: [] as any[] })
const reply = ref('')
const current = ref<any>(null)
const showReply = ref(false)
const dialogMode = ref<'view' | 'review'>('view')

function query() {
  return {
    page: page.value,
    size: PAGE_SIZE,
    handle: handle.value,
    visible: deleted.value === undefined ? undefined : deleted.value === 1 ? 0 : 1,
    notice: notice.value,
    send: send.value,
  }
}

async function load() {
  data.value = await adminApi.messages(query())
}

function resetFilters() {
  handle.value = 0
  deleted.value = undefined
  notice.value = undefined
  send.value = undefined
}

function openDialog(row: any, mode: 'view' | 'review') {
  current.value = row
  reply.value = ''
  dialogMode.value = mode
  showReply.value = true
}

async function toggleVisible(row: any) {
  const hide = row.visible === 1
  await ElMessageBox.confirm(
    hide ? '确认隐藏该留言？前台将不再显示，数据仍会保留。' : '确认恢复显示该留言？',
    '提示',
    { type: 'warning' },
  )
  await adminApi.visibleMessage(row.id, hide ? 0 : 1)
  ElMessage.success(hide ? '已隐藏' : '已恢复显示')
  await load()
}

async function sendReply() {
  if (!current.value || !reply.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await ElMessageBox.confirm('确认回复该留言？回复后将自动标记为已处理。', '回复确认', {
    type: 'warning',
    confirmButtonText: '确认回复',
    cancelButtonText: '取消',
  })
  await adminApi.replyMessage({
    parentId: current.value.id,
    nickname: '博主',
    content: reply.value.trim(),
  })
  ElMessage.success('已回复')
  showReply.value = false
  current.value = null
  reply.value = ''
  await load()
}

async function review(approved: boolean) {
  if (!current.value) return
  await adminApi.reviewMessage(current.value.id, approved)
  ElMessage.success(approved ? '留言已通过' : '留言已设为不通过并删除')
  showReply.value = false
  current.value = null
  await load()
}

onMounted(load)
</script>

<template>
  <CrudPage
    v-model:page="page"
    :rows="data.list"
    :total="data.total"
    :show-create="false"
    :show-keyword="false"
    @search="load"
    @reset="resetFilters"
    @page-change="load"
  >
    <template #filters>
      <span class="filter-label">是否处理</span>
      <el-select v-model="handle" clearable placeholder="全部" class="filter-select">
        <el-option label="是" :value="1" />
        <el-option label="否" :value="0" />
      </el-select>
      <span class="filter-label">是否删除</span>
      <el-select v-model="deleted" clearable placeholder="全部" class="filter-select">
        <el-option label="是" :value="1" />
        <el-option label="否" :value="0" />
      </el-select>
      <span class="filter-label">发送邮件</span>
      <el-select v-model="notice" clearable placeholder="全部" class="filter-select">
        <el-option label="是" :value="1" />
        <el-option label="否" :value="0" />
      </el-select>
      <span class="filter-label">邮件是否发送</span>
      <el-select v-model="send" clearable placeholder="全部" class="filter-select">
        <el-option label="是" :value="1" />
        <el-option label="否" :value="0" />
      </el-select>
    </template>
    <el-table-column prop="nickname" label="昵称" />
    <el-table-column prop="parentNickname" label="回复" />
    <el-table-column prop="content" label="内容" />
    <el-table-column prop="createTime" label="时间" :formatter="tableTime" />
    <el-table-column label="处理">
      <template #default="{ row }">{{ row.handle === 1 ? '已处理' : '未处理' }}</template>
    </el-table-column>
    <el-table-column label="显示">
      <template #default="{ row }">{{ row.visible === 1 ? '是' : '否' }}</template>
    </el-table-column>
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button
          v-if="row.handle === 1 || row.visible === 0"
          link
          type="primary"
          @click="openDialog(row, 'view')"
        >
          查看
        </el-button>
        <el-button
          v-else
          link
          type="warning"
          @click="openDialog(row, 'review')"
        >
          处理
        </el-button>
        <el-button link :type="row.visible === 1 ? 'danger' : 'primary'" @click="toggleVisible(row)">
          {{ row.visible === 1 ? '删除' : '恢复' }}
        </el-button>
      </template>
    </el-table-column>
    <template #extra>
      <ReplyDetailDialog
        v-model="showReply"
        v-model:reply="reply"
        :row="current"
        kind="message"
        :mode="dialogMode"
        @submit="sendReply"
        @approve="review(true)"
        @reject="review(false)"
      />
    </template>
  </CrudPage>
</template>

<style scoped>
.filter-label {
  color: var(--el-text-color-regular);
  font-size: 14px;
}
.filter-select {
  width: 220px;
}
</style>
