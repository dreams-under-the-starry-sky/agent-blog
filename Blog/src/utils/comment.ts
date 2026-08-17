const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export const NICKNAME_MAX = 20
export const CONTENT_MAX = 255

export function validateCommentInput(form: {
  nickname?: string
  email?: string
  content?: string
}): string | null {
  const nickname = (form.nickname || '').trim()
  const email = (form.email || '').trim()
  const content = (form.content || '').trim()
  if (!nickname) return '请填写昵称'
  if (nickname.length > NICKNAME_MAX) return '昵称不能超过20个字符'
  if (email && !EMAIL_RE.test(email)) return '请填写正确的邮箱格式'
  if (!content) return '请填写内容'
  if (content.length > CONTENT_MAX) return '消息内容不能超过255个字符'
  return null
}
