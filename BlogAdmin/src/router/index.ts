import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', meta: { title: '仪表盘' }, component: () => import('@/views/Dashboard.vue') },
        { path: 'articles', name: 'articles', meta: { title: '文章管理' }, component: () => import('@/views/articles/List.vue') },
        { path: 'articles/edit/:id?', name: 'article-edit', meta: { title: '编辑文章' }, component: () => import('@/views/articles/Edit.vue') },
        { path: 'categories', meta: { title: '分类管理' }, component: () => import('@/views/Categories.vue') },
        { path: 'tags', meta: { title: '标签管理' }, component: () => import('@/views/Tags.vue') },
        { path: 'comments', meta: { title: '评论管理' }, component: () => import('@/views/Comments.vue') },
        { path: 'messages', meta: { title: '留言管理' }, component: () => import('@/views/Messages.vue') },
        { path: 'account', meta: { title: '账号设置' }, component: () => import('@/views/Account.vue') },
        { path: 'essays', meta: { title: '动态' }, component: () => import('@/views/Essays.vue') },
        { path: 'records', meta: { title: '记录' }, component: () => import('@/views/Records.vue') },
        { path: 'record-categories', meta: { title: '记录分类' }, component: () => import('@/views/RecordCategories.vue') },
        { path: 'friends', meta: { title: '友链管理' }, component: () => import('@/views/Friends.vue') },
        { path: 'friend-categories', meta: { title: '友链分类' }, component: () => import('@/views/FriendCategories.vue') },
        { path: 'music', meta: { title: '音乐' }, component: () => import('@/views/Music.vue') },
        { path: 'blacks', meta: { title: '黑名单' }, component: () => import('@/views/Blacks.vue') },
        { path: 'logs', meta: { title: '运行日志' }, component: () => import('@/views/Logs.vue') },
        { path: 'web-update-logs', meta: { title: '功能日志' }, component: () => import('@/views/WebUpdateLogs.vue') },
        { path: 'emails', meta: { title: '邮件记录' }, component: () => import('@/views/Emails.vue') },
        { path: 'file-fails', meta: { title: '删除失败' }, component: () => import('@/views/FileFails.vue') },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.loggedIn) {
    return '/login'
  }
  if (to.path === '/login' && auth.loggedIn) {
    return '/dashboard'
  }
  return true
})

export default router
