<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <h1 class="auth-title">校园二手交易</h1>
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-auth);
  position: relative;
  overflow: hidden;
}

/* 装饰性光斑 */
.auth-wrapper::before,
.auth-wrapper::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.auth-wrapper::before {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.4) 0%, transparent 70%);
  top: -100px;
  right: -100px;
}
.auth-wrapper::after {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.35) 0%, transparent 70%);
  bottom: -80px;
  left: -80px;
}

.auth-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: var(--radius-xl);
  padding: 40px;
  width: 420px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-auth);
  position: relative;
  z-index: 1;
}

.auth-title {
  text-align: center;
  font-size: var(--text-3xl);
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 32px;
  letter-spacing: 0.5px;
}

@media (max-width: 480px) {
  .auth-wrapper::before,
  .auth-wrapper::after {
    display: none;
  }
  .auth-card {
    width: calc(100% - 32px);
    padding: 24px;
  }
}
</style>
