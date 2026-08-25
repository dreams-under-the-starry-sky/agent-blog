<script setup lang="ts">
import { ref, watch } from 'vue'
import ArrowBackUp from '@vicons/tabler/es/ArrowBackUp'
import ChevronDown from '@vicons/tabler/es/ChevronDown'
import ChevronUp from '@vicons/tabler/es/ChevronUp'
import Clock from '@vicons/tabler/es/Clock'
import DeviceDesktop from '@vicons/tabler/es/DeviceDesktop'
import MapPin from '@vicons/tabler/es/MapPin'
import World from '@vicons/tabler/es/World'
import { formatTime } from '@/utils/format'
import CommentForm from '@/components/CommentForm.vue'
import type { CommentFormModel } from '@/utils/comment'

export interface ThreadItem {
  id: number
  nickname?: string
  content: string
  blogger?: number
  parentNickname?: string
  website?: string
  avatar?: string
  createTime?: string
  province?: string
  city?: string
  district?: string
  systemInfo?: string
  browser?: string
  visible?: number
  handle?: number
  children?: ThreadItem[]
}

const props = defineProps<{
  items: ThreadItem[]
  replyTo: number | null
  form: CommentFormModel
}>()

const emit = defineEmits<{
  reply: [id: number]
  cancel: []
  submit: [parentId: number]
}>()

const expanded = ref<number[]>([])

function visibleList(items?: ThreadItem[]) {
  return (items || []).filter((item) => item.visible !== 0 && item.handle !== 0)
}

function childList(item: ThreadItem) {
  return visibleList(item.children)
}

function previewChildren(item: ThreadItem) {
  return childList(item).slice(0, 3)
}

function extraChildren(item: ThreadItem) {
  return childList(item).slice(3)
}

function expand(id: number) {
  if (!expanded.value.includes(id)) expanded.value = [...expanded.value, id]
}

function collapse(id: number) {
  const item = visibleList(props.items).find((row) => row.id === id)
  if (item && props.replyTo != null) {
    const hidden = childList(item).slice(3)
    if (hidden.some((child) => child.id === props.replyTo)) {
      emit('cancel')
    }
  }
  expanded.value = expanded.value.filter((value) => value !== id)
}

watch(
  () => props.replyTo,
  (id) => {
    if (id == null) return
    const parent = visibleList(props.items).find((item) => childList(item).some((child) => child.id === id))
    if (parent && childList(parent).length > 3) expand(parent.id)
  },
)

function initial(name?: string) {
  return (name || '?').trim().charAt(0).toUpperCase()
}

function hue(name?: string) {
  let hash = 0
  for (const ch of name || '') hash = (hash * 31 + ch.charCodeAt(0)) >>> 0
  return hash % 360
}

function displayMeta(value?: string) {
  const text = (value || '').trim()
  if (!text || /^unknown$/i.test(text) || text === 'web') return '未知'
  return text
}
</script>

<template>
  <div class="tk-list">
    <article v-for="item in visibleList(items)" :key="item.id" class="tk-comment">
      <div class="tk-avatar" :style="item.avatar ? undefined : { background: `oklch(0.62 0.12 ${hue(item.nickname)})` }">
        <img v-if="item.avatar" :src="item.avatar" alt="" referrerpolicy="no-referrer" />
        <template v-else>{{ initial(item.nickname) }}</template>
      </div>
      <div class="tk-main">
        <div class="tk-head">
          <a v-if="item.website" class="tk-nick" :href="item.website" target="_blank" rel="noreferrer">{{ item.nickname }}</a>
          <strong v-else class="tk-nick">{{ item.nickname }}</strong>
          <span v-if="item.blogger === 1" class="tk-tag">博主</span>
          <span v-if="item.parentNickname" class="tk-to">回复 @{{ item.parentNickname }}</span>
          <span class="tk-info">
            <MapPin class="tabler-icon" />{{ displayMeta(item.province) }}
          </span>
          <span class="tk-info">
            <DeviceDesktop class="tabler-icon" />{{ displayMeta(item.systemInfo) }}
          </span>
          <span class="tk-info">
            <World class="tabler-icon" />{{ displayMeta(item.browser) }}
          </span>
          <span class="tk-time">
            <Clock class="tabler-icon" />{{ formatTime(item.createTime) }}
          </span>
          <button class="tk-reply" type="button" title="回复" @click="emit('reply', item.id)">
            <ArrowBackUp class="tabler-icon" />
          </button>
        </div>
        <p class="tk-content">{{ item.content }}</p>
        <div v-if="replyTo === item.id" class="tk-reply-box">
          <CommentForm
            :model-value="form"
            cancelable
            :placeholder="`回复 @${item.nickname}`"
            @submit="emit('submit', item.id)"
            @cancel="emit('cancel')"
          />
        </div>
        <div v-if="childList(item).length" class="tk-replies">
          <article v-for="child in previewChildren(item)" :key="child.id" class="tk-comment nested">
            <div class="tk-avatar" :style="child.avatar ? undefined : { background: `oklch(0.62 0.12 ${hue(child.nickname)})` }">
              <img v-if="child.avatar" :src="child.avatar" alt="" referrerpolicy="no-referrer" />
              <template v-else>{{ initial(child.nickname) }}</template>
            </div>
            <div class="tk-main">
              <div class="tk-head">
                <a v-if="child.website" class="tk-nick" :href="child.website" target="_blank" rel="noreferrer">{{ child.nickname }}</a>
                <strong v-else class="tk-nick">{{ child.nickname }}</strong>
                <span v-if="child.blogger === 1" class="tk-tag">博主</span>
                <span v-if="child.parentNickname" class="tk-to">@{{ child.parentNickname }}</span>
                <span class="tk-info">
                  <MapPin class="tabler-icon" />{{ displayMeta(child.province) }}
                </span>
                <span class="tk-info">
                  <DeviceDesktop class="tabler-icon" />{{ displayMeta(child.systemInfo) }}
                </span>
                <span class="tk-info">
                  <World class="tabler-icon" />{{ displayMeta(child.browser) }}
                </span>
                <span class="tk-time">
                  <Clock class="tabler-icon" />{{ formatTime(child.createTime) }}
                </span>
                <button class="tk-reply" type="button" title="回复" @click="emit('reply', child.id)">
                  <ArrowBackUp class="tabler-icon" />
                </button>
              </div>
              <p class="tk-content">{{ child.content }}</p>
              <div v-if="replyTo === child.id" class="tk-reply-box">
                <CommentForm
                  :model-value="form"
                  cancelable
                  :placeholder="`回复 @${child.nickname}`"
                  @submit="emit('submit', child.id)"
                  @cancel="emit('cancel')"
                />
              </div>
            </div>
          </article>
          <div
            v-if="extraChildren(item).length"
            class="tk-extra-wrap"
            :class="{ open: expanded.includes(item.id) }"
          >
            <div class="tk-extra">
              <article v-for="child in extraChildren(item)" :key="child.id" class="tk-comment nested">
                <div class="tk-avatar" :style="child.avatar ? undefined : { background: `oklch(0.62 0.12 ${hue(child.nickname)})` }">
                  <img v-if="child.avatar" :src="child.avatar" alt="" referrerpolicy="no-referrer" />
                  <template v-else>{{ initial(child.nickname) }}</template>
                </div>
                <div class="tk-main">
                  <div class="tk-head">
                    <a v-if="child.website" class="tk-nick" :href="child.website" target="_blank" rel="noreferrer">{{ child.nickname }}</a>
                    <strong v-else class="tk-nick">{{ child.nickname }}</strong>
                    <span v-if="child.blogger === 1" class="tk-tag">博主</span>
                    <span v-if="child.parentNickname" class="tk-to">@{{ child.parentNickname }}</span>
                    <span class="tk-info">
                      <MapPin class="tabler-icon" />{{ displayMeta(child.province) }}
                    </span>
                    <span class="tk-info">
                      <DeviceDesktop class="tabler-icon" />{{ displayMeta(child.systemInfo) }}
                    </span>
                    <span class="tk-info">
                      <World class="tabler-icon" />{{ displayMeta(child.browser) }}
                    </span>
                    <span class="tk-time">
                      <Clock class="tabler-icon" />{{ formatTime(child.createTime) }}
                    </span>
                    <button class="tk-reply" type="button" title="回复" @click="emit('reply', child.id)">
                      <ArrowBackUp class="tabler-icon" />
                    </button>
                  </div>
                  <p class="tk-content">{{ child.content }}</p>
                  <div v-if="replyTo === child.id" class="tk-reply-box">
                    <CommentForm
                      :model-value="form"
                      cancelable
                      :placeholder="`回复 @${child.nickname}`"
                      @submit="emit('submit', child.id)"
                      @cancel="emit('cancel')"
                    />
                  </div>
                </div>
              </article>
            </div>
          </div>
          <button
            v-if="childList(item).length > 3 && !expanded.includes(item.id)"
            class="tk-more"
            type="button"
            @click="expand(item.id)"
          >
            <ChevronDown class="tabler-icon" />
            展开全部 {{ childList(item).length }} 条回复
          </button>
          <button
            v-else-if="childList(item).length > 3"
            class="tk-more"
            type="button"
            @click="collapse(item.id)"
          >
            <ChevronUp class="tabler-icon" />
            收起回复
          </button>
        </div>
      </div>
    </article>
  </div>
</template>

<style scoped lang="scss">
.tk-list {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}
.tk-comment {
  display: flex;
  gap: 0.85rem;
  padding: 1rem;
  border: 1px solid rgba(144, 147, 153, 0.31);
  border-radius: 1rem;
  transition: box-shadow 0.2s ease;
}
.tk-comment:hover { box-shadow: var(--shadow); }
.tk-comment.nested {
  background: var(--page-bg);
  margin-top: 0.75rem;
}
.tk-avatar {
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 50%;
  color: #fff;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.tk-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.tk-main { flex: 1; min-width: 0; }
.tk-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.35rem 0.55rem;
  font-size: calc(0.85rem + 2px);
  color: var(--c-text-3);
}
.tk-nick {
  color: var(--c-text-1);
  font-weight: 700;
}
.tk-tag {
  padding: 0.05rem 0.45rem;
  border-radius: 0.45rem;
  background: var(--btn-regular-bg);
  color: var(--btn-content);
  font-size: calc(0.75rem + 2px);
}
.tk-to { color: var(--btn-content); font-size: calc(0.8rem + 2px); }
.tk-info {
  display: inline-flex;
  align-items: center;
  gap: 0.18rem;
  color: var(--c-text-3);
  font-size: calc(0.78rem + 2px);
}
.tk-time {
  display: inline-flex;
  align-items: center;
  gap: 0.18rem;
  color: var(--c-text-3);
}
.tk-reply {
  margin-left: auto;
  border: none;
  background: none;
  color: var(--c-text-3);
  cursor: pointer;
  padding: 0.1rem;
  display: inline-flex;
  align-items: center;
}
.tk-reply:hover { color: var(--btn-content); }
.tk-content {
  margin: 0.55rem 0 0;
  color: var(--c-text-1);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
.tk-replies { margin-top: 0.35rem; }
.tk-extra-wrap {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.28s ease;
}
.tk-extra-wrap.open {
  grid-template-rows: 1fr;
}
.tk-extra {
  min-height: 0;
  overflow: hidden;
}
.tk-more {
  margin-top: 0.75rem;
  height: 2rem;
  padding: 0 0.85rem;
  border: 1px solid var(--line-divider);
  border-radius: 0.55rem;
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: calc(0.85rem + 2px);
}
.tk-more:hover {
  border-color: var(--btn-content);
  color: var(--btn-content);
}
.tk-reply-box {
  margin-top: 0.75rem;
  padding: 0.85rem;
  border: 1px solid var(--line-divider);
  border-radius: 0.85rem;
}
</style>
