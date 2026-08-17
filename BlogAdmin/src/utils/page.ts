export const PAGE_SIZE = 10

export function filterPage<T extends Record<string, any>>(
  list: T[],
  keyword: string,
  page: number,
  fields: string[],
) {
  const kw = keyword.trim().toLowerCase()
  const filtered = kw
    ? list.filter((item) => fields.some((field) => String(item[field] ?? '').toLowerCase().includes(kw)))
    : list
  const start = (Math.max(page, 1) - 1) * PAGE_SIZE
  return {
    total: filtered.length,
    rows: filtered.slice(start, start + PAGE_SIZE),
  }
}
