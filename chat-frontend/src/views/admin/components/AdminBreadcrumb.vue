<template>
  <div class="admin-breadcrumb">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item>管理后台</el-breadcrumb-item>
      <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
/** 管理后台面包屑导航组件 @component */
import { HomeFilled } from '@element-plus/icons-vue'

defineProps<{ currentTitle: string }>()
</script>

<style scoped>
.admin-breadcrumb {
  padding: 8px 0 16px;
}

.admin-breadcrumb :deep(.el-breadcrumb) {
  font-size: 13px;
}

.admin-breadcrumb :deep(.el-breadcrumb__item) {
  display: inline-flex;
  align-items: center;
}

.admin-breadcrumb :deep(.el-breadcrumb__inner) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  font-weight: 500;
  transition: color 0.2s;
}

.admin-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--color-primary);
}

.admin-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 600;
}

.admin-breadcrumb :deep(.el-breadcrumb__separator) {
  color: var(--text-placeholder);
  margin: 0 6px;
}
</style>
