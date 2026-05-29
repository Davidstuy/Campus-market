import { ref, onMounted, onUnmounted } from 'vue'

export function useScrollObserver(threshold = 10) {
  const isScrolled = ref(false)

  const onScroll = () => {
    isScrolled.value = window.scrollY > threshold
  }

  onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
  onUnmounted(() => window.removeEventListener('scroll', onScroll))

  return { isScrolled }
}
