<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{ html: string }>()
const root = ref<HTMLElement | null>(null)
const preview = ref('')
const copyTimers: number[] = []

function clearCopyTimers() {
  while (copyTimers.length) {
    window.clearTimeout(copyTimers.pop())
  }
}

function enhance() {
  const el = root.value
  if (!el) return
  const copySvg = '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/></svg>'
  const checkSvg = '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12l5 5L20 7"/></svg>'
  el.querySelectorAll('pre').forEach((pre) => {
    if (pre.parentElement?.classList.contains('code-block')) return
    const wrap = document.createElement('div')
    wrap.className = 'code-block'
    const btn = document.createElement('button')
    btn.type = 'button'
    btn.className = 'copy-btn'
    btn.title = '复制'
    btn.innerHTML = copySvg
    btn.addEventListener('click', async (event) => {
      event.preventDefault()
      event.stopPropagation()
      try {
        await navigator.clipboard.writeText(pre.innerText)
        btn.innerHTML = checkSvg
        btn.classList.add('copied')
        ElMessage.success('已复制')
        copyTimers.push(window.setTimeout(() => {
          btn.innerHTML = copySvg
          btn.classList.remove('copied')
        }, 1200))
      } catch {
        ElMessage.error('复制失败')
      }
    })
    pre.parentNode?.insertBefore(wrap, pre)
    wrap.appendChild(btn)
    wrap.appendChild(pre)
  })
}

async function apply() {
  await nextTick()
  if (!root.value) return
  clearCopyTimers()
  root.value.innerHTML = props.html || ''
  enhance()
}

function onContentClick(event: MouseEvent) {
  const img = (event.target as HTMLElement | null)?.closest('img')
  if (img instanceof HTMLImageElement && img.src) {
    event.preventDefault()
    preview.value = img.src
  }
}

function closePreview() {
  preview.value = ''
}

function onKey(event: KeyboardEvent) {
  if (event.key === 'Escape') closePreview()
}

watch(() => props.html, apply)
onMounted(apply)

watch(preview, (value) => {
  if (value) {
    document.addEventListener('keydown', onKey)
    document.body.style.overflow = 'hidden'
  } else {
    document.removeEventListener('keydown', onKey)
    document.body.style.overflow = ''
  }
})

onBeforeUnmount(() => {
  clearCopyTimers()
  document.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <div>
    <div ref="root" class="markdown-body" @click="onContentClick" />
    <Teleport to="body">
      <div v-if="preview" class="lightbox" @click="closePreview">
        <img :src="preview" alt="" @click.stop />
      </div>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgb(0 0 0 / 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  cursor: zoom-out;
}
.lightbox img {
  max-width: min(92vw, 1200px);
  max-height: 90vh;
  border-radius: 0.75rem;
  cursor: default;
}
</style>
