import { defineStore } from 'pinia'
import { ref } from 'vue'

const DARK_KEY = 'blog_front_dark'
const COLOR_KEY = 'blog_front_color'

export const PRESET_COLORS = [
  '#ff4500',
  '#ff8c00',
  '#ffd700',
  '#90ee90',
  '#00ced1',
  '#1e90ff',
  '#a71585',
  '#00d463',
  '#c06070',
  '#c81545',
]

export const DEFAULT_COLOR = '#1e90ff'

function parseHex(hex: string) {
  let h = hex.replace('#', '').trim()
  if (h.length === 3 || h.length === 4) {
    h = [...h].map((c) => c + c).join('')
  }
  const r = Number.parseInt(h.slice(0, 2), 16) || 0
  const g = Number.parseInt(h.slice(2, 4), 16) || 0
  const b = Number.parseInt(h.slice(4, 6), 16) || 0
  const solid = `#${h.slice(0, 6).toLowerCase()}`
  return { r, g, b, solid }
}

function rgbToHue(r: number, g: number, b: number) {
  r /= 255
  g /= 255
  b /= 255
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const d = max - min
  if (d === 0) return 0
  let h = 0
  if (max === r) h = ((g - b) / d) % 6
  else if (max === g) h = (b - r) / d + 2
  else h = (r - g) / d + 4
  h *= 60
  if (h < 0) h += 360
  return Math.round(h)
}

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
  const a = parseHex(hex)
  const b = parseHex(target)
  const w = weight / 100
  const r = Math.round(a.r * (1 - w) + b.r * w)
  const g = Math.round(a.g * (1 - w) + b.g * w)
  const bl = Math.round(a.b * (1 - w) + b.b * w)
  return `#${[r, g, bl].map((v) => v.toString(16).padStart(2, '0')).join('')}`
}

function sameColor(a: string, b: string) {
  return a.replace('#', '').toLowerCase() === b.replace('#', '').toLowerCase()
}

export const useThemeStore = defineStore('theme', () => {
  const dark = ref(localStorage.getItem(DARK_KEY) === '1')
  const color = ref(localStorage.getItem(COLOR_KEY) || DEFAULT_COLOR)
  const hue = ref(0)

  function apply() {
    const root = document.documentElement
    const parsed = parseHex(color.value)
    hue.value = rgbToHue(parsed.r, parsed.g, parsed.b)
    root.classList.toggle('dark', dark.value)
    root.style.setProperty('--hue', String(hue.value))
    root.style.setProperty('--primary', color.value)
    root.style.setProperty('--btn-content', parsed.solid)
    root.style.setProperty('--el-color-primary', parsed.solid)
    root.style.setProperty('--el-color-primary-rgb', `${parsed.r}, ${parsed.g}, ${parsed.b}`)
    ;[3, 5, 7, 8, 9].forEach((step) => {
      root.style.setProperty(`--el-color-primary-light-${step}`, mix(parsed.solid, '#ffffff', step * 10))
    })
    root.style.setProperty('--el-color-primary-dark-2', mix(parsed.solid, '#000000', 20))
  }

  function setDark(value: boolean) {
    dark.value = value
    localStorage.setItem(DARK_KEY, value ? '1' : '0')
    apply()
  }

  function setColor(value: string) {
    color.value = value
    localStorage.setItem(COLOR_KEY, value)
    apply()
  }

  function setHue(value: number) {
    setColor(hslToHex(value, 0.58, 0.58))
  }

  function toggle() {
    setDark(!dark.value)
  }

  apply()
  return { dark, color, hue, apply, setDark, setColor, setHue, toggle, sameColor }
})
