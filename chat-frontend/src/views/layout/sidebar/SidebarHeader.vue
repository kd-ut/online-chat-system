<template>
  <div class="sidebar-header">
    <div class="logo-wrap">
      <svg class="logo-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="20" cy="20" r="18" stroke="var(--color-primary)" stroke-width="2" opacity="0.3" />
        <circle cx="20" cy="20" r="12" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.15" />
        <path d="M12 18 Q20 30 28 18" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" fill="none" opacity="0.7" />
        <circle cx="15" cy="16" r="2" fill="var(--color-primary)" opacity="0.7" />
        <circle cx="25" cy="16" r="2" fill="var(--color-primary)" opacity="0.7" />
      </svg>
      <h3>闪聊</h3>
    </div>
  </div>
</template>

<style scoped>
.sidebar-header {
  padding: 16px 20px 14px;
  display: flex;
  align-items: center;
  height: var(--header-height);
  border-bottom: 1px solid var(--border-color);
}

.logo-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  opacity: 0.85;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}
</style>
