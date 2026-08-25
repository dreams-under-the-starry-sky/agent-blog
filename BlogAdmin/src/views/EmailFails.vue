<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import { PAGE_SIZE } from '@/utils/page'
import { tableTime } from '@/utils/format'

const keyword = ref('')
const page = ref(1)
const data = ref({ total: 0, list: [] as any[] })
const sending = ref<number | null>(null)

async function load() {
  data.value = await adminApi.emailFails({ page: page.value, size: PAGE_SIZE, keyword: keyword.value })
}

async function resend(row: any) {
  await ElMessageBox.confirm('确认重新发送该邮件？', '重发确认', {
    type: 'warning',
    confirmButtonText: '确认重发',
    cancelButtonText: '取消',
  })
  sending.value = row.id
  try {
    await adminApi.resendEmailFail(row.id)
    ElMessage.success('邮件已重新发送')
    await load()
  } finally {
    sending.value = null
  }
}

function kindLabel(kind: string) {
  return kind === 'comment' ? '评论' : '留言'
}

onMounted(load)
</script>

<template>
  <CrudPage
    v-model:keyword="keyword"
    v-model:page="page"
    :rows="data.list"
    :total="data.total"
    :show-create="false"
    search-label="内容"
    search-placeholder="请输入发件人、收件人或失败原因"
    @search="load"
    @page-change="load"
  >
    <el-table-column label="类型">
      <template #default="{ row }">{{ kindLabel(row.kind) }}</template>
    </el-table-column>
    <el-table-column prop="sendName" label="发件人" />
    <el-table-column prop="receiveName" label="收件人" />
    <el-table-column prop="receiveEmail" label="收件邮箱" />
    <el-table-column prop="content" label="内容" />
    <el-table-column prop="extra" label="失败原因" />
    <el-table-column prop="createTime" label="时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" :loading="sending === row.id" @click="resend(row)">重发</el-button>
      </template>
    </el-table-column>
  </CrudPage>
</template>
