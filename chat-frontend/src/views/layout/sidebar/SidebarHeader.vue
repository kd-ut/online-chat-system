<template>
  <div class="sidebar-header">
    <div class="logo-wrap">
      <svg class="logo-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="20" cy="20" r="18" stroke="var(--color-primary)" stroke-width="2" opacity="0.25" />
        <circle cx="20" cy="20" r="12" stroke="var(--color-primary)" stroke-width="1.5" opacity="0.12" />
        <path d="M12 18 Q20 30 28 18" stroke="var(--color-primary)" stroke-width="2" stroke-linecap="round" fill="none" opacity="0.8" />
        <circle cx="15" cy="16" r="2" fill="var(--color-primary)" opacity="0.8" />
        <circle cx="25" cy="16" r="2" fill="var(--color-primary)" opacity="0.8" />
      </svg>
      <div class="logo-text">
        <h3>闪聊</h3>
        <span class="logo-sub">FlashChat</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sidebar-header {
  padding: 16px 18px 14px;
  display: flex;
  align-items: center;
  height: var(--header-height);
  border-bottom: 1px solid var(--border-color);
}

.logo-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-text h3 {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
  line-height: 1.2;
}

.logo-sub {
  font-size: 10px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1.5px;
  font-weight: 500;
}
</style>
