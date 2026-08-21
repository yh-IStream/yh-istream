<script setup lang="ts">
import { HomeOutline } from '@vicons/ionicons5'

const router = useRouter()
</script>

<template>
  <div class="wh-full flex-col-center gap-16px">
    <div class="text-120px font-bold text-gray-200 dark:text-gray-700 select-none">
      404
    </div>
    <h2 class="text-xl text-gray-600 dark:text-gray-400 m-0">
      页面不存在
    </h2>
    <p class="text-sm text-gray-400 dark:text-gray-500 m-0">
      你访问的页面可能已被移除或暂时不可用
    </p>
    <NButton type="primary" @click="router.push('/dashboard')">
      <template #icon>
        <NIcon :component="HomeOutline" />
      </template>
      返回首页
    </NButton>
  </div>
</template>