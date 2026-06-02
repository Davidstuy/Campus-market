<template>
  <el-popover
    trigger="click"
    placement="top"
    :width="300"
    :hide-after="0"
  >
    <template #reference>
      <slot name="trigger">
        <el-button size="small" circle :title="title">
          <span class="emoji-btn-icon">😊</span>
        </el-button>
      </slot>
    </template>
    <div class="emoji-grid">
      <button
        v-for="e in emojis"
        :key="e"
        class="emoji-item"
        @click="emit('select', e)"
      >{{ e }}</button>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
defineProps<{ title?: string }>()
const emit = defineEmits<{ select: [emoji: string] }>()

const emojis = [
  '😀','😂','🤣','😍','🥰','😘','😜','🤪','😎','🤩',
  '👍','👎','👏','🙌','💪','🤝','🙏','❤️','💔','🔥',
  '⭐','✨','🎉','🎊','🌸','🌺','🍀','🌈','☀️','🌙',
  '🐱','🐶','🐼','🐨','🦊','🐰','🐸','🦄','🐙','🐳',
  '🍕','🍔','🍟','🍩','🍰','🍉','🍓','🍺','☕','🧋',
  '🎮','🎵','🎬','📚','✏️','💡','💰','🎁','📷','📱',
  '💯','✅','❌','❓','💤','💢','👀','🫶','🤔','😅',
]
</script>

<style scoped>
.emoji-btn-icon {
  font-size: 18px;
  line-height: 1;
}
.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px;
}
.emoji-item {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 6px;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.emoji-item:hover {
  background: #f0f0ff;
  transform: scale(1.2);
}
</style>
