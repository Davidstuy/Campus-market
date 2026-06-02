import { ref, type Ref } from 'vue'
import { nextTick } from 'vue'

/**
 * Emoji 插入工具 composable
 * 用法：
 *   const { insertEmoji } = useEmojiInsert(inputRef)
 *   <EmojiPicker @select="insertEmoji" />
 */
export function useEmojiInsert(modelRef: Ref<string>) {
  const insertEmoji = (emoji: string) => {
    const el = document.activeElement as HTMLTextAreaElement | HTMLInputElement | null
    if (el && (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT')) {
      const start = el.selectionStart ?? modelRef.value.length
      const end = el.selectionEnd ?? modelRef.value.length
      const text = modelRef.value
      modelRef.value = text.substring(0, start) + emoji + text.substring(end)
      nextTick(() => {
        el.selectionStart = el.selectionEnd = start + emoji.length
        el.focus()
      })
    } else {
      modelRef.value += emoji
    }
  }

  return { insertEmoji }
}
