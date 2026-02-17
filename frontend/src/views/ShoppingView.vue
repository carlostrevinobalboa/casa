<template>
  <section class="space-y-4">
    <div>
      <h2 class="text-xl font-semibold">Compra inteligente</h2>
      <p class="mt-1 text-sm text-slate-600">
        Productos faltantes desde recetas y despensa, más altas manuales.
      </p>
    </div>

    <form class="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-5" @submit.prevent="addManualItem">
      <div class="md:col-span-2">
        <label for="shopping-product-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Producto
        </label>
        <input
          id="shopping-product-name"
          v-model="form.productName"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="Ej: Leche"
        />
      </div>

      <div>
        <label for="shopping-quantity" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Cantidad
        </label>
        <input
          id="shopping-quantity"
          v-model.number="form.quantity"
          required
          min="0.01"
          step="0.01"
          type="number"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
        />
      </div>

      <div>
        <label for="shopping-unit" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Unidad
        </label>
        <input
          id="shopping-unit"
          v-model="form.unit"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="ud"
        />
      </div>

      <div>
        <label for="shopping-category" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Categoría
        </label>
        <input
          id="shopping-category"
          v-model="form.category"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="GENERAL"
        />
      </div>

      <button
        type="submit"
        class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700"
      >
        Añadir a compra
      </button>
    </form>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

    <div class="overflow-x-auto">
      <table class="min-w-full border-collapse text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-left text-slate-500">
            <th class="px-3 py-2">Producto</th>
            <th class="px-3 py-2">Cantidad</th>
            <th class="px-3 py-2">Categoría</th>
            <th class="px-3 py-2">Origen</th>
            <th class="px-3 py-2">Estado</th>
            <th class="px-3 py-2">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id" class="border-b border-slate-100">
            <td class="px-3 py-2 font-medium text-slate-900">{{ item.productName }}</td>
            <td class="px-3 py-2">{{ item.quantity }} {{ item.unit }}</td>
            <td class="px-3 py-2">{{ item.category }}</td>
            <td class="px-3 py-2">{{ sourceLabel(item.sourceType) }}</td>
            <td class="px-3 py-2">
              <span
                class="rounded px-2 py-1 text-xs font-medium"
                :class="item.purchased ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'"
              >
                {{ item.purchased ? "Comprado" : "Pendiente" }}
              </span>
            </td>
            <td class="px-3 py-2">
              <button
                v-if="!item.purchased"
                type="button"
                class="rounded border border-emerald-300 px-2 py-1 text-xs text-emerald-700 hover:bg-emerald-50"
                @click="setPurchased(item.id, true)"
              >
                Marcar comprado
              </button>
              <button
                v-else
                type="button"
                class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
                @click="setPurchased(item.id, false)"
              >
                Volver a pendiente
              </button>
            </td>
          </tr>
          <tr v-if="items.length === 0">
            <td colspan="6" class="px-3 py-6 text-center text-slate-500">No hay items en la lista de compra.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import type { ShoppingListItem, ShoppingListItemRequest } from "../types";

const session = useSessionStore();

const items = ref<ShoppingListItem[]>([]);
const errorMessage = ref("");

const form = reactive<ShoppingListItemRequest>({
  productName: "",
  quantity: 1,
  unit: "ud",
  category: "GENERAL"
});

const householdId = () => session.activeHouseholdId;

const sourceLabel = (source: ShoppingListItem["sourceType"]) => {
  if (source === "MANUAL") return "Manual";
  if (source === "RECIPE_SHORTAGE") return "Faltante receta";
  return "Stock mínimo";
};

const load = async () => {
  const id = householdId();
  if (!id) {
    items.value = [];
    return;
  }

  const { data } = await api.get<ShoppingListItem[]>(`/api/households/${id}/shopping-list-items`);
  items.value = data;
};

const addManualItem = async () => {
  const id = householdId();
  if (!id) return;

  errorMessage.value = "";
  try {
    await api.post(`/api/households/${id}/shopping-list-items`, form);
    form.productName = "";
    form.quantity = 1;
    form.unit = "ud";
    form.category = "GENERAL";
    await load();
  } catch {
    errorMessage.value = "No se pudo añadir el producto a compra.";
  }
};

const setPurchased = async (itemId: string, purchased: boolean) => {
  const id = householdId();
  if (!id) return;

  const action = purchased ? "purchase" : "unpurchase";
  await api.post(`/api/households/${id}/shopping-list-items/${itemId}/${action}`);
  await load();
};

watch(
  () => session.activeHouseholdId,
  () => {
    void load();
  },
  { immediate: true }
);
</script>
