<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import { PAGE_SIZE } from '@/utils/page'
import { tableTime } from '@/utils/format'

const keyword = ref('')
const page = ref(1)
const data = ref({ total: 0, list: [] as any[] })

async function load() {
  data.value = await adminApi.emails({ page: page.value, size: PAGE_SIZE, keyword: keyword.value })
}
onMounted(load)
</script>

<template>
  <CrudPage
    v-model:keyword="keyword"
    v-model:page="page"
    :rows="data.list"
    :total="data.total"
    search-label="内容"
    search-placeholder="请输入发件人、收件人或内容"
    @search="load"
    @page-change="load"
    @create="ElMessage.info('邮件记录不支持新建')"
  >
    <el-table-column prop="sendName" label="发件人" />
    <el-table-column prop="receiveName" label="收件人" />
    <el-table-column prop="content" label="内容" />
    <el-table-column prop="createTime" label="时间" :formatter="tableTime" />
  </CrudPage>
</template>
