<script setup lang="ts">
withDefaults(defineProps<{
  disabled?: boolean
  loading?: boolean
  compact?: boolean
  bare?: boolean
  sweep?: 'ltr' | 'rtl'
}>(), {
  sweep: 'ltr',
})
</script>

<template>
  <button
    type="button"
    class="frame-btn"
    :class="[`sweep-${sweep}`, { compact, bare }]"
    :disabled="disabled || loading"
  >
    <span class="frame-btn__label" :class="{ loading }">
      <span v-show="!loading" class="frame-btn__text"><slot /></span>
      <span v-if="loading" class="frame-btn__spin" />
    </span>
  </button>
</template>

<style scoped lang="scss">
.frame-btn {
  position: relative;
  display: inline-flex;
  padding: 0.55rem 0.5rem 0.4rem;
  border: none;
  background: transparent;
  cursor: pointer;
  color: inherit;
  overflow: visible;
}
.frame-btn::before {
  content: '';
  position: absolute;
  top: 0.48rem;
  right: 0;
  bottom: 0;
  left: 0;
  border: 3px solid var(--primary);
  pointer-events: none;
}
.frame-btn__label {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 9rem;
  min-height: 2.35rem;
  margin-top: -0.42rem;
  padding: 0.48rem 1.56rem;
  border-radius: 8px;
  overflow: hidden;
  background: var(--primary);
  color: #fff;
  font-family: MutterKrause, var(--font-sans);
  font-style: italic;
  font-weight: 700;
  font-size: 1.5rem;
  line-height: 1.2;
  letter-spacing: 0.02em;
  white-space: nowrap;
}
.frame-btn__label::after {
  content: '';
  position: absolute;
  inset: 0;
  background: color-mix(in oklab, #fff 32%, transparent);
  transform: scaleX(0);
  pointer-events: none;
  transition: transform 0.38s ease;
}
.sweep-ltr .frame-btn__label::after {
  transform-origin: left center;
}
.sweep-rtl .frame-btn__label::after {
  transform-origin: right center;
}
.frame-btn:hover:not(:disabled) .frame-btn__label::after {
  transform: scaleX(1);
}
.frame-btn.bare {
  padding: 0;
}
.frame-btn.bare::before {
  content: none;
}
.frame-btn.compact .frame-btn__label {
  min-width: 4.5rem;
  padding-left: 0.78rem;
  padding-right: 0.78rem;
}
.frame-btn__text {
  position: relative;
  z-index: 1;
}
.frame-btn__spin {
  position: relative;
  z-index: 1;
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid rgb(255 255 255 / 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
.frame-btn:disabled {
  cursor: default;
}
.frame-btn:disabled .frame-btn__label:not(.loading) {
  filter: saturate(0.85);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
