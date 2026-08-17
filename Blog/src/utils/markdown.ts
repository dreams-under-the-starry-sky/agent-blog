import { marked } from 'marked'

marked.setOptions({
  gfm: true,
  breaks: true,
})

export interface Heading {
  id: string
  text: string
  level: number
}

function slugify(text: string, used: Map<string, number>) {
  let slug = text
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^\w\u4e00-\u9fff-]/g, '')
  if (!slug) slug = 'section'
  const next = (used.get(slug) || 0) + 1
  used.set(slug, next)
  return next > 1 ? `${slug}-${next}` : slug
}

export function renderMarkdown(source?: string) {
  return marked.parse(source || '', { async: false }) as string
}

export function renderArticle(source?: string) {
  const headings: Heading[] = []
  const used = new Map<string, number>()
  let html = renderMarkdown(source).replace(/<h([1-4])>([\s\S]*?)<\/h\1>/gi, (_, level, inner) => {
    const plain = String(inner).replace(/<[^>]*>/g, '').trim()
    const depth = Number(level)
    const id = slugify(plain, used)
    if (depth >= 2) {
      headings.push({ id, text: plain, level: depth })
    }
    return `<h${level} id="${id}">${inner}</h${level}>`
  })
  return { html, headings }
}
