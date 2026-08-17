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
  author: '',
  url: '',
  cover: '',
  lrc: '',
})
const view = computed(() => filterPage(all.value, applied.value, page.value, ['name', 'author', 'url']))

async function load() {
  all.value = await adminApi.music()
}

function applySearch() {
  applied.value = keyword.value
}

function open(row?: any) {
  form.id = row?.id
  form.name = row?.name || ''
  form.author = row?.author || ''
  form.url = row?.url || ''
  form.cover = row?.cover || ''
  form.lrc = row?.lrc || ''
  visible.value = true
}

async function save() {
  const name = form.name.trim()
  const url = form.url.trim()
  if (!name) {
    ElMessage.warning('请填写歌名')
    return
  }
  if (!url) {
    ElMessage.warning('请填写播放地址')
    return
  }
  try {
    await adminApi.saveMusic({
      ...form,
      name,
      author: form.author.trim(),
      url,
      cover: form.cover.trim(),
      lrc: form.lrc.trim(),
    })
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch {
    // 错误已由请求拦截器提示
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该音乐？', '提示', { type: 'warning' })
  try {
    await adminApi.deleteMusic(id)
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
    search-label="歌名"
    search-placeholder="请输入歌名或作者"
    @search="applySearch"
    @create="open()"
  >
    <el-table-column prop="name" label="歌名" />
    <el-table-column prop="author" label="作者" />
    <el-table-column prop="url" label="地址" show-overflow-tooltip />
    <el-table-column prop="createTime" label="创建时间" :formatter="tableTime" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="primary" @click="open(row)">编辑</el-button>
        <el-button link type="danger" @click="remove(row.id)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" :title="form.id ? '编辑音乐' : '新建音乐'" width="520px" align-center>
        <el-form label-width="80px">
          <el-form-item label="歌名"><el-input v-model="form.name" maxlength="80" /></el-form-item>
          <el-form-item label="作者"><el-input v-model="form.author" maxlength="80" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="form.url" placeholder="音频 URL" /></el-form-item>
          <el-form-item label="封面"><el-input v-model="form.cover" placeholder="封面 URL" /></el-form-item>
          <el-form-item label="歌词">
            <el-input v-model="form.lrc" type="textarea" :rows="6" placeholder="可选，LRC 格式" />
          </el-form-item>
        </el-form>
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </template>
  </CrudPage>
</template>
