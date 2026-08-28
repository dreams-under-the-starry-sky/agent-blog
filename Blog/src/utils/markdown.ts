import hljs from 'highlight.js/lib/common'
import { Marked } from 'marked'
import { markedHighlight } from 'marked-highlight'

const parser = new Marked(
  markedHighlight({
    emptyLangClass: 'hljs',
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
      return hljs.highlight(code, { language }).value
    },
  }),
)

parser.setOptions({
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
  return parser.parse(source || '', { async: false }) as string
}

function withThumbnails(source?: string, images?: { imgUrl?: string; thumbnailUrl?: string }[]) {
  if (!source || !images?.length) return source || ''
  let out = source
  for (const img of images) {
    if (img.imgUrl && img.thumbnailUrl && img.imgUrl !== img.thumbnailUrl) {
      out = out.split(img.imgUrl).join(img.thumbnailUrl)
    }
  }
  return out
}

export function renderArticle(source?: string, images?: { imgUrl?: string; thumbnailUrl?: string }[]) {
  const headings: Heading[] = []
  const used = new Map<string, number>()
  let html = renderMarkdown(withThumbnails(source, images)).replace(/<h([1-4])([^>]*)>([\s\S]*?)<\/h\1>/gi, (_, level, attrs, inner) => {
    const plain = String(inner).replace(/<[^>]*>/g, '').trim()
    const depth = Number(level)
    const id = slugify(plain, used)
    if (depth >= 2) {
      headings.push({ id, text: plain, level: depth })
    }
    const cleanAttrs = String(attrs).replace(/\s+id=(["']).*?\1/i, '')
    return `<h${level}${cleanAttrs} id="${id}">${inner}</h${level}>`
  })
  return { html, headings }
}
