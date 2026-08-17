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
  data.value = await adminApi.logs({ page: page.value, size: PAGE_SIZE, keyword: keyword.value })
}
onMounted(load)
</script>

<template>
  <CrudPage
    v-model:keyword="keyword"
    v-model:page="page"
    :rows="data.list"
    :total="data.total"
    search-label="事件"
    search-placeholder="请输入事件或详情"
    @search="load"
    @page-change="load"
    @create="ElMessage.info('运行日志不支持新建')"
  >
    <el-table-column prop="event" label="事件" />
    <el-table-column prop="status" label="状态" />
    <el-table-column prop="detail" label="详情" />
    <el-table-column prop="createTime" label="时间" :formatter="tableTime" />
  </CrudPage>
</template>
