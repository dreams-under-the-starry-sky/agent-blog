<script setup lang="ts">
import Search from '@vicons/tabler/es/Search'
import Refresh from '@vicons/tabler/es/Refresh'
import Plus from '@vicons/tabler/es/Plus'
import { PAGE_SIZE } from '@/utils/page'

const keyword = defineModel<string>('keyword', { default: '' })
const page = defineModel<number>('page', { default: 1 })

withDefaults(defineProps<{
  rows: any[]
  total: number
  searchLabel?: string
  searchPlaceholder?: string
  createText?: string
  showCreate?: boolean
  showKeyword?: boolean
  showSearch?: boolean
  showReset?: boolean
}>(), {
  showCreate: true,
  showKeyword: true,
  showSearch: true,
  showReset: true,
})

const emit = defineEmits<{
  search: []
  create: []
  pageChange: []
  reset: []
}>()

let mutePageChange = false

function onSearch() {
  if (page.value !== 1) {
    mutePageChange = true
    page.value = 1
  }
  emit('search')
}

function onReset() {
  keyword.value = ''
  emit('reset')
  onSearch()
}

function onPageChange() {
  if (mutePageChange) {
    mutePageChange = false
    return
  }
  emit('pageChange')
}
</script>

<template>
  <div class="crud">
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <span v-if="showKeyword && searchLabel" class="search-label">{{ searchLabel }}</span>
        <el-input
          v-if="showKeyword"
          v-model="keyword"
          :placeholder="searchPlaceholder || '请输入关键词'"
          clearable
          class="search-input"
          @keyup.enter="onSearch"
        />
        <slot name="filters" />
        <el-button v-if="showSearch" type="primary" @click="onSearch">
          <template #icon><Search class="btn-icon" /></template>
          查询
        </el-button>
        <el-button v-if="showReset" @click="onReset">
          <template #icon><Refresh class="btn-icon" /></template>
          重置
        </el-button>
        <el-button v-if="showCreate" type="primary" @click="emit('create')">
          <template #icon><Plus class="btn-icon" /></template>
          {{ createText || '新建' }}
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        :data="rows"
        class="equal-table"
        :header-cell-style="{ textAlign: 'center' }"
        :cell-style="{ textAlign: 'center' }"
      >
        <slot />
      </el-table>
      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="PAGE_SIZE"
          v-model:current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <slot name="extra" />
  </div>
</template>

<style scoped lang="scss">
.crud {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.table-card {
  border-radius: 8px;
}

.search-bar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}

.search-label {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.search-input {
  width: 220px;
}

.btn-icon {
  width: 14px;
  height: 14px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

:deep(.equal-table) {
  width: 100%;

  .el-table__header,
  .el-table__body {
    table-layout: fixed !important;
    width: 100% !important;
  }

  colgroup col {
    width: 0 !important;
  }

  .el-table__cell {
    text-align: center;
  }

  .cell {
    text-align: center;
    justify-content: center;
  }
}
</style>
