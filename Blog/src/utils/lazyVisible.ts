import { onUnmounted, ref, watch, type Ref } from 'vue'

export function useLazyVisible(rootMargin = '240px') {
  const target = ref<HTMLElement | null>(null)
  const visible = ref(false)
  let observer: IntersectionObserver | null = null

  function disconnect() {
    observer?.disconnect()
    observer = null
  }

  function observe(el: HTMLElement | null) {
    disconnect()
    if (!el || visible.value || typeof IntersectionObserver === 'undefined') {
      if (el && !visible.value && typeof IntersectionObserver === 'undefined') {
        visible.value = true
      }
      return
    }
    observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry?.isIntersecting) {
          return
        }
        visible.value = true
        disconnect()
      },
      { rootMargin },
    )
    observer.observe(el)
  }

  watch(target, (el) => observe(el), { flush: 'post' })
  onUnmounted(disconnect)

  return { target: target as Ref<HTMLElement | null>, visible }
}
