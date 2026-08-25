<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import ArrowsShuffle from '@vicons/tabler/es/ArrowsShuffle'
import Playlist from '@vicons/tabler/es/Playlist'
import PlayerPause from '@vicons/tabler/es/PlayerPause'
import PlayerPlay from '@vicons/tabler/es/PlayerPlay'
import PlayerSkipBack from '@vicons/tabler/es/PlayerSkipBack'
import PlayerSkipForward from '@vicons/tabler/es/PlayerSkipForward'
import Repeat from '@vicons/tabler/es/Repeat'
import RepeatOnce from '@vicons/tabler/es/RepeatOnce'
import Volume2 from '@vicons/tabler/es/Volume2'
import { frontApi } from '@/api/front'
import type { Music as MusicItem } from '@/api/types'
import { mediaUrl } from '@/utils/format'
import defaultCover from '@/assets/default-music-bg.png'

type PlayMode = 'loop' | 'single' | 'random'

const list = ref<MusicItem[]>([])
const index = ref(0)
const playing = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(40)
const mode = ref<PlayMode>('loop')
const listOpen = ref(true)
const audio = ref<HTMLAudioElement | null>(null)
const history = ref<number[]>([])
const current = computed(() => list.value[index.value] || null)
const coverSrc = computed(() => current.value?.cover ? mediaUrl(current.value.cover) : defaultCover)
const percent = computed(() => duration.value ? Math.min(100, (currentTime.value / duration.value) * 100) : 0)
const modeMeta = computed(() => {
  if (mode.value === 'single') return { title: '单曲循环', icon: RepeatOnce }
  if (mode.value === 'random') return { title: '随机播放', icon: ArrowsShuffle }
  return { title: '循环播放', icon: Repeat }
})

function fmt(sec: number) {
  if (!Number.isFinite(sec) || sec < 0) return '0:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

function srcOf(item?: MusicItem) {
  return mediaUrl(item?.url)
}

function applyVolume() {
  if (audio.value) audio.value.volume = volume.value / 100
}

async function load() {
  try {
    const data = await frontApi.music()
    const rows = Array.isArray(data) ? data : []
    list.value = rows.filter((item) => String(item.url || '').trim())
    index.value = 0
  } catch {
    list.value = []
  }
}

function bindSrc(i: number) {
  const el = audio.value
  const item = list.value[i]
  if (!el || !item) return
  index.value = i
  currentTime.value = 0
  duration.value = 0
  el.src = srcOf(item)
  applyVolume()
}

function playAt(i: number) {
  if (i !== index.value) history.value.push(index.value)
  bindSrc(i)
  audio.value?.play().then(() => { playing.value = true }).catch(() => { playing.value = false })
}

function toggle() {
  const el = audio.value
  if (!el || !current.value) return
  if (!el.src) bindSrc(index.value)
  if (playing.value) {
    el.pause()
    playing.value = false
    return
  }
  el.play().then(() => { playing.value = true }).catch(() => { playing.value = false })
}

function nextIndex(from: number) {
  const total = list.value.length
  if (total < 2) return from
  if (mode.value === 'random') {
    let next = from
    while (next === from) next = Math.floor(Math.random() * total)
    return next
  }
  return (from + 1) % total
}

function skip(step: number) {
  if (!list.value.length) return
  if (step < 0 && history.value.length) {
    const prev = history.value.pop() as number
    bindSrc(prev)
    audio.value?.play().then(() => { playing.value = true }).catch(() => { playing.value = false })
    return
  }
  playAt(step > 0 ? nextIndex(index.value) : (index.value - 1 + list.value.length) % list.value.length)
}

function cycleMode() {
  mode.value = mode.value === 'loop' ? 'single' : mode.value === 'single' ? 'random' : 'loop'
}

function onTime() {
  const el = audio.value
  if (!el) return
  currentTime.value = el.currentTime || 0
  duration.value = Number.isFinite(el.duration) ? el.duration : 0
}

function seek(event: MouseEvent) {
  const el = audio.value
  if (!el || !duration.value) return
  const bar = event.currentTarget as HTMLElement
  const rect = bar.getBoundingClientRect()
  el.currentTime = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width)) * duration.value
  onTime()
}

function onEnded() {
  if (!list.value.length) return
  if (mode.value === 'single' || list.value.length === 1) {
    const el = audio.value
    if (!el) return
    el.currentTime = 0
    el.play().then(() => { playing.value = true }).catch(() => { playing.value = false })
    return
  }
  playAt(nextIndex(index.value))
}

watch(volume, applyVolume)

onMounted(() => {
  load()
  applyVolume()
})
onBeforeUnmount(() => {
  const el = audio.value
  if (!el) return
  el.pause()
  el.removeAttribute('src')
  el.load()
})
</script>

<template>
  <div class="player">
    <div class="head">
      <div class="cover" :class="{ on: playing }">
        <img :src="coverSrc" alt="" />
      </div>
      <div class="meta">
        <p class="title">{{ current?.name || '暂未播放' }}</p>
        <p class="sub">{{ current?.author || '' }}</p>
        <div class="row">
          <span class="clock">{{ fmt(currentTime) }}/{{ fmt(duration) }}</span>
          <label class="vol">
            <Volume2 />
            <input v-model.number="volume" type="range" min="0" max="100" />
          </label>
        </div>
      </div>
    </div>

    <div class="bar" @click="seek">
      <i :style="{ width: `${percent}%` }" />
    </div>

    <div class="controls">
      <button type="button" :title="modeMeta.title" @click="cycleMode">
        <component :is="modeMeta.icon" />
      </button>
      <button type="button" title="上一首" :disabled="!list.length" @click="skip(-1)">
        <PlayerSkipBack />
      </button>
      <button type="button" class="main" :title="playing ? '暂停' : '播放'" :disabled="!list.length" @click="toggle">
        <PlayerPause v-if="playing" />
        <PlayerPlay v-else />
      </button>
      <button type="button" title="下一首" :disabled="!list.length" @click="skip(1)">
        <PlayerSkipForward />
      </button>
      <button type="button" title="播放列表" :class="{ on: listOpen }" @click="listOpen = !listOpen">
        <Playlist />
      </button>
    </div>

    <div class="tracks-wrap" :class="{ open: listOpen && list.length }">
      <ul class="tracks">
        <li
          v-for="(item, i) in list"
          :key="item.id"
          :class="{ on: i === index }"
          @click="playAt(i)"
        >
          <span>{{ item.name }}</span>
          <em>{{ item.author }}</em>
        </li>
      </ul>
    </div>

    <audio
      ref="audio"
      preload="none"
      @timeupdate="onTime"
      @loadedmetadata="onTime"
      @ended="onEnded"
      @pause="playing = false"
      @play="playing = true"
    />
  </div>
</template>

<style scoped lang="scss">
.player {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}
.head {
  display: flex;
  gap: 0.7rem;
  align-items: center;
}
.cover {
  width: 3.6rem;
  height: 3.6rem;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--btn-regular-bg);
  animation: spin 16s linear infinite;
  animation-play-state: paused;
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--btn-content) 18%, transparent);
}
.cover.on { animation-play-state: running; }
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.meta { min-width: 0; flex: 1; }
.title,
.sub,
.clock {
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.title {
  font-size: calc(1rem + 2px);
  font-weight: 700;
  color: var(--c-text-1);
}
.sub {
  margin-top: 0.12rem;
  font-size: calc(0.75rem + 2px);
  color: var(--c-text-3);
  min-height: 1em;
}
.row {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 0.55rem;
  margin-top: 0.18rem;
}
.clock {
  flex-shrink: 0;
  font-size: calc(0.75rem + 2px);
  color: var(--c-text-3);
}
.vol {
  width: 66.67%;
  justify-self: end;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 0.28rem;
  color: var(--c-text-3);
  :deep(svg) { width: 0.95rem; height: 0.95rem; flex-shrink: 0; }
}
.vol input[type='range'] {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 0.28rem;
  border-radius: 999px;
  background: var(--btn-regular-bg);
  outline: none;
}
.vol input[type='range']::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 0.7rem;
  height: 0.7rem;
  border-radius: 50%;
  background: var(--btn-content);
  cursor: pointer;
}
.bar {
  height: 0.28rem;
  border-radius: 999px;
  background: var(--btn-regular-bg);
  cursor: pointer;
  overflow: hidden;
}
.bar i {
  display: block;
  height: 100%;
  width: 0;
  border-radius: inherit;
  background: var(--btn-content);
}
.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.55rem;
}
.controls button {
  width: 2rem;
  height: 2rem;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  :deep(svg) { width: 1.15rem; height: 1.15rem; }
}
.controls button:hover:not(:disabled),
.controls button.on { color: var(--btn-content); }
.controls button:disabled { opacity: 0.35; cursor: default; }
.controls .main {
  width: 2.35rem;
  height: 2.35rem;
  background: var(--btn-regular-bg);
  color: var(--btn-content);
  :deep(svg) { width: 1.25rem; height: 1.25rem; }
}
.tracks-wrap {
  display: grid;
  grid-template-rows: 0fr;
  margin-top: -0.7rem;
  transition: grid-template-rows 0.28s ease, margin-top 0.28s ease;
}
.tracks-wrap.open {
  grid-template-rows: 1fr;
  margin-top: 0;
}
.tracks {
  list-style: none;
  margin: 0;
  padding: 0;
  min-height: 0;
  overflow: hidden;
  max-height: 8.5rem;
  scrollbar-width: thin;
  scrollbar-color: var(--btn-content) var(--btn-regular-bg);
}
.tracks-wrap.open .tracks { overflow: auto; }
.tracks::-webkit-scrollbar { width: 4px; }
.tracks::-webkit-scrollbar-thumb {
  background: var(--btn-content);
  border-radius: 999px;
}
.tracks::-webkit-scrollbar-track { background: var(--btn-regular-bg); }
.tracks li {
  display: flex;
  justify-content: space-between;
  gap: 0.45rem;
  padding: 0.35rem 0.15rem;
  border-top: 1px solid var(--line-divider);
  font-size: calc(0.78rem + 2px);
  color: var(--c-text-1);
  cursor: pointer;
}
.tracks li.on,
.tracks li:hover { color: var(--btn-content); }
.tracks em {
  font-style: normal;
  color: var(--c-text-3);
  flex-shrink: 0;
  max-width: 42%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
