<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Lock from '@vicons/tabler/es/Lock'
import User from '@vicons/tabler/es/User'
import { useAuthStore } from '@/stores/auth'
import loginBg from '@/assets/admin-login-bg.jpg'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

onMounted(() => {
  document.documentElement.style.overflow = 'hidden'
  document.body.style.overflow = 'hidden'
})
onUnmounted(() => {
  document.documentElement.style.overflow = ''
  document.body.style.overflow = ''
})

async function submit() {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    await router.push('/dashboard')
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login" :style="{ backgroundImage: `url(${loginBg})` }">
    <div class="panel">
      <h1 class="title-left">Login</h1>
      <h1 class="title-right">Crossroads</h1>
      <el-form class="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="Username">
            <template #prefix><User class="tabler-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="Password" show-password>
            <template #prefix><Lock class="tabler-icon" /></template>
          </el-input>
        </el-form-item>
        <el-button class="submit" :loading="loading" native-type="submit">Log in</el-button>
      </el-form>
      <p class="intro">
        We have arrived at the station.<br />
        Have you decided where to go<br />
        next.
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
@font-face {
  font-family: "MutterKrause";
  src: url("@/assets/fonts/MutterKrauseNormal.ttf") format("truetype");
  font-weight: normal;
  font-style: normal;
  font-display: swap;
}

.login {
  height: 100%;
  overflow: hidden;
  display: grid;
  place-items: center;
  background-size: cover;
  background-position: center;
  font-family: "MutterKrause", "Segoe UI", "Microsoft YaHei", sans-serif;
}

.panel {
  position: relative;
  width: min(585px, calc(100% - 48px));
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto 1fr;
  column-gap: 24px;
  row-gap: 22px;
  padding: 42px 32px;
  border-radius: 18px;
  border: none;
  background: transparent;
  box-shadow:
    0 0 8px rgba(255, 255, 255, 0.9),
    0 0 22px rgba(220, 255, 240, 0.5),
    0 0 48px rgba(255, 255, 255, 0.32);
}

.title-left {
  grid-column: 1;
  grid-row: 1;
}

.title-right {
  grid-column: 2;
  grid-row: 1;
}

h1 {
  margin: 0;
  text-align: center;
  font-size: 34px;
  font-weight: 400;
  font-style: italic;
  letter-spacing: 0.02em;
  color: #fff;
  line-height: 1.2;
}

.form {
  grid-column: 1;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.intro {
  grid-column: 2;
  grid-row: 2;
  align-self: end;
  margin: 0;
  text-align: center;
  font-size: 22px;
  font-style: italic;
  line-height: 2.35;
  color: #fff;
}

:deep(.el-input) {
  --el-text-color-placeholder: #ffffff;
  --el-text-color-regular: #ffffff;
  --el-input-icon-color: #ffffff;
  --el-input-text-color: #ffffff;
}

:deep(.el-form-item) {
  width: 100%;
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  padding: 4px 0;
  background: transparent;
  box-shadow: none;
  border-radius: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.55);
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  background: transparent;
  box-shadow: none;
  border-bottom-color: #fff;
}

:deep(.el-input__inner),
:deep(.el-input__inner::placeholder),
:deep(.el-input__prefix),
:deep(.el-input__suffix),
:deep(.el-button) {
  font-family: "MutterKrause", "Segoe UI", "Microsoft YaHei", sans-serif;
  font-style: italic;
}

:deep(.el-input__inner) {
  color: #fff;
  height: 34px;
}

:deep(.el-input__inner::placeholder) {
  color: #fff;
  opacity: 1;
}

:deep(.el-input__inner::-webkit-input-placeholder) {
  color: #fff;
  opacity: 1;
}

:deep(.el-input__prefix),
:deep(.el-input__suffix) {
  color: #fff;
}

.tabler-icon {
  width: 16px;
  height: 16px;
  color: #fff;
}

.submit {
  width: 128px;
  height: 36px;
  margin-top: 11px;
  border: none;
  border-radius: 999px;
  background: #2ea043;
  color: #fff;
  font-size: 16px;
  font-style: italic;
  letter-spacing: 0.02em;
}

.submit:hover,
.submit:focus {
  background: #4aa557;
  color: #fff;
}

@media (max-width: 800px) {
  .panel {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto;
    padding: 28px 24px;
  }

  .title-left { grid-row: 1; grid-column: 1; }
  .title-right { grid-row: 2; grid-column: 1; }
  .form { grid-row: 3; grid-column: 1; }
  .intro { grid-row: 4; grid-column: 1; }
}
</style>
