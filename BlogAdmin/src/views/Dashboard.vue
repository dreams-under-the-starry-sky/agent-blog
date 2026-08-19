<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import FileText from '@vicons/tabler/es/FileText'
import Link from '@vicons/tabler/es/Link'
import Message from '@vicons/tabler/es/Message'
import MessageCircle from '@vicons/tabler/es/MessageCircle'
import Ban from '@vicons/tabler/es/Ban'
import AlertTriangle from '@vicons/tabler/es/AlertTriangle'
import { adminApi } from '@/api/admin'
import { tableTime } from '@/utils/format'

interface HotArticle {
  id: number
  title?: string
  pv?: number
}

interface BlackRow {
  id: number
  ip?: string
  nickname?: string
  email?: string
  position?: string
  createTime?: string | number | number[]
}

interface DashboardData {
  articleCount: number
  friendCount: number
  messageCount: number
  commentCount: number
  blackCount: number
  errorLogCount: number
  hotArticles: HotArticle[]
  recentBlacks: BlackRow[]
}

const data = ref<DashboardData>({
  articleCount: 0,
  friendCount: 0,
  messageCount: 0,
  commentCount: 0,
  blackCount: 0,
  errorLogCount: 0,
  hotArticles: [],
  recentBlacks: [],
})

const cards = computed(() => [
  { key: 'article', label: '文章', value: data.value.articleCount, icon: FileText },
  { key: 'friend', label: '友链', value: data.value.friendCount, icon: Link },
  { key: 'message', label: '留言', value: data.value.messageCount, icon: Message },
  { key: 'comment', label: '评论', value: data.value.commentCount, icon: MessageCircle },
  { key: 'black', label: '黑名单', value: data.value.blackCount, icon: Ban },
  { key: 'error', label: '错误日志', value: data.value.errorLogCount, icon: AlertTriangle },
])

const maxPv = computed(() => Math.max(1, ...data.value.hotArticles.map((item) => item.pv || 0)))

function barWidth(pv?: number) {
  return `${((pv || 0) / maxPv.value) * 100}%`
}

onMounted(async () => {
  data.value = await adminApi.dashboard()
})
</script>

<template>
  <div class="dashboard">
    <div class="stat-row">
      <el-card v-for="card in cards" :key="card.key" class="stat-card" shadow="never">
        <div class="stat">
          <span class="stat-left">
            <span class="stat-icon">
              <component :is="card.icon" />
            </span>
            <span class="stat-label">{{ card.label }}</span>
          </span>
          <span class="stat-value">{{ card.value }}</span>
        </div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never">
          <template #header>文章浏览前 10</template>
          <div v-if="data.hotArticles.length" class="bars">
            <div v-for="item in data.hotArticles" :key="item.id" class="bar-row">
              <span class="bar-title" :title="item.title">{{ item.title || '未命名' }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(item.pv) }" />
              </div>
              <span class="bar-value">{{ item.pv || 0 }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无文章" :image-size="64" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never">
          <template #header>近期黑名单</template>
          <el-table
            v-if="data.recentBlacks.length"
            :data="data.recentBlacks"
            class="equal-table"
            :header-cell-style="{ textAlign: 'center' }"
            :cell-style="{ textAlign: 'center' }"
          >
            <el-table-column prop="ip" label="IP" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="position" label="位置" />
            <el-table-column prop="createTime" label="时间" :formatter="tableTime" width="170" />
          </el-table>
          <el-empty v-else description="近两日暂无记录" :image-size="64" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card,
.panel {
  border-radius: 8px;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  min-width: 0;
  overflow: visible;
}

.stat-card :deep(.el-card__body) {
  padding: 12px;
  overflow: visible;
}

.stat {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.stat-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  overflow: hidden;
}

.stat-icon {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);

  :deep(svg) {
    width: 16px;
    height: 16px;
  }
}

.stat-label {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 17px;
  color: var(--el-text-color-regular);
}

.stat-value {
  flex: none;
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  color: var(--el-text-color-primary);
}

.bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-row {
  display: grid;
  grid-template-columns: minmax(0, 7.5rem) 1fr 3rem;
  align-items: center;
  gap: 10px;
}

.bar-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.bar-track {
  height: 10px;
  border-radius: 999px;
  background: var(--el-fill-color);
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: inherit;
  background: var(--el-color-primary);
}

.bar-value {
  font-size: 13px;
  text-align: right;
  color: var(--el-text-color-secondary);
}

.panel {
  min-height: 100%;
}

:deep(.equal-table) {
  width: 100%;

  .el-table__cell {
    text-align: center;
  }
}
</style>
