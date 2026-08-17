<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const nameForm = reactive({ username: auth.username, password: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

async function saveUsername() {
  const username = nameForm.username.trim()
  if (!username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (username.length > 15) {
    ElMessage.warning('用户名不能超过15个字符')
    return
  }
  if (!nameForm.password) {
    ElMessage.warning('请输入当前密码')
    return
  }
  try {
    const res = await authApi.updateUsername({ username, password: nameForm.password })
    auth.applySession(res.token, res.username)
    nameForm.password = ''
    ElMessage.success('用户名已更新')
  } catch {
    // 错误已由请求拦截器提示
  }
}

async function savePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写当前密码和新密码')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await authApi.updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    ElMessage.success('密码已更新')
  } catch {
    // 错误已由请求拦截器提示
  }
}
</script>

<template>
  <el-card shadow="never">
    <template #header>账号设置</template>
    <el-form label-width="100px" style="max-width: 460px">
      <el-form-item label="用户名">
        <el-input v-model="nameForm.username" maxlength="15" show-word-limit />
      </el-form-item>
      <el-form-item label="当前密码">
        <el-input v-model="nameForm.password" type="password" show-password placeholder="修改用户名需验证" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveUsername">保存新用户名</el-button>
      </el-form-item>
    </el-form>
    <el-divider />
    <el-form label-width="100px" style="max-width: 460px">
      <el-form-item label="当前密码">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="pwdForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="savePassword">保存新密码</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
