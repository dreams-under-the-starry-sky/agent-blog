<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import CrudPage from '@/components/CrudPage.vue'
import { filterPage } from '@/utils/page'

const all = ref<any[]>([])
const keyword = ref('')
const applied = ref('')
const page = ref(1)
const visible = ref(false)
const form = reactive({ ip: '', nickname: '', email: '', position: '' })
const view = computed(() => filterPage(all.value, applied.value, page.value, ['ip', 'nickname', 'email', 'position']))

async function load() {
  all.value = await adminApi.blacks()
}
function applySearch() {
  applied.value = keyword.value
}
async function save() {
  await adminApi.saveBlack({ ...form })
  ElMessage.success('已添加')
  visible.value = false
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
    search-label="昵称"
    search-placeholder="请输入 IP / 昵称 / 邮箱"
    @search="applySearch"
    @create="visible = true"
  >
    <el-table-column prop="ip" label="IP" />
    <el-table-column prop="nickname" label="昵称" />
    <el-table-column prop="email" label="邮箱" />
    <el-table-column prop="position" label="位置" />
    <el-table-column label="操作">
      <template #default="{ row }">
        <el-button link type="danger" @click="adminApi.deleteBlack(row.id).then(load)">删除</el-button>
      </template>
    </el-table-column>
    <template #extra>
      <el-dialog v-model="visible" title="黑名单" width="420px" align-center>
        <el-input v-model="form.ip" placeholder="IP" class="mt" />
        <el-input v-model="form.nickname" placeholder="昵称" class="mt" />
        <el-input v-model="form.email" placeholder="邮箱" class="mt" />
        <el-input v-model="form.position" placeholder="位置" class="mt" />
        <template #footer><el-button type="primary" @click="save">保存</el-button></template>
      </el-dialog>
    </template>
  </CrudPage>
</template>
<style scoped>.mt { margin-top: 12px; }</style>
