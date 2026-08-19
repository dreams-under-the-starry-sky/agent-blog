<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import Activity from '@vicons/tabler/es/Activity'
import Cloud from '@vicons/tabler/es/Cloud'
import Copyright from '@vicons/tabler/es/Copyright'
import IdBadge from '@vicons/tabler/es/IdBadge'
import Terminal2 from '@vicons/tabler/es/Terminal2'
import { siteConfig } from '@/config'

const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | undefined
const start = new Date(`${siteConfig.startDate}T00:00:00`).getTime()
const year = new Date().getFullYear()
const runtime = computed(() => {
  const ms = Math.max(0, now.value - start)
  const days = Math.floor(ms / 86400000)
  const hours = Math.floor((ms % 86400000) / 3600000)
  const minutes = Math.floor((ms % 3600000) / 60000)
  return `${days}天${hours}小时${minutes}分`
})

onMounted(() => {
  timer = setInterval(() => { now.value = Date.now() }, 30000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="badges">
    <div class="row">
      <a class="badge" href="https://beian.miit.gov.cn/" target="_blank" rel="noreferrer">
        <span class="label"><IdBadge class="icon" />备案号</span>
        <span class="value red">蜀ICP备2024083430号</span>
      </a>
    </div>
    <div class="row">
      <span class="badge">
        <span class="label"><Cloud class="icon" />CDN</span>
        <span class="value green">腾讯云</span>
      </span>
      <span class="badge">
        <span class="label"><Copyright class="icon" />Copyright</span>
        <span class="value blue">© {{ year }} Crossroads</span>
      </span>
    </div>
    <div class="row">
      <span class="badge">
        <span class="label"><Activity class="icon" />Running Time</span>
        <span class="value purple">{{ runtime }}</span>
      </span>
      <span class="badge">
        <span class="label"><Terminal2 class="icon" />System.out.println</span>
        <span class="value ochre">Hello world !</span>
      </span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.badges {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.45rem;
}
.row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.45rem;
}
.badge {
  display: inline-flex;
  align-items: stretch;
  height: 1.65rem;
  overflow: hidden;
  border-radius: 0.28rem;
  color: #fff;
  font-size: calc(0.85rem + 2px);
  font-weight: 400;
  line-height: 1;
  letter-spacing: 0.02em;
  text-decoration: none;
}
.label,
.value {
  display: inline-flex;
  align-items: center;
  padding: 0 0.55rem;
  white-space: nowrap;
}
.label {
  gap: 0.28rem;
  background: #555;
}
.icon {
  width: 0.85rem;
  height: 0.85rem;
}
.red { background: #e05d44; }
.green { background: #4c1; }
.blue { background: #007ec6; }
.purple { background: #9f4bce; }
.ochre { background: #c9a227; }
</style>
