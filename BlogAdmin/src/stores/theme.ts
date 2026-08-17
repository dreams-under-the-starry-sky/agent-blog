import { defineStore } from 'pinia'
import { ref } from 'vue'

export const THEME_COLORS = [
  '#409EFF',
  '#3375B9',
  '#67C23A',
  '#13C2C2',
  '#E6A23C',
  '#F56C6C',
  '#F472B6',
  '#9B59B6',
  '#626AEF',
  '#8B5CF6',
  '#0EA5E9',
  '#10B981',
  '#F59E0B',
  '#EF4444',
  '#6366F1',
  '#14B8A6',
]

const DARK_KEY = 'blog_admin_dark'
const COLOR_KEY = 'blog_admin_primary'
const COLLAPSE_KEY = 'blog_admin_collapse'

function parseHex(hex: string) {
  const n = Number.parseInt(hex.replace('#', ''), 16)
  return { r: (n >> 16) & 255, g: (n >> 8) & 255, b: n & 255 }
}

function mix(hex: string, target: string, weight: number) {
  const a = parseHex(hex)
  const b = parseHex(target)
  const w = weight / 100
  const r = Math.round(a.r * (1 - w) + b.r * w)
  const g = Math.round(a.g * (1 - w) + b.g * w)
  const bl = Math.round(a.b * (1 - w) + b.b * w)
  return `#${[r, g, bl].map((v) => v.toString(16).padStart(2, '0')).join('')}`
}

export const useThemeStore = defineStore('theme', () => {
  const dark = ref(localStorage.getItem(DARK_KEY) === '1')
  const primary = ref(localStorage.getItem(COLOR_KEY) || '#409EFF')
  const collapsed = ref(localStorage.getItem(COLLAPSE_KEY) === '1')

  function apply() {
    const root = document.documentElement
    root.classList.toggle('dark', dark.value)
    root.style.setProperty('--el-color-primary', primary.value)
    const rgb = parseHex(primary.value)
    root.style.setProperty('--el-color-primary-rgb', `${rgb.r}, ${rgb.g}, ${rgb.b}`)
    ;[3, 5, 7, 8, 9].forEach((n) => {
      root.style.setProperty(`--el-color-primary-light-${n}`, mix(primary.value, '#ffffff', n * 10))
    })
    root.style.setProperty('--el-color-primary-dark-2', mix(primary.value, '#000000', 20))
  }

  function setDark(value: boolean) {
    dark.value = value
    localStorage.setItem(DARK_KEY, value ? '1' : '0')
    apply()
  }

  function setPrimary(color: string) {
    primary.value = color
    localStorage.setItem(COLOR_KEY, color)
    apply()
  }

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
    localStorage.setItem(COLLAPSE_KEY, collapsed.value ? '1' : '0')
  }

  apply()

  return { dark, primary, collapsed, apply, setDark, setPrimary, toggleCollapsed }
})
