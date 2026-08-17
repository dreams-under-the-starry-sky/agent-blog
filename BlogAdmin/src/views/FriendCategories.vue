<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import { filterPage } from '@/utils/page'
import { tableTime } from '@/utils/format'

const all = ref<any[]>([])
const keyword = ref('')
const applied = ref('')
const page = ref(1)
const visible = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  sort: 99,
  description: '',
})
const view = computed(() => filterPage(all.value, applied.value, page.value, ['name', 'description']))

async function load() {
  all.value = await adminApi.friendCategories()
}
function applySearch() {
  applied.value = keyword.value
}
function open(row?: any) {
  form.id = row?.id
  form.name = row?.name || ''
  form.sort = row?.sort ?? 99
  form.description = row?.description || ''
  visible.value = true
}
async function save() {
  const name = form.name.trim()
  if (!name) {
    ElMessage.warning('请输入分类名')
    return
  }
  if (all.value.some((item) => item.name === name && item.id !== form.id)) {
    ElMessage.warning('分类名已存在')
    return
  }
  try {
    await adminApi.saveFriendCategory({ ...form, name })
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch {
    // 错误已由请求拦截器提示
  }
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
  await adminApi.deleteFriendCategory(id)
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
    search-label="分类名"
    search-placeholder="请输入分类名"
    @search="applySearch"
    @create="open()"
  >
    <el-table-column prop="name" label="分类名" />
    <el-table-column prop="description" label="介绍" />
    <el-table-column prop="sort" label="排序" />
    <el-table-column prop="createTime" label="创建时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" @click="open(row)">编辑</el-button>
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" title="友链分类" width="400px" align-center>
        <el-form label-width="80px">
          <el-form-item label="分类名"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="介绍"><el-input v-model="form.description" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sort" /></el-form-item>
        </el-form>
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </template>
  </CrudPage>
</template>
