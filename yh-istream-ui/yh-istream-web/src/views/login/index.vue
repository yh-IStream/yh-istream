<script setup lang="ts">
import { getCaptcha } from '@/api/modules/auth'
import { createAbortController, isAbortError } from '@/api/request'
import { LockClosedOutline, PersonOutline, ShieldCheckmarkOutline } from '@vicons/ionicons5'
import type { FormInst } from 'naive-ui'

const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const cardVisible = ref(false)
let captchaAbort: AbortController | null = null

const REMEMBER_KEY = 'yh-istream-remember'
const MIN_LOADING_MS = 800

function encodeRemember(username: string, password: string): string {
  return btoa(encodeURIComponent(JSON.stringify({ u: username, p: password })))
}

function decodeRemember(encoded: string): { username: string; password: string } | null {
  try {
    const obj = JSON.parse(decodeURIComponent(atob(encoded))) as Record<string, string>
    return { username: obj.u ?? obj.username ?? '', password: obj.p ?? obj.password ?? '' }
  } catch {
    if (encoded && !encoded.startsWith('{')) {
      return { username: encoded, password: '' }
    }
    return null
  }
}

function loadRemembered(): { username: string; password: string; remember: boolean } {
  const stored = localStorage.getItem(REMEMBER_KEY)
  if (stored) {
    const decoded = decodeRemember(stored)
    if (decoded) {
      return { username: decoded.username, password: decoded.password, remember: true }
    }
  }
  return { username: '', password: '', remember: false }
}

const remembered = loadRemembered()

const particleStyles = Array.from({ length: 20 }, () => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  animationDelay: `${Math.random() * 6}s`,
  animationDuration: `${3 + Math.random() * 4}s`,
  width: `${1 + Math.random() * 2}px`,
  height: `${1 + Math.random() * 2}px`,
}))

const formData = reactive({
  username: remembered.username,
  password: remembered.password,
  captchaCode: '',
  remember: remembered.remember,
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
  await nextTick()
  const startTime = Date.now()
  try {
    await authStore.login(formData.username, formData.password, captchaKey.value, formData.captchaCode)
    const elapsed = Date.now() - startTime
    if (elapsed < MIN_LOADING_MS) {
      await new Promise(r => setTimeout(r, MIN_LOADING_MS - elapsed))
    }
    if (formData.remember) {
      localStorage.setItem(REMEMBER_KEY, encodeRemember(formData.username, formData.password))
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    message.success('登录成功')
    const redirect = router.currentRoute.value.query.redirect as string | undefined
    router.push(redirect || authStore.firstMenuPath)
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '登录失败'
    message.error(msg || '登录失败')
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
        <div v-for="p in particleStyles" :key="p.id" class="particle" :style="p" />
      </div>
    </div>

    <transition name="card-enter">
      <div v-if="cardVisible" class="login-card glass glow-strong p-8 w-420px flex flex-col gap-6" :class="{ 'card-loading': loading }">
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

          <div class="login-btn-wrapper">
            <n-button
              type="primary"
              block
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              登 录
            </n-button>
            <div class="login-progress-bar" :class="{ 'progress-active': loading }">
              <div class="login-progress-glow" />
            </div>
          </div>
        </n-form>

        <div class="flex-center gap-1.5 mt-1 text-xs text-gray-400 dark:text-gray-500">
          <span class="inline-block w-1.5 h-1.5 rounded-full bg-teal-400 pulse-glow-animate" />
          <span>Secure Connection</span>
        </div>

        <transition name="overlay-fade">
          <div v-if="loading" class="loading-overlay">
            <div class="loading-overlay-content">
              <div class="loading-spinner" />
              <span class="loading-text">正在验证身份<span class="loading-dots" /></span>
            </div>
          </div>
        </transition>
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

.card-loading {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), filter 0.4s ease;
  transform: scale(0.98);
  filter: brightness(0.97);
}

.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(4px);
  border-radius: inherit;
}

.loading-overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(20, 184, 166, 0.2);
  border-top-color: #14b8a6;
  border-right-color: #06b6d4;
  border-radius: 50%;
  animation: spinner-rotate 0.8s linear infinite;
}

@keyframes spinner-rotate {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 15px;
  font-weight: 500;
  color: #14b8a6;
  letter-spacing: 2px;
}

.loading-dots::after {
  content: '';
  animation: dots 1.5s steps(4, end) infinite;
}

@keyframes dots {
  0% { content: ''; }
  25% { content: '.'; }
  50% { content: '..'; }
  75% { content: '...'; }
}

.overlay-fade-enter-active {
  transition: opacity 0.25s ease;
}
.overlay-fade-leave-active {
  transition: opacity 0.2s ease;
}
.overlay-fade-enter-from,
.overlay-fade-leave-to {
  opacity: 0;
}

.login-btn-wrapper {
  position: relative;
}

.login-progress-bar {
  position: relative;
  width: 100%;
  height: 4px;
  margin-top: 8px;
  background: rgba(20, 184, 166, 0.12);
  overflow: hidden;
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.login-progress-bar.progress-active {
  opacity: 1;
}

.login-progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 40%;
  background: linear-gradient(90deg, transparent, rgba(20, 184, 166, 0.9), rgba(6, 182, 212, 0.9), transparent);
  animation: progress-slide 1.2s ease-in-out infinite;
}

@keyframes progress-slide {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}
</style>