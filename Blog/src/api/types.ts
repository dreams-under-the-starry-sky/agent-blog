export interface PageResult<T> {
  total: number
  list: T[]
}

export interface Article {
  id: number
  categoryId?: number
  title: string
  description?: string
  cover?: string
  thumbnail?: string
  comment?: number
  comments?: number
  status?: number
  recommend?: number
  pv?: number
  createTime?: string
  updateTime?: string
  yearTime?: number
  monthTime?: string | number
  content?: string
  categoryName?: string
  tags?: Tag[]
  tagNames?: string[]
  tagIds?: number[]
  images?: { imgUrl?: string; thumbnailUrl?: string }[]
}

export interface Category {
  id: number
  name: string
  count?: number
}

export interface Tag {
  id: number
  name: string
  articleCount?: number
}

export interface Comment {
  id: number
  articleId?: number
  parentId?: number
  rootId?: number
  content: string
  blogger?: number
  parentNickname?: string
  nickname?: string
  website?: string
  avatar?: string
  createTime?: string
  articleTitle?: string
  handle?: number
  visible?: number
  province?: string
  city?: string
  systemInfo?: string
  browser?: string
  children?: Comment[]
}

export interface Message {
  id: number
  parentId?: number
  rootId?: number
  content: string
  blogger?: number
  parentNickname?: string
  nickname?: string
  website?: string
  avatar?: string
  createTime?: string
  handle?: number
  visible?: number
  province?: string
  city?: string
  systemInfo?: string
  browser?: string
  children?: Message[]
}

export interface Essay {
  id: number
  content: string
  status?: number
  createTime?: string
  images?: { imgUrl?: string; thumbnailUrl?: string }[]
}

export interface RecordItem {
  id: number
  categoryId?: number
  happenTime?: number
  content: string
  categoryName?: string
  createTime?: string
  images?: { imgUrl?: string; thumbnailUrl?: string }[]
}

export interface Friend {
  id: number
  categoryId?: number
  categoryName?: string
  name: string
  description?: string
  logo?: string
  href?: string
  cover?: string
  sort?: number
}

export interface FriendCategory {
  id: number
  name: string
  sort?: number
  description?: string
}

export interface Music {
  id: number
  name: string
  author?: string
  url?: string
  cover?: string
  lrc?: string
}

export interface WebUpdateLog {
  id: number
  title: string
  description?: string
  createTime?: string
  updateTime?: string
}

export interface SidebarData {
  hotArticles: Article[]
  categories: Category[]
  tags: Tag[]
}
