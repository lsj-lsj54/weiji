<script setup lang="ts">
defineProps<{
  items: { value: string; label: string }[]
}>()
const model = defineModel<string>({ required: true })
</script>

<template>
  <div class="chips" role="tablist">
    <button
      v-for="item in items"
      :key="item.value"
      class="chip"
      :class="{ 'is-on': model === item.value }"
      type="button"
      role="tab"
      :aria-selected="model === item.value"
      @click="model = item.value"
    >
      {{ item.label }}
    </button>
  </div>
</template>
