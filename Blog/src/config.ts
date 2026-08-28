export const siteConfig = {
  title: '长路漫漫',
  engTitle: 'Crossroads',
  subtitle: '朝花夕拾',
  author: '长路漫漫',
  bio: '记录技术、生活与沿途风景',
  github: 'https://github.com/dreams-under-the-starry-sky/agent-blog',
  startDate: '2025-04-09',
  msgAvator: 'https://myqc.net.cn/blog/head-picture.jpg',
  siteAvator: 'https://myqc.net.cn/blog/site-avatar.jpg'
}

/** 前台各页 pageId；目前仅留言板、友链开放 blog_message 评论 */
export const PAGE_IDS = {
  home: 29,
  category: 30,
  tag: 31,
  archive: 32,
  essays: 33,
  records: 34,
  about: 35,
  friends: 36,
  messages: 37,
} as const

export const COMMENTABLE_PAGE_IDS = [PAGE_IDS.friends, PAGE_IDS.messages] as const

export const PAGE_SIZE = 8
export const PAGE_WIDTH = '75rem'
export const BLOGGER_EMAIL = '1762546812@qq.com'
