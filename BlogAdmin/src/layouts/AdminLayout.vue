<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Check from '@vicons/tabler/es/Check'
import Globe from '@vicons/tabler/es/Globe'
import BrandGithub from '@vicons/tabler/es/BrandGithub'
import LayoutSidebarLeftCollapse from '@vicons/tabler/es/LayoutSidebarLeftCollapse'
import LayoutSidebarLeftExpand from '@vicons/tabler/es/LayoutSidebarLeftExpand'
import Settings from '@vicons/tabler/es/Settings'
import { useAuthStore } from '@/stores/auth'
import { THEME_COLORS, useThemeStore } from '@/stores/theme'
import { findMenuLeaf, menus } from './menus'
import avatarImg from '@/assets/siteAvatar.jpg'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const theme = useThemeStore()
const settingsOpen = ref(false)

const FRONT_URL = `https://crossroads.net.cn`
const GITHUB_URL = 'https://github.com/dreams-under-the-starry-sky/agent-blog'

const activeMenu = computed(() => {
  if (route.path.startsWith('/articles/edit')) return '/articles'
  return route.path
})

const openedMenus = computed(() => menus.filter((item) => item.children?.length).map((item) => item.index))
const menuKey = computed(() => menus.map((group) => `${group.index}:${group.children?.map((child) => child.path).join(',') || group.path || ''}`).join('|'))

const crumbs = computed(() => {
  const items = [{ path: '/dashboard', label: 'crossroads' }]
  const hit = findMenuLeaf(route.path)
  if (!hit) {
    const title = typeof route.meta.title === 'string' ? route.meta.title : ''
    if (title) items.push({ path: route.path, label: title })
    return items
  }
  if (hit.group.path !== '/dashboard') {
    items.push({ path: hit.child?.path || hit.group.path || '', label: hit.group.label })
  }
  if (hit.child) {
    items.push({ path: hit.child.path, label: hit.child.label })
  } else if (hit.group.path) {
    items.push({ path: hit.group.path, label: hit.group.label })
  }
  if (route.path.startsWith('/articles/edit')) {
    items[items.length - 1] = {
      path: route.path,
      label: route.params.id ? '编辑文章' : '写文章',
    }
  }
  return items
})

function logout() {
  auth.logout()
  settingsOpen.value = false
  router.push('/login')
}

function onAvatarCommand(command: string) {
  if (command === 'logout') logout()
}
</script>

<template>
  <el-container class="layout">
    <el-aside class="aside" :width="theme.collapsed ? '64px' : '220px'">
      <div class="brand" :class="{ compact: theme.collapsed }">
        {{ theme.collapsed ? 'CS' : 'Crossroads' }}
      </div>
      <el-menu
        :key="menuKey"
        class="side-menu"
        router
        :collapse="theme.collapsed"
        :collapse-transition="false"
        :default-active="activeMenu"
        :default-openeds="openedMenus"
      >
        <template v-for="group in menus" :key="group.index">
          <el-menu-item v-if="group.path" :index="group.path">
            <i class="menu-svg"><component :is="group.icon" /></i>
            <span>{{ group.label }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="group.index">
            <template #title>
              <i class="menu-svg"><component :is="group.icon" /></i>
              <span>{{ group.label }}</span>
            </template>
            <el-menu-item v-for="child in group.children" :key="child.path" :index="child.path">
              {{ child.label }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="body">
      <el-header class="header">
        <div class="header-left">
          <button class="icon-btn" type="button" :title="theme.collapsed ? '展开菜单' : '收起菜单'" @click="theme.toggleCollapsed()">
            <LayoutSidebarLeftExpand v-if="theme.collapsed" />
            <LayoutSidebarLeftCollapse v-else />
          </button>
          <el-breadcrumb separator=">">
            <el-breadcrumb-item
              v-for="(item, index) in crumbs"
              :key="item.path + index"
              :to="index === 0 && crumbs.length > 1 ? item.path : undefined"
            >
              {{ item.label }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <a class="icon-btn" :href="FRONT_URL" target="_blank" rel="noreferrer" title="跳转前台">
            <Globe />
          </a>
          <a class="icon-btn" :href="GITHUB_URL" target="_blank" rel="noreferrer" title="GitHub">
            <BrandGithub />
          </a>
          <button class="icon-btn" type="button" title="设置" @click="settingsOpen = true">
            <Settings />
          </button>
          <el-dropdown trigger="hover" @command="onAvatarCommand">
            <span class="avatar-trigger">
              <img class="avatar" :src="avatarImg" alt="avatar" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>

  <el-drawer v-model="settingsOpen" title="布局设置" size="300px" append-to-body>
    <div class="drawer-block">
      <p class="drawer-label">主题颜色</p>
      <div class="color-grid">
        <button
          v-for="color in THEME_COLORS"
          :key="color"
          type="button"
          class="color-dot"
          :style="{ background: color }"
          :title="color"
          @click="theme.setPrimary(color)"
        >
          <Check v-if="theme.primary === color" class="color-check" />
        </button>
      </div>
    </div>
    <div class="drawer-block">
      <p class="drawer-label">界面设置</p>
      <div class="drawer-row">
        <span>暗黑模式</span>
        <el-switch :model-value="theme.dark" @update:model-value="theme.setDark" />
      </div>
    </div>
  </el-drawer>
</template>

<style scoped lang="scss">
$aside-bg: #191a23;

.layout {
  height: 100%;
}

.aside {
  display: flex;
  flex-direction: column;
  background: $aside-bg;
  overflow: hidden;
  transition: width 0.2s ease;
}

.brand {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-family: "MutterKrause", Georgia, serif;
  font-size: 22px;
  font-style: italic;
  letter-spacing: 0.02em;
  background: $aside-bg;
}

.brand.compact {
  font-size: 18px;
  letter-spacing: 0;
}

.side-menu {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none !important;
  background: $aside-bg !important;
  padding-bottom: 24px;
}

:deep(.el-menu) {
  background: $aside-bg !important;
  border-right: none;
}

:deep(.el-sub-menu .el-menu) {
  background: $aside-bg !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.78) !important;
  background: $aside-bg !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--el-color-primary) !important;
  color: #fff !important;
}

:deep(.el-menu:not(.el-menu--collapse) .el-sub-menu .el-menu-item) {
  padding-left: 54px !important;
  min-width: 0;
}

:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.45);
}

:deep(.el-menu--collapse .el-menu-item),
:deep(.el-menu--collapse .el-sub-menu__title) {
  padding: 0 20px !important;
}

.menu-svg {
  display: inline-flex;
  width: 18px;
  height: 18px;
  margin-right: 10px;
  color: currentColor;

  :deep(svg) {
    width: 18px;
    height: 18px;
  }
}

:deep(.el-menu--collapse .menu-svg) {
  margin-right: 0;
}

.body {
  min-width: 0;
  background: var(--el-bg-color-page);
}

.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--el-text-color-regular);
  cursor: pointer;
  text-decoration: none;

  :deep(svg) {
    width: 18px;
    height: 18px;
  }
}

.icon-btn:hover {
  background: var(--el-fill-color);
  color: var(--el-color-primary);
}

.avatar-trigger {
  display: inline-flex;
  outline: none;
  cursor: pointer;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.main {
  background: var(--el-bg-color-page);
}

.drawer-block + .drawer-block {
  margin-top: 28px;
}

.drawer-label {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.drawer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(8, 20px);
  gap: 10px;
}

.color-dot {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 4px;
  padding: 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.color-check {
  width: 12px;
  height: 12px;
  color: #fff;
}
</style>

<style lang="scss">
.el-menu--popup {
  background: #191a23 !important;
  border: none !important;

  .el-menu-item {
    color: rgba(255, 255, 255, 0.78) !important;
    background: #191a23 !important;
  }

  .el-menu-item:hover {
    background: rgba(255, 255, 255, 0.06) !important;
    color: #fff !important;
  }

  .el-menu-item.is-active {
    background: var(--el-color-primary) !important;
    color: #fff !important;
  }
}
</style>
