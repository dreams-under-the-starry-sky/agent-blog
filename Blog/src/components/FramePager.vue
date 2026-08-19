<script setup lang="ts">
import FrameBtn from '@/components/FrameBtn.vue'

defineProps<{
  hasPrev: boolean
  hasNext: boolean
  paging?: 'prev' | 'next' | null
}>()

const emit = defineEmits<{
  prev: []
  next: []
}>()
</script>

<template>
  <div v-if="hasPrev || hasNext" class="nav-btns">
    <div v-if="hasPrev && hasNext" class="frame-pair">
      <FrameBtn bare compact sweep="rtl" :disabled="paging === 'next'" :loading="paging === 'prev'" @click="emit('prev')">Prev</FrameBtn>
      <FrameBtn bare compact sweep="ltr" :disabled="paging === 'prev'" :loading="paging === 'next'" @click="emit('next')">Next</FrameBtn>
    </div>
    <FrameBtn v-else-if="hasPrev" sweep="rtl" :loading="paging === 'prev'" @click="emit('prev')">Prev</FrameBtn>
    <FrameBtn v-else-if="hasNext" sweep="ltr" :loading="paging === 'next'" @click="emit('next')">Next</FrameBtn>
  </div>
</template>

<style scoped lang="scss">
.nav-btns {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1.5rem;
}
.frame-pair {
  position: relative;
  display: inline-flex;
  align-items: stretch;
  gap: 0;
  padding: 0.55rem 0.5rem 0.4rem;
  overflow: visible;
}
.frame-pair::before {
  content: '';
  position: absolute;
  top: 0.48rem;
  right: 0;
  bottom: 0;
  left: 0;
  border: 3px solid var(--primary);
  pointer-events: none;
}
.frame-pair :deep(.frame-btn:first-child .frame-btn__label) {
  border-radius: 8px 0 0 8px;
}
.frame-pair :deep(.frame-btn:last-child .frame-btn__label) {
  border-radius: 0 8px 8px 0;
}
</style>
