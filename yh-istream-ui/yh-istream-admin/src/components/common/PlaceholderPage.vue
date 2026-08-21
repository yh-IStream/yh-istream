<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<template>
  <div class="card">
    <div class="flex items-center gap-4px mb-16px">
      <div class="w-4px h-18px rounded-2px bg-primary" />
      <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100 m-0">{{ title }}</h3>
    </div>
    <n-empty description="功能开发中..." class="py-48px" />
  </div>
</template>