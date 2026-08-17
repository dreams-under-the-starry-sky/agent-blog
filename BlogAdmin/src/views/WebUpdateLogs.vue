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
const form = reactive({ id: undefined as number | undefined, title: '', description: '' })
const view = computed(() => filterPage(all.value, applied.value, page.value, ['title', 'description']))

async function load() {
  all.value = await adminApi.webUpdateLogs()
}
function applySearch() {
  applied.value = keyword.value
}
function open(row?: any) {
  form.id = row?.id
  form.title = row?.title || ''
  form.description = row?.description || ''
  visible.value = true
}
async function save() {
  const title = form.title.trim()
  if (!title) {
    ElMessage.warning('请输入标题')
    return
  }
  try {
    await adminApi.saveWebUpdateLog({ ...form, title, description: form.description.trim() })
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch {
    // 错误已由请求拦截器提示
  }
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该功能日志？', '提示', { type: 'warning' })
  try {
    await adminApi.deleteWebUpdateLog(id)
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
    search-label="标题"
    search-placeholder="请输入标题或介绍"
    @search="applySearch"
    @create="open()"
  >
    <el-table-column prop="title" label="标题" />
    <el-table-column prop="description" label="更新介绍" show-overflow-tooltip />
    <el-table-column prop="createTime" label="创建时间" :formatter="tableTime" />
    <el-table-column prop="updateTime" label="修改时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" @click="open(row)">编辑</el-button>
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" title="功能日志" width="520px" align-center>
        <el-form label-width="80px">
          <el-form-item label="标题"><el-input v-model="form.title" maxlength="80" /></el-form-item>
          <el-form-item label="介绍">
            <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </template>
  </CrudPage>
</template>
