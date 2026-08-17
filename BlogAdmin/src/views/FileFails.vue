<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import { filterPage } from '@/utils/page'
import { tableTime } from '@/utils/format'

const all = ref<any[]>([])
const keyword = ref('')
const applied = ref('')
const page = ref(1)
const view = computed(() => filterPage(all.value, applied.value, page.value, ['fileKey', 'extra']))

async function load() {
  all.value = await adminApi.fileFails()
}
function applySearch() {
  applied.value = keyword.value
}
async function remove(id: number) {
  await adminApi.deleteFileFail(id)
  await load()
}
onMounted(load)
</script>

<template>
  <CrudPage
    v-model:keyword="keyword"
    v-model:page="page"
    :rows="view.rows"
    :total="view.total"
    search-label="文件"
    search-placeholder="请输入文件或原因"
    @search="applySearch"
    @create="ElMessage.info('删除失败记录不支持新建')"
  >
    <el-table-column prop="fileKey" label="文件" />
    <el-table-column prop="extra" label="原因" />
    <el-table-column prop="createTime" label="时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
  </CrudPage>
</template>
