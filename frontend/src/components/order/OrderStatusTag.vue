<template>
  <el-tag :type="tagType" size="small">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ORDER_STATUS, ORDER_STATUS_TAG_TYPE } from '@/utils/constants'

const props = defineProps<{
  status: string
}>()

const label = computed(() => ORDER_STATUS[props.status] || props.status)
const tagType = computed(() => (ORDER_STATUS_TAG_TYPE[props.status] || 'info') as string)
</script>
