<script setup lang="ts">
defineProps<{
  items: { value: string; label: string }[]
}>()
const model = defineModel<string>({ required: true })
</script>

<template>
  <view class="chips">
    <view
      v-for="item in items"
      :key="item.value"
      class="chip"
      :class="{ 'is-on': model === item.value }"
      @click="model = item.value"
    >
      {{ item.label }}
    </view>
  </view>
</template>
