<template>
  <section class="space-y-4">
    <div>
      <h2 class="text-xl font-semibold">Despensa</h2>
      <p class="mt-1 text-sm text-slate-600">Control de stock minimo y caducidades por hogar.</p>
    </div>

    <form class="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-6" @submit.prevent="createItem">
      <div class="md:col-span-2">
        <label for="pantry-product-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Producto
        </label>
        <input
          id="pantry-product-name"
          v-model="form.productName"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="Ej: Leche"
        />
      </div>

      <div>
        <label for="pantry-current-quantity" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Cantidad actual
        </label>
        <input
          id="pantry-current-quantity"
          v-model.number="form.currentQuantity"
          required
          min="0"
          step="0.01"
          type="number"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="0"
        />
      </div>

      <div>
        <label for="pantry-minimum-quantity" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Stock minimo
        </label>
        <input
          id="pantry-minimum-quantity"
          v-model.number="form.minimumQuantity"
          required
          min="0"
          step="0.01"
          type="number"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="0"
        />
      </div>

      <div>
        <label for="pantry-unit" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Unidad
        </label>
        <input
          id="pantry-unit"
          v-model="form.unit"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="ud / kg / L"
        />
      </div>

      <div>
        <label for="pantry-expiration-date" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Fecha de caducidad
        </label>
        <input
          id="pantry-expiration-date"
          v-model="form.expirationDate"
          type="date"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
        />
      </div>

      <div class="md:col-span-2">
        <label for="pantry-category" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Categoria
        </label>
        <input
          id="pantry-category"
          v-model="form.category"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="Ej: LACTEOS"
        />
      </div>

      <button
        type="submit"
        class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700"
      >
        Anadir
      </button>
    </form>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
      {{ errorMessage }}
    </p>

    <div class="overflow-x-auto">
      <table class="min-w-full border-collapse text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-left text-slate-500">
            <th class="px-3 py-2">Producto</th>
            <th class="px-3 py-2">Cantidad</th>
            <th class="px-3 py-2">Minimo</th>
            <th class="px-3 py-2">Caducidad</th>
            <th class="px-3 py-2">Estado</th>
            <th class="px-3 py-2">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id" class="border-b border-slate-100">
            <td class="px-3 py-2 font-medium text-slate-900">{{ item.productName }}</td>
            <td class="px-3 py-2">{{ item.currentQuantity }} {{ item.unit }}</td>
            <td class="px-3 py-2">{{ item.minimumQuantity }} {{ item.unit }}</td>
            <td class="px-3 py-2">{{ item.expirationDate ?? '-' }}</td>
            <td class="px-3 py-2">
              <span v-if="item.belowMinimum" class="rounded bg-amber-100 px-2 py-1 text-xs font-medium text-amber-700">
                Bajo minimo
              </span>
              <span v-if="item.expiresSoon" class="ml-1 rounded bg-red-100 px-2 py-1 text-xs font-medium text-red-700">
                Caduca pronto
              </span>
              <span v-if="!item.belowMinimum && !item.expiresSoon" class="text-slate-500">OK</span>
            </td>
            <td class="px-3 py-2">
              <div class="flex flex-wrap gap-2">
                <button
                  type="button"
                  class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
                  @click="adjustQuantity(item, -1)"
                >
                  -1
                </button>
                <button
                  type="button"
                  class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
                  @click="adjustQuantity(item, 1)"
                >
                  +1
                </button>
                <button
                  type="button"
                  class="rounded border border-red-300 px-2 py-1 text-xs text-red-700 hover:bg-red-50"
                  @click="removeItem(item.id)"
                >
                  Eliminar
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="items.length === 0">
            <td colspan="6" class="px-3 py-6 text-center text-slate-500">No hay productos todavia.</td>
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
import type { PantryItem, PantryItemRequest } from "../types";

const session = useSessionStore();

const items = ref<PantryItem[]>([]);
const errorMessage = ref("");

const form = reactive<PantryItemRequest>({
  productName: "",
  currentQuantity: 0,
  minimumQuantity: 0,
  unit: "ud",
  expirationDate: null,
  category: "GENERAL"
});

const householdId = () => session.activeHouseholdId;

const load = async () => {
  errorMessage.value = "";
  const id = householdId();
  if (!id) {
    items.value = [];
    return;
  }

  const { data } = await api.get<PantryItem[]>(`/api/households/${id}/pantry-items`);
  items.value = data;
};

const createItem = async () => {
  const id = householdId();
  if (!id) {
    return;
  }

  try {
    await api.post<PantryItem>(`/api/households/${id}/pantry-items`, form);
    form.productName = "";
    form.currentQuantity = 0;
    form.minimumQuantity = 0;
    form.unit = "ud";
    form.expirationDate = null;
    form.category = "GENERAL";
    await load();
  } catch {
    errorMessage.value = "No se pudo crear el producto.";
  }
};

const adjustQuantity = async (item: PantryItem, delta: number) => {
  const id = householdId();
  if (!id) {
    return;
  }

  const payload: PantryItemRequest = {
    productName: item.productName,
    currentQuantity: Math.max(0, Number((item.currentQuantity + delta).toFixed(2))),
    minimumQuantity: item.minimumQuantity,
    unit: item.unit,
    expirationDate: item.expirationDate,
    category: item.category
  };

  await api.put(`/api/households/${id}/pantry-items/${item.id}`, payload);
  await load();
};

const removeItem = async (itemId: string) => {
  const id = householdId();
  if (!id) {
    return;
  }

  await api.delete(`/api/households/${id}/pantry-items/${itemId}`);
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
