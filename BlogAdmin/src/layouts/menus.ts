import type { Component } from 'vue'
import Dashboard from '@vicons/tabler/es/Dashboard'
import FileText from '@vicons/tabler/es/FileText'
import Users from '@vicons/tabler/es/Users'
import User from '@vicons/tabler/es/User'
import World from '@vicons/tabler/es/World'

export interface MenuChild {
  path: string
  label: string
}

export interface MenuGroup {
  index: string
  label: string
  icon: Component
  path?: string
  children?: MenuChild[]
}

export const menus: MenuGroup[] = [
  {
    index: 'dashboard',
    label: '仪表盘',
    icon: Dashboard,
    path: '/dashboard',
  },
  {
    index: 'articles',
    label: '文章',
    icon: FileText,
    children: [
      { path: '/articles', label: '文章管理' },
      { path: '/categories', label: '分类管理' },
      { path: '/tags', label: '标签管理' },
      { path: '/comments', label: '评论管理' },
    ],
  },
  {
    index: 'users',
    label: '用户',
    icon: Users,
    children: [
      { path: '/friends', label: '友链管理' },
      { path: '/friend-categories', label: '友链分类' },
      { path: '/messages', label: '留言管理' },
    ],
  },
  {
    index: 'personal',
    label: '个人',
    icon: User,
    children: [
      { path: '/account', label: '账号设置' },
      { path: '/essays', label: '动态' },
      { path: '/records', label: '记录' },
      { path: '/record-categories', label: '记录分类' },
    ],
  },
  {
    index: 'site',
    label: '网站',
    icon: World,
    children: [
      { path: '/web-update-logs', label: '功能日志' },
      { path: '/music', label: '音乐' },
      { path: '/logs', label: '运行日志' },
      { path: '/email-fails', label: '失败邮件' },
    ],
  },
]

export function findMenuGroup(path: string) {
  return menus.find((group) => {
    if (group.path === path) return true
    return group.children?.some((child) => path === child.path || path.startsWith(`${child.path}/`))
  })
}

export function findMenuLeaf(path: string) {
  for (const group of menus) {
    if (group.path === path) return { group, child: undefined }
    const child = group.children?.find((item) => path === item.path || path.startsWith(`${item.path}/`))
    if (child) return { group, child }
  }
  return undefined
}
