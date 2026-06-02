<template>
  <div class="guide-page">
    <div class="guide-hero">
      <h1>交易指南</h1>
      <p>安全、诚信、便捷 — 让你的校园二手交易更放心</p>
    </div>

    <div class="guide-content">
      <!-- 交易流程 -->
      <section class="guide-section">
        <h2><span class="section-number">01</span> 交易流程</h2>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon">
              <el-icon :size="28"><Search /></el-icon>
            </div>
            <h4>浏览商品</h4>
            <p>在首页或分类页面浏览商品，使用搜索和筛选功能快速找到心仪的好物。</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-icon">
              <el-icon :size="28"><ChatDotRound /></el-icon>
            </div>
            <h4>联系卖家</h4>
            <p>点击商品详情页的"联系卖家"按钮，与卖家在线沟通，了解商品成色、使用时长等细节。</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-icon">
              <el-icon :size="28"><View /></el-icon>
            </div>
            <h4>当面验货</h4>
            <p>约在校园内的公共场所见面，仔细检查商品实物是否与描述一致，满意后再确认购买。</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-icon">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <h4>完成交易</h4>
            <p>一手交钱一手交货，交易完成后可在平台确认收货，为卖家留下真实评价。</p>
          </div>
        </div>
      </section>

      <!-- 安全提示 -->
      <section class="guide-section">
        <h2><span class="section-number">02</span> 安全提示</h2>
        <div class="tips-grid">
          <div class="tip-card">
            <el-icon :size="24" color="#f59e0b"><WarningFilled /></el-icon>
            <div>
              <h4>选择当面交易</h4>
              <p>优先选择校园内当面交易，避免线上转账。看到实物再付款，确保商品与描述相符。</p>
            </div>
          </div>
          <div class="tip-card">
            <el-icon :size="24" color="#f59e0b"><WarningFilled /></el-icon>
            <div>
              <h4>核实商品信息</h4>
              <p>购买前仔细查看商品图片和描述，关注价格是否合理。价格过低往往暗藏风险。</p>
            </div>
          </div>
          <div class="tip-card">
            <el-icon :size="24" color="#f59e0b"><WarningFilled /></el-icon>
            <div>
              <h4>不要提前付款</h4>
              <p>切勿在收到商品前支付任何定金或全款。正规交易无需提前付款，警惕任何要求预付的理由。</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 见面安全 -->
      <section class="guide-section">
        <h2><span class="section-number">03</span> 见面安全</h2>
        <ul class="safety-list">
          <li>
            <strong>选对地点：</strong>优先选择图书馆大厅、食堂、教学楼走廊、宿舍楼下等人员密集、有监控的校内公共场所。避免前往偏僻地点或对方指定的陌生场所。
          </li>
          <li>
            <strong>选对时间：</strong>尽量在白天交易，避免深夜单独赴约。如果只能晚上见面，最好找同学结伴同行。
          </li>
          <li>
            <strong>告知他人：</strong>出发前把见面时间、地点告知室友或朋友，保持手机通讯畅通。
          </li>
          <li>
            <strong>结伴同行：</strong>如果是价格较高的商品，建议叫上同学一起前往，多一个人多一份安心。
          </li>
        </ul>
      </section>

      <!-- 支付方式 -->
      <section class="guide-section">
        <h2><span class="section-number">04</span> 支付建议</h2>
        <div class="pay-cards">
          <div class="pay-card">
            <h4>💰 现金</h4>
            <p>最传统、最直观的方式，适合小额交易。当面清点，避免找零纠纷。</p>
          </div>
          <div class="pay-card">
            <h4>📱 微信 / 支付宝</h4>
            <p>适用于中高金额交易，扫码即付，有转账记录可查。注意确认收款方身份。</p>
          </div>
          <div class="pay-card warn">
            <h4>⚠️ 不推荐的方式</h4>
            <p>拒绝任何形式的链接付款、二维码转账或第三方平台付款。只在验货后当面支付。</p>
          </div>
        </div>
      </section>

      <!-- 纠纷处理 -->
      <section class="guide-section">
        <h2><span class="section-number">05</span> 纠纷处理</h2>
        <p class="section-intro">交易中难免遇到分歧，保持冷静、友好沟通是解决问题的第一步。</p>
        <div class="dispute-steps">
          <div class="dispute-step">
            <span class="ds-num">1</span>
            <div>
              <h4>友好协商</h4>
              <p>先通过聊天功能与对方沟通，说明问题并表达诉求。大多数问题都可通过协商解决。</p>
            </div>
          </div>
          <div class="dispute-step">
            <span class="ds-num">2</span>
            <div>
              <h4>保留证据</h4>
              <p>保存聊天记录、商品照片、转账截图等证据，以便必要时提供给平台客服协助处理。</p>
            </div>
          </div>
          <div class="dispute-step">
            <span class="ds-num">3</span>
            <div>
              <h4>联系客服</h4>
              <p>如协商无果，通过页面底部的"联系我们"联系平台客服，我们会尽力协助双方化解纠纷。</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 买家须知 -->
      <section class="guide-section">
        <h2><span class="section-number">06</span> 买家须知</h2>
        <ul class="check-list">
          <li>下单前仔细阅读商品描述，有疑问先联系卖家确认</li>
          <li>关注商品成色和使用时长，二手物品难免有正常使用痕迹</li>
          <li>见面时当场检查商品功能是否正常（电子产品开机测试、书籍检查缺页等）</li>
          <li>确认无误后再付款，付款后索要交易凭证</li>
          <li>交易完成后及时确认收货并评价，帮助其他同学参考</li>
        </ul>
      </section>

      <!-- 卖家须知 -->
      <section class="guide-section">
        <h2><span class="section-number">07</span> 卖家须知</h2>
        <ul class="check-list">
          <li>如实描述商品成色和瑕疵，诚信是交易的基础</li>
          <li>上传清晰的实物照片，建议多角度拍摄，有瑕疵的地方要特写</li>
          <li>合理定价，可参考同类商品的市场价格</li>
          <li>及时回复买家消息，良好的沟通能促成更多交易</li>
          <li>发布商品后需等待管理员审核，审核通过后才会公开展示</li>
          <li>商品售出后会自动下架，无需手动操作</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search, ChatDotRound, View, CircleCheck, WarningFilled } from '@element-plus/icons-vue'
</script>

<style scoped>
.guide-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px var(--page-padding) 60px;
}

.guide-hero {
  text-align: center;
  padding: 40px 0 32px;
}
.guide-hero h1 {
  font-size: 28px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px;
}
.guide-hero p {
  color: var(--text-muted);
  font-size: var(--text-base);
  margin: 0;
}

.guide-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.guide-section h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--gradient-primary);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
.section-intro {
  color: var(--text-secondary);
  margin: 0 0 16px;
}

/* 交易流程 */
.flow-steps {
  display: flex;
  align-items: flex-start;
  gap: 0;
  background: var(--bg-page);
  border-radius: 12px;
  padding: 24px 16px;
  overflow-x: auto;
}
.flow-step {
  flex: 1;
  min-width: 120px;
  text-align: center;
  padding: 0 8px;
}
.flow-step .step-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.flow-step h4 {
  margin: 0 0 4px;
  font-size: var(--text-sm);
  font-weight: 600;
}
.flow-step p {
  font-size: var(--text-xs);
  color: var(--text-muted);
  line-height: 1.5;
  margin: 0;
}
.flow-arrow {
  color: var(--text-muted);
  font-size: 20px;
  padding-top: 16px;
  flex-shrink: 0;
}

/* 安全提示 */
.tips-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.tip-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: #fffbeb;
  border-radius: 10px;
  border: 1px solid #fde68a;
}
.tip-card h4 {
  margin: 0 0 4px;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
}
.tip-card p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 见面安全 */
.safety-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.safety-list li {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  padding-left: 20px;
  position: relative;
}
.safety-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-primary);
}

/* 支付 */
.pay-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.pay-card {
  padding: 20px;
  background: var(--bg-page);
  border-radius: 10px;
  text-align: center;
}
.pay-card h4 {
  margin: 0 0 6px;
  font-size: var(--text-base);
}
.pay-card p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-muted);
  line-height: 1.5;
}
.pay-card.warn {
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.pay-card.warn p {
  color: #dc2626;
}

/* 纠纷 */
.dispute-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.dispute-step {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}
.ds-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}
.dispute-step h4 {
  margin: 0 0 2px;
  font-size: var(--text-sm);
  font-weight: 600;
}
.dispute-step p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-muted);
  line-height: 1.5;
}

/* 须知 */
.check-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.check-list li {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  padding-left: 20px;
  position: relative;
}
.check-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--el-color-success);
  font-weight: 700;
}

/* 移动端 */
@media (max-width: 768px) {
  .guide-page { padding-top: 20px; }
  .guide-hero { padding: 20px 0 24px; }
  .guide-hero h1 { font-size: 24px; }
  .flow-steps { flex-wrap: wrap; gap: 16px; justify-content: center; }
  .flow-arrow { display: none; }
  .flow-step { min-width: 40%; padding: 0; }
  .pay-cards { grid-template-columns: 1fr; }
}
</style>
