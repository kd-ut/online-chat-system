<template>
  <div class="empty-container">
    <div class="empty-content">
      <div class="empty-illustration">
        <div class="empty-icon" v-if="icon">
          <component :is="icon" :size="size" />
        </div>
        <div v-else class="empty-default-icon">
          <svg width="80" height="80" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="40" cy="40" r="36" fill="var(--color-primary-light-1)" />
            <circle cx="40" cy="40" r="36" fill="var(--color-primary)" opacity="0.04" />
            <rect x="22" y="32" width="36" height="24" rx="6" fill="var(--color-primary-light-3)" />
            <circle cx="34" cy="44" r="3" fill="var(--color-primary)" opacity="0.5" />
            <circle cx="40" cy="44" r="3" fill="var(--color-primary)" opacity="0.5" />
            <circle cx="46" cy="44" r="3" fill="var(--color-primary)" opacity="0.5" />
          </svg>
        </div>
      </div>
      <div class="empty-title">{{ title }}</div>
      <div v-if="description" class="empty-description">{{ description }}</div>
      <div v-if="$slots.default" class="empty-action">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 空状态占位组件 @component */
withDefaults(defineProps<{
  title?: string
  description?: string
  icon?: any
  size?: number
}>(), {
  title: '暂无数据',
  size: 40
})
</script>

<style scoped>
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-content {
  text-align: center;
  animation: fadeInUp 0.4s ease;
}

.empty-illustration {
  margin-bottom: 20px;
}

.empty-default-icon {
  display: inline-block;
  opacity: 0.7;
  transition: transform 0.3s ease;
}

.empty-container:hover .empty-default-icon {
  transform: scale(1.05);
}

.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.empty-description {
  font-size: 13px;
  color: var(--text-placeholder);
  margin-top: 4px;
}

.empty-action {
  margin-top: 20px;
}
</style>
