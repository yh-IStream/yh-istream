<script setup lang="ts">
import { HomeOutline } from '@vicons/ionicons5'

const router = useRouter()
</script>

<template>
  <div class="wh-full flex-col-center gap-16px">
    <div class="text-120px font-bold text-gray-200 dark:text-gray-700 select-none">
      403
    </div>
    <h2 class="text-xl text-gray-600 dark:text-gray-400 m-0">
      无访问权限
    </h2>
    <p class="text-sm text-gray-400 dark:text-gray-500 m-0">
      你没有权限访问此页面，请联系管理员
    </p>
    <n-button type="primary" @click="router.push('/dashboard')">
      <template #icon>
        <n-icon :component="HomeOutline" />
      </template>
      返回首页
    </n-button>
  </div>
</template>