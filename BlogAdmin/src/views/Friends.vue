<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import { filterPage } from '@/utils/page'

const all = ref<any[]>([])
const cats = ref<any[]>([])
const keyword = ref('')
const applied = ref('')
const categoryId = ref<number | undefined>()
const appliedCategory = ref<number | undefined>()
const page = ref(1)
const visible = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  name: '',
  description: '',
  href: '',
  logo: '',
  sort: 99,
})
const view = computed(() => {
  const source = appliedCategory.value == null
    ? all.value
    : all.value.filter((item) => item.categoryId === appliedCategory.value)
  return filterPage(source, applied.value, page.value, ['name', 'href', 'description', 'categoryName'])
})

async function load() {
  all.value = await adminApi.friends()
  cats.value = await adminApi.friendCategories()
}
function applySearch() {
  applied.value = keyword.value
  appliedCategory.value = categoryId.value
}
function resetFilters() {
  categoryId.value = undefined
}
function categorySort(id?: number) {
  return cats.value.find((item) => item.id === id)?.sort ?? 99
}

watch(() => form.categoryId, (id) => {
  form.sort = categorySort(id)
})

function open(row?: any) {
  Object.assign(form, {
    id: row?.id,
    categoryId: row?.categoryId,
    name: row?.name || '',
    description: row?.description || '',
    href: row?.href || '',
    logo: row?.logo || '',
    sort: categorySort(row?.categoryId),
  })
  visible.value = true
}
async function save() {
  await adminApi.saveFriend({ ...form })
  ElMessage.success('已保存')
  visible.value = false
  await load()
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该友链？', '提示', { type: 'warning' })
  await adminApi.deleteFriend(id)
  ElMessage.success('已删除')
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
    search-label="名称"
    search-placeholder="请输入友链名称"
    @search="applySearch"
    @reset="resetFilters"
    @create="open()"
  >
    <template #filters>
      <span class="filter-label">分类</span>
      <el-select v-model="categoryId" clearable placeholder="全部" class="filter-select">
        <el-option v-for="item in cats" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </template>
    <el-table-column prop="name" label="名称" />
    <el-table-column prop="categoryName" label="分类" />
    <el-table-column prop="href" label="链接" />
    <el-table-column prop="sort" label="排序" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" @click="open(row)">编辑</el-button>
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" title="友链" width="480px" align-center>
        <el-form label-width="80px">
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" clearable placeholder="选择分类" style="width: 100%">
              <el-option v-for="item in cats" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="简介"><el-input v-model="form.description" /></el-form-item>
          <el-form-item label="链接"><el-input v-model="form.href" /></el-form-item>
          <el-form-item label="Logo"><el-input v-model="form.logo" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sort" disabled /></el-form-item>
        </el-form>
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
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
