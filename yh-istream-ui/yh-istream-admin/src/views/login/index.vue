<script setup lang="ts">
import { getCaptcha } from '@/api/modules/auth'
import { LockClosedOutline, PersonOutline, ShieldCheckmarkOutline } from '@vicons/ionicons5'

const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref()
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const formData = reactive({
  username: 'admin',
  password: '',
  captchaCode: '',
  remember: false,
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    const data = (res as any).data
    let img = data.image
    if (img && !img.startsWith('data:')) {
      img = 'data:image/png;base64,' + img
    }
    captchaImage.value = img
    captchaKey.value = data.uuid
  } catch {
    // ignore
  }
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await authStore.login(formData.username, formData.password, captchaKey.value, formData.captchaCode)
    message.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    message.error(e.message || '登录失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

refreshCaptcha()
</script>

<template>
  <div class="wh-full flex-center bg-gradient-to-br from-green-50 to-emerald-100 dark:from-gray-900 dark:to-gray-800">
    <div class="card w-400px flex flex-col gap-24px">
      <!-- Logo -->
      <div class="flex-col-center gap-8px">
        <div class="w-56px h-56px rounded-xl bg-primary flex-center text-white text-28px font-bold shadow-lg">
          i
        </div>
        <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-100 m-0">
          iStream
        </h1>
        <p class="text-sm text-gray-500 dark:text-gray-400 m-0">
          智能流式管理平台
        </p>
      </div>

      <!-- 表单 -->
      <n-form ref="formRef" :model="formData" :rules="rules" size="large">
        <n-form-item path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :input-props="{ autocomplete: 'username' }"
          >
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            :input-props="{ autocomplete: 'current-password' }"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="captchaCode">
          <div class="flex gap-8px w-full">
            <n-input
              v-model:value="formData.captchaCode"
              placeholder="请输入验证码"
              class="flex-1"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon :component="ShieldCheckmarkOutline" />
              </template>
            </n-input>
            <img
              v-if="captchaImage"
              :src="captchaImage"
              alt="验证码"
              class="h-40px w-120px rounded cursor-pointer border border-gray-200 dark:border-gray-600"
              @click="refreshCaptcha"
            />
          </div>
        </n-form-item>

        <div class="flex items-center justify-between mb-8px">
          <n-checkbox v-model:checked="formData.remember">
            记住密码
          </n-checkbox>
        </div>

        <n-button
          type="primary"
          block
          :loading="loading"
          @click="handleLogin"
        >
          登 录
        </n-button>
      </n-form>
    </div>
  </div>
</template>