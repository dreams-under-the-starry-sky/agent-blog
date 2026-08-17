export function formatTime(value?: string | number) {
  if (value == null || value === '') return ''
  if (typeof value === 'number' || /^\d{8}$/.test(String(value))) {
    const text = String(value)
    if (text.length === 8) {
      return `${text.slice(0, 4)}-${text.slice(4, 6)}-${text.slice(6, 8)}`
    }
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

export function formatDate(value?: string | number) {
  return formatTime(value).slice(0, 10)
}

export function mediaUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) return url
  return `/uploads/${url}`
}

export function readingMeta(text?: string) {
  const chars = (text || '').replace(/\s/g, '').length
  const minutes = Math.max(1, Math.round(chars / 400) || 1)
  return { chars: chars || 0, minutes }
}
