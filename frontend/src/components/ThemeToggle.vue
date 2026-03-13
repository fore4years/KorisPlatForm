<template>
  <el-button 
    class="theme-toggle" 
    :icon="isDark ? Moon : Sunny" 
    circle 
    @click="toggleTheme"
    :title="isDark ? '切换亮色模式' : '切换暗色模式'"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useDark, useToggle } from '@vueuse/core'

// We don't have vueuse installed, let's implement manually for now to avoid dependency issues
const isDark = ref(false)

const toggleTheme = () => {
  isDark.value = !isDark.value
  updateTheme()
}

const updateTheme = () => {
  const html = document.documentElement
  if (isDark.value) {
    html.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    html.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}

onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDark.value = true
  }
  updateTheme()
})
</script>

<style scoped>
.theme-toggle {
  margin-right: 12px;
  font-size: 18px;
}
</style>
