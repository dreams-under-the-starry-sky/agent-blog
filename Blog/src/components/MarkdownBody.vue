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

function langOf(pre: HTMLElement) {
  const code = pre.querySelector('code')
  const match = (code?.className || '').match(/language-([\w+-]+)/)
  const lang = match?.[1] || ''
  return !lang || lang === 'plaintext' ? '' : lang
}

function enhance() {
  const el = root.value
  if (!el) return
  el.querySelectorAll('pre').forEach((pre) => {
    if (pre.parentElement?.closest('.code-block')) return
    const source = pre.innerText.replace(/\n$/, '')
    const lineCount = source ? source.split('\n').length : 1

    const wrap = document.createElement('div')
    wrap.className = 'code-block'
    const head = document.createElement('div')
    head.className = 'code-head'
    head.title = '折叠'
    head.innerHTML = '<span class="code-dots" aria-hidden="true"><i></i><i></i><i></i></span>'

    const actions = document.createElement('div')
    actions.className = 'code-actions'
    const lang = langOf(pre)
    if (lang) {
      const langEl = document.createElement('span')
      langEl.className = 'code-lang'
      langEl.textContent = lang
      actions.appendChild(langEl)
    }
    const copyBtn = document.createElement('button')
    copyBtn.type = 'button'
    copyBtn.className = 'copy-btn'
    copyBtn.textContent = '复制代码'
    copyBtn.addEventListener('click', async (event) => {
      event.preventDefault()
      event.stopPropagation()
      try {
        await navigator.clipboard.writeText(source)
        copyBtn.textContent = '已复制'
        copyBtn.classList.add('copied')
        ElMessage.success('已复制')
        copyTimers.push(window.setTimeout(() => {
          copyBtn.textContent = '复制代码'
          copyBtn.classList.remove('copied')
        }, 1200))
      } catch {
        ElMessage.error('复制失败')
      }
    })
    const foldHint = document.createElement('span')
    foldHint.className = 'fold-btn'
    foldHint.setAttribute('aria-hidden', 'true')
    foldHint.innerHTML = '<svg viewBox="0 0 16 16" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 6l4 4 4-4"/></svg>'
    actions.appendChild(copyBtn)
    actions.appendChild(foldHint)
    head.appendChild(actions)
    head.addEventListener('click', () => {
      const folded = wrap.classList.toggle('is-folded')
      head.title = folded ? '展开' : '折叠'
    })

    const body = document.createElement('div')
    body.className = 'code-body'
    const gutter = document.createElement('div')
    gutter.className = 'code-gutter'
    gutter.setAttribute('aria-hidden', 'true')
    gutter.textContent = Array.from({ length: lineCount }, (_, i) => String(i + 1)).join('\n')
    body.appendChild(gutter)

    pre.parentNode?.insertBefore(wrap, pre)
    wrap.appendChild(head)
    wrap.appendChild(body)
    body.appendChild(pre)
  })
  el.querySelectorAll('table').forEach((table) => {
    if (table.parentElement?.classList.contains('table-wrap')) return
    const wrap = document.createElement('div')
    wrap.className = 'table-wrap'
    table.parentNode?.insertBefore(wrap, table)
    wrap.appendChild(table)
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
