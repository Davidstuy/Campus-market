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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}
.auth-title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
}
</style>
