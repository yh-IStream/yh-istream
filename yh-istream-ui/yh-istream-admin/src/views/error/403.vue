<script setup lang="ts">
import { HomeOutline } from '@vicons/ionicons5'

const router = useRouter()
</script>

<template>
  <div class="error-page">
    <div class="error-bg">
      <div class="orb orb-1" />
      <div class="orb orb-2" />
    </div>
    <div class="relative z-1">
      <n-result
        status="403"
        title="无访问权限"
        description="你没有权限访问此页面，如需访问请联系管理员授权"
      >
        <template #footer>
          <n-button type="primary" @click="router.push('/dashboard')">
            <template #icon>
              <n-icon :component="HomeOutline" />
            </template>
            返回首页
          </n-button>
        </template>
      </n-result>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f8fafc;
}

html.dark .error-page {
  background: #0f172a;
}

.error-bg {
  position: absolute;
  inset: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(239,68,68,0.2), transparent 70%);
  top: 20%;
  left: 10%;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(20,184,166,0.2), transparent 70%);
  bottom: 20%;
  right: 15%;
}
</style>