<script setup lang="ts">
import { usePageReadyOnMount } from '@/utils/pageReady'

usePageReadyOnMount()
</script>

<template>
  <div class="about">
    <h1 class="page-title">关于</h1>
    <p>这里是一个基于 Vue3 + Spring Boot 3 的个人博客。</p>
    <p>记录技术、生活与沿途风景。欢迎留言交流。</p>
    <ul>
      <li>前台：Vue3 / TypeScript / Element Plus</li>
      <li>后台：独立管理端，JWT 登录</li>
      <li>服务：Spring Boot 3 + MyBatis</li>
    </ul>
  </div>
</template>

<style scoped lang="scss">
.about { line-height: 1.8; }
ul { color: var(--c-text-2); }
</style>
