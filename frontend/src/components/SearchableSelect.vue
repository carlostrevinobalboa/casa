<template>
  <div ref="root" class="relative">
    <button
      type="button"
      class="flex w-full items-center justify-between gap-2 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm transition hover:border-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900/20 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-400"
      :id="props.id"
      :disabled="disabled"
      @click="toggle"
    >
      <span class="truncate">{{ selectedLabel || placeholder }}</span>
      <span class="text-slate-400">v</span>
    </button>

    <div
      v-if="open"
      class="absolute z-50 mt-1 w-full rounded-xl border border-slate-200 bg-white shadow-lg"
    >
      <div v-if="searchable" class="border-b border-slate-100 p-2">
        <input
          v-model="query"
          type="text"
          class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
          placeholder="Buscar..."
        />
      </div>

      <ul class="max-h-60 overflow-auto py-1 text-sm">
        <li v-if="filteredOptions.length === 0" class="px-3 py-2 text-slate-500">
          Sin resultados
        </li>
        <li
          v-for="option in filteredOptions"
          :key="option.value"
          class="flex cursor-pointer items-center justify-between px-3 py-2 transition hover:bg-slate-50"
          :class="option.value === modelValue ? 'bg-slate-100' : ''"
          @click="selectOption(option.value)"
        >
          <span class="truncate">{{ option.label }}</span>
          <span v-if="option.value === modelValue" class="text-slate-700">OK</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";

interface SelectOption {
  value: string;
  label: string;
}

const props = withDefaults(defineProps<{
  id?: string;
  modelValue: string | null;
  options: SelectOption[];
  placeholder?: string;
  disabled?: boolean;
  searchable?: boolean;
}>(), {
  placeholder: "Selecciona...",
  disabled: false,
  searchable: true
});

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

const root = ref<HTMLElement | null>(null);
const open = ref(false);
const query = ref("");

const selectedLabel = computed(() =>
  props.options.find((option) => option.value === props.modelValue)?.label ?? ""
);

const filteredOptions = computed(() => {
  if (!props.searchable || !query.value.trim()) {
    return props.options;
  }
  const needle = query.value.trim().toLowerCase();
  return props.options.filter((option) => option.label.toLowerCase().includes(needle));
});

const selectOption = (value: string) => {
  emit("update:modelValue", value);
  open.value = false;
  query.value = "";
};

const toggle = () => {
  if (props.disabled) return;
  open.value = !open.value;
};

const handleClickOutside = (event: MouseEvent) => {
  if (!root.value) return;
  if (!root.value.contains(event.target as Node)) {
    open.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>
