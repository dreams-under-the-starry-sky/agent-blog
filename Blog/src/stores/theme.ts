import { defineStore } from 'pinia'
import { ref } from 'vue'

const DARK_KEY = 'blog_front_dark'
const HUE_KEY = 'blog_front_hue'
export const DEFAULT_HUE = 250
export const PRESET_HUES = [0, 30, 60, 120, 160, 200, 250, 280, 320, 345]

function hslToHex(h: number, s: number, l: number) {
  const a = s * Math.min(l, 1 - l)
  const f = (n: number) => {
    const k = (n + h / 30) % 12
    const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1)
    return Math.round(255 * color).toString(16).padStart(2, '0')
  }
  return `#${f(0)}${f(8)}${f(4)}`
}

function mix(hex: string, target: string, weight: number) {
  const n = Number.parseInt(hex.replace('#', ''), 16)
  const t = Number.parseInt(target.replace('#', ''), 16)
  const a = { r: (n >> 16) & 255, g: (n >> 8) & 255, b: n & 255 }
  const b = { r: (t >> 16) & 255, g: (t >> 8) & 255, b: t & 255 }
  const w = weight / 100
  const r = Math.round(a.r * (1 - w) + b.r * w)
  const g = Math.round(a.g * (1 - w) + b.g * w)
  const bl = Math.round(a.b * (1 - w) + b.b * w)
  return `#${[r, g, bl].map((v) => v.toString(16).padStart(2, '0')).join('')}`
}

export const useThemeStore = defineStore('theme', () => {
  const dark = ref(localStorage.getItem(DARK_KEY) === '1')
  const hue = ref(Number(localStorage.getItem(HUE_KEY) || DEFAULT_HUE))

  function apply() {
    const root = document.documentElement
    const primary = hslToHex(hue.value, 0.58, 0.58)
    root.classList.toggle('dark', dark.value)
    root.style.setProperty('--hue', String(hue.value))
    root.style.setProperty('--el-color-primary', primary)
    const n = Number.parseInt(primary.replace('#', ''), 16)
    root.style.setProperty('--el-color-primary-rgb', `${(n >> 16) & 255}, ${(n >> 8) & 255}, ${n & 255}`)
    ;[3, 5, 7, 8, 9].forEach((step) => {
      root.style.setProperty(`--el-color-primary-light-${step}`, mix(primary, '#ffffff', step * 10))
    })
    root.style.setProperty('--el-color-primary-dark-2', mix(primary, '#000000', 20))
  }

  function setDark(value: boolean) {
    dark.value = value
    localStorage.setItem(DARK_KEY, value ? '1' : '0')
    apply()
  }

  function setHue(value: number) {
    hue.value = value
    localStorage.setItem(HUE_KEY, String(value))
    apply()
  }

  function toggle() {
    setDark(!dark.value)
  }

  apply()
  return { dark, hue, apply, setDark, setHue, toggle }
})
