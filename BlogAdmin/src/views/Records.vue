<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import ImageTiles from '@/components/ImageTiles.vue'
import { PAGE_SIZE } from '@/utils/page'
import { tableTime } from '@/utils/format'

const keyword = ref('')
const page = ref(1)
const data = ref({ total: 0, list: [] as any[] })
const cats = ref<any[]>([])
const visible = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  happenTime: undefined as number | undefined,
  content: '',
  status: 1,
  images: [] as { imgUrl: string; thumbnailUrl?: string }[],
})

async function load() {
  data.value = await adminApi.records({ page: page.value, size: PAGE_SIZE, keyword: keyword.value })
  cats.value = await adminApi.recordCategories()
}
function open(row?: any) {
  form.id = row?.id
  form.categoryId = row?.categoryId
  form.happenTime = row?.happenTime
  form.content = row?.content || ''
  form.status = row?.status ?? 1
  form.images = (row?.images || []).map((i: any) => ({
    imgUrl: i.imgUrl,
    thumbnailUrl: i.thumbnailUrl || i.imgUrl,
  }))
  visible.value = true
}
async function save() {
  await adminApi.saveRecord({ ...form })
  ElMessage.success('已保存')
  visible.value = false
  await load()
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
  await adminApi.deleteRecord(id)
  ElMessage.success('已删除')
  await load()
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
    search-placeholder="请输入记录内容"
    @search="load"
    @page-change="load"
    @create="open()"
  >
    <el-table-column prop="categoryName" label="分类" />
    <el-table-column prop="content" label="内容" />
    <el-table-column prop="happenTime" label="发生时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" @click="open(row)">编辑</el-button>
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" title="记录" width="640px" align-center>
        <el-select v-model="form.categoryId" placeholder="分类">
          <el-option v-for="item in cats" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input v-model.number="form.happenTime" placeholder="发生时间，如 20260815" class="mt" />
        <el-input v-model="form.content" type="textarea" :rows="4" class="mt" />
        <div class="mt"><ImageTiles v-model="form.images" /></div>
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </template>
  </CrudPage>
</template>
<style scoped>.mt { margin-top: 12px; }</style>
