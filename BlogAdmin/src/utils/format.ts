import dayjs from 'dayjs'

function fromJacksonArray(value: number[]) {
  const [year, month, day, hour = 0, minute = 0, second = 0] = value
  return dayjs(new Date(year, month - 1, day, hour, minute, second))
}

export function formatTime(value?: string | number | Date | number[] | null) {
  if (value == null || value === '') return ''
  if (Array.isArray(value)) {
    if (value.length < 3) return ''
    const parsed = fromJacksonArray(value)
    return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : ''
  }
  if (typeof value === 'number' && Number.isInteger(value) && value >= 19700101 && value <= 20991231) {
    const text = String(value)
    if (text.length === 8) {
      return `${text.slice(0, 4)}-${text.slice(4, 6)}-${text.slice(6, 8)}`
    }
  }
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : String(value)
}

export function tableTime(_row: unknown, _column: unknown, cellValue: unknown) {
  return formatTime(cellValue as string | number | Date | number[] | null)
}

export function mediaUrl(url?: string) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')) return url
  return `/uploads/${url}`
}
