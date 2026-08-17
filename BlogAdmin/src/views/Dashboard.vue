<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const data = ref<any>({})
onMounted(async () => {
  data.value = await adminApi.dashboard()
})
</script>

<template>
  <div>
    <h2>仪表盘</h2>
    <el-row :gutter="16">
      <el-col :span="6"><el-card>文章 {{ data.articleCount }} / 已发布 {{ data.publishedCount }}</el-card></el-col>
      <el-col :span="6"><el-card>评论 {{ data.commentCount }}</el-card></el-col>
      <el-col :span="6"><el-card>留言 {{ data.messageCount }}</el-card></el-col>
      <el-col :span="6"><el-card>总阅读 {{ data.pvTotal }}</el-card></el-col>
    </el-row>
    <el-row :gutter="16" class="mt">
      <el-col :span="12">
        <el-card header="分类分布">
          <div v-for="item in data.categoryStats || []" :key="item.name">{{ item.name }} · {{ item.value }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="最新评论">
          <p v-for="item in data.recentComments || []" :key="item.id">{{ item.nickname }}：{{ item.content }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.mt { margin-top: 16px; }
</style>
