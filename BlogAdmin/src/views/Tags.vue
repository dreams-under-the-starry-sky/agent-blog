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
const form = reactive({ id: undefined as number | undefined, name: '' })
const view = computed(() => filterPage(all.value, applied.value, page.value, ['name']))

async function load() {
  all.value = await adminApi.tags()
}
function applySearch() {
  applied.value = keyword.value
}
function open(row?: any) {
  form.id = row?.id
  form.name = row?.name || ''
  visible.value = true
}
async function save() {
  const name = form.name.trim()
  if (!name) {
    ElMessage.warning('请输入标签名')
    return
  }
  if (all.value.some((item) => item.name === name && item.id !== form.id)) {
    ElMessage.warning('标签名已存在')
    return
  }
  try {
    await adminApi.saveTag({ ...form, name })
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch {
    // 错误已由请求拦截器提示
  }
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该标签？', '提示', { type: 'warning' })
  try {
    await adminApi.deleteTag(id)
    ElMessage.success('已删除')
    await load()
  } catch {
    // 错误已由请求拦截器提示
  }
}
onMounted(load)
</script>

<template>
  <CrudPage
    v-model:keyword="keyword"
    v-model:page="page"
    :rows="view.rows"
    :total="view.total"
    search-label="标签名"
    search-placeholder="请输入标签名"
    @search="applySearch"
    @create="open()"
  >
    <el-table-column prop="name" label="标签名" />
    <el-table-column prop="createTime" label="创建时间" :formatter="tableTime" />
    <el-table-column prop="updateTime" label="修改时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" @click="open(row)">编辑</el-button>
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" title="标签" width="400px" align-center>
        <el-input v-model="form.name" placeholder="标签名" />
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </template>
  </CrudPage>
</template>
