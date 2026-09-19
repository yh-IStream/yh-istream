<script setup lang="ts">
import { getCaptcha } from '@/api/modules/auth'
import { createAbortController, isAbortError } from '@/api/request'
import { LockClosedOutline, PersonOutline, ShieldCheckmarkOutline } from '@vicons/ionicons5'

const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref()
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const cardVisible = ref(false)
let captchaAbort: AbortController | null = null

const REMEMBER_KEY = 'yh-istream-remember'

const particleStyles = Array.from({ length: 20 }, () => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  animationDelay: `${Math.random() * 6}s`,
  animationDuration: `${3 + Math.random() * 4}s`,
  width: `${1 + Math.random() * 2}px`,
  height: `${1 + Math.random() * 2}px`,
}))

const formData = reactive({
  username: localStorage.getItem(REMEMBER_KEY) ?? '',
  password: '',
  captchaCode: '',
  remember: !!localStorage.getItem(REMEMBER_KEY),
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function refreshCaptcha() {
  captchaAbort?.abort()
  captchaAbort = createAbortController()
  try {
    const res = await getCaptcha({ signal: captchaAbort.signal })
    const data = res.data
    let img = data.image
    if (img && !img.startsWith('data:')) {
      img = 'data:image/png;base64,' + img
    }
    captchaImage.value = img
    captchaKey.value = data.uuid
  } catch (e: unknown) {
    if (!isAbortError(e)) {
      // ignore non-abort errors silently on captcha refresh
    }
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
    if (formData.remember) {
      localStorage.setItem(REMEMBER_KEY, formData.username)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    message.success('登录成功')
    router.push(authStore.firstMenuPath)
  } catch (e: unknown) {
    message.error((e as Error).message || '登录失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (router.currentRoute.value.query.expired === '1') {
    message.warning('登录已过期，请重新登录')
  }
  requestAnimationFrame(() => {
    cardVisible.value = true
  })
  refreshCaptcha()
})

onBeforeUnmount(() => {
  captchaAbort?.abort()
})
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="orb orb-1" />
      <div class="orb orb-2" />
      <div class="orb orb-3" />
      <div class="grid-lines" />
      <div class="particles">
        <div v-for="(p, idx) in particleStyles" :key="idx" class="particle" :style="p" />
      </div>
    </div>

    <transition name="card-enter">
      <div v-if="cardVisible" class="login-card glass glow-strong p-8 w-420px flex flex-col gap-6">
        <div class="flex-col-center gap-3">
          <div class="logo-icon stream-animate float-animate">
            <svg viewBox="0 0 40 40" width="56" height="56">
              <defs>
                <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#14b8a6" />
                  <stop offset="50%" style="stop-color:#06b6d4" />
                  <stop offset="100%" style="stop-color:#3b82f6" />
                </linearGradient>
              </defs>
              <path d="M8 20 Q14 10 20 20 Q26 30 32 20" stroke="url(#logoGrad)" stroke-width="3" fill="none" stroke-linecap="round" />
              <path d="M8 28 Q14 18 20 28 Q26 38 32 28" stroke="url(#logoGrad)" stroke-width="2.5" fill="none" stroke-linecap="round" opacity="0.6" />
              <circle cx="8" cy="20" r="2" fill="#14b8a6" />
              <circle cx="32" cy="20" r="2" fill="#3b82f6" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold m-0 text-gray-800 dark:text-gray-100 tracking-wide">
            iStream
          </h1>
          <p class="text-sm m-0 text-teal-600/70 dark:text-teal-400/60 font-medium tracking-wider">
            INTELLIGENT STREAM PLATFORM
          </p>
        </div>

        <n-form ref="formRef" :model="formData" :rules="rules" size="large">
          <n-form-item path="username">
            <n-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
              :input-props="{ autocomplete: 'username' }"
              class="input-glow"
            >
              <template #prefix>
                <n-icon :component="PersonOutline" class="text-teal-500" />
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
              class="input-glow"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <n-icon :component="LockClosedOutline" class="text-teal-500" />
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="captchaCode">
            <div class="flex gap-2 w-full">
              <n-input
                v-model:value="formData.captchaCode"
                placeholder="请输入验证码"
                class="flex-1 input-glow"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <n-icon :component="ShieldCheckmarkOutline" class="text-teal-500" />
                </template>
              </n-input>
              <img
                v-if="captchaImage"
                :src="captchaImage"
                alt="验证码"
                class="h-40px w-120px rounded-lg cursor-pointer border border-teal-200/40 dark:border-teal-700/30 hover:border-teal-400/60 transition-all duration-300 hover:shadow-[0_0_8px_rgba(20,184,166,0.2)]"
                @click="refreshCaptcha"
              />
            </div>
          </n-form-item>

          <div class="flex items-center justify-between mb-2">
            <n-checkbox v-model:checked="formData.remember">
              记住密码
            </n-checkbox>
          </div>

          <n-button
            type="primary"
            block
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </n-button>
        </n-form>

        <div class="flex-center gap-1 text-xs text-gray-400 dark:text-gray-500 mt-1">
          <span class="inline-block w-1.5 h-1.5 rounded-full bg-teal-400 pulse-glow-animate" />
          <span>Secure Connection</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #0f172a;
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(20,184,166,0.4), transparent 70%);
  top: -10%;
  left: -5%;
  animation: float 8s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(6,182,212,0.35), transparent 70%);
  bottom: -15%;
  right: -5%;
  animation: float 10s ease-in-out infinite reverse;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(59,130,246,0.3), transparent 70%);
  top: 40%;
  right: 20%;
  animation: float 12s ease-in-out infinite;
}

.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(20,184,166,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(20,184,166,0.04) 1px, transparent 1px);
  background-size: 60px 60px;
}

.particles {
  position: absolute;
  inset: 0;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(20, 184, 166, 0.4);
  animation: particle-drift linear infinite;
}

@keyframes particle-drift {
  0% { transform: translateY(0) translateX(0); opacity: 0; }
  10% { opacity: 0.6; }
  90% { opacity: 0.6; }
  100% { transform: translateY(-100px) translateX(30px); opacity: 0; }
}

.card-enter-enter-active {
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.card-enter-leave-active {
  transition: all 0.3s ease-in;
}
.card-enter-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}
.card-enter-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(1.02);
}

.login-card {
  position: relative;
  z-index: 1;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.input-glow .n-input__border),
:deep(.input-glow .n-input__state-border) {
  transition: all 0.3s ease !important;
}

:deep(.input-glow:focus-within) {
  box-shadow: 0 0 0 1px rgba(20, 184, 166, 0.3), 0 0 12px rgba(20, 184, 166, 0.1) !important;
}

:deep(.login-btn) {
  height: 44px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 4px;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #14b8a6, #06b6d4, #3b82f6) !important;
  background-size: 200% 200% !important;
  transition: all 0.3s ease !important;
}

:deep(.login-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(20,184,166,0.35) !important;
  background-position: 100% 50% !important;
}

:deep(.login-btn:active) {
  transform: translateY(0px);
}

:deep(.n-input) {
  border-radius: 10px !important;
}

:deep(.n-form-item-feedback-wrapper) {
  min-height: 20px;
}
</style>