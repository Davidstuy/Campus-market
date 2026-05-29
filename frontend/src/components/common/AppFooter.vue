<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-col">
        <div class="footer-logo">校园二手交易</div>
        <p class="footer-desc">安全、便捷的校园二手交易平台</p>
      </div>
      <div class="footer-col">
        <h4>快速链接</h4>
        <router-link to="/products">浏览商品</router-link>
        <router-link to="/publish">发布商品</router-link>
      </div>
      <div class="footer-col">
        <h4>帮助</h4>
        <a href="#">交易指南</a>
        <a href="#">常见问题</a>
        <a href="#">联系我们</a>
      </div>
    </div>
    <div class="footer-bottom">
      <span>&copy; 2026 校园二手交易平台</span>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--bg-white);
  border-top: 1px solid var(--border-color);
  margin-top: 80px;
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 40px var(--page-padding) 24px;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
}

.footer-logo {
  font-size: 18px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  display: inline-block;
}

.footer-desc {
  color: var(--text-muted);
  font-size: var(--text-sm);
  line-height: 1.6;
  max-width: 280px;
}

.footer-col h4 {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.footer-col a {
  display: block;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: 8px;
  transition: color var(--transition-fast);
  text-decoration: none;
}
.footer-col a:hover {
  color: var(--el-color-primary);
}

.footer-bottom {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 16px var(--page-padding);
  border-top: 1px solid var(--border-color);
  text-align: center;
  color: var(--text-muted);
  font-size: var(--text-xs);
}

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>
