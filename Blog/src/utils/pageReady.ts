import { inject, onMounted, onUnmounted } from 'vue'

export const PAGE_READY_KEY = 'pageReady'

export function usePageReady() {
  const ready = inject<() => void>(PAGE_READY_KEY, () => {})
  let alive = true
  let token = 0
  onUnmounted(() => {
    alive = false
  })
  return () => {
    const current = ++token
    return () => {
      if (alive && current === token) ready()
    }
  }
}

export function usePageReadyOnMount() {
  const begin = usePageReady()
  onMounted(() => begin()())
}
