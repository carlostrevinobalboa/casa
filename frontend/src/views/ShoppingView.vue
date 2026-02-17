<template>
  <section class="space-y-4">
    <div>
      <h2 class="text-xl font-semibold">Compra inteligente</h2>
      <p class="mt-1 text-sm text-slate-600">
        Productos faltantes desde recetas y despensa, mas altas manuales.
      </p>
    </div>

    <form class="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-5" @submit.prevent="addManualItem">
      <div class="md:col-span-2">
        <label for="shopping-product-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Producto
        </label>
        <select
          id="shopping-product-name"
          v-model="form.productName"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          @change="onProductChange(form.productName)"
        >
          <option value="" disabled>Selecciona un producto</option>
          <option v-for="product in catalogProducts" :key="product.id" :value="product.name">
            {{ product.name }}
          </option>
        </select>
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
        <select
          id="shopping-unit"
          v-model="form.unit"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="" disabled>Selecciona unidad</option>
          <option v-for="unit in catalogUnits" :key="unit.id" :value="unit.code">
            {{ unit.code }} - {{ unit.label }}
          </option>
        </select>
      </div>

      <div>
        <label for="shopping-category" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Categoria
        </label>
        <select
          id="shopping-category"
          v-model="form.category"
          required
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="" disabled>Selecciona categoria</option>
          <option v-for="category in catalogCategories" :key="category.id" :value="category.name">
            {{ category.name }}
          </option>
        </select>
      </div>

      <button
        type="submit"
        :disabled="!canCreateShoppingItem"
        class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white enabled:hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
      >
        Anadir a compra
      </button>
    </form>

    <p v-if="!canCreateShoppingItem" class="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
      Configura productos, unidades y categorias en Admin para poder anadir items de compra.
    </p>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

    <div class="overflow-x-auto">
      <table class="min-w-full border-collapse text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-left text-slate-500">
            <th class="px-3 py-2">Producto</th>
            <th class="px-3 py-2">Cantidad</th>
            <th class="px-3 py-2">Categoria</th>
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
import { computed, reactive, ref, watch } from "vue";
import { api } from "../lib/api";
import { fetchHouseholdCatalog, findProductInCatalog } from "../lib/catalog";
import { useSessionStore } from "../stores/session";
import type {
  CatalogCategory,
  CatalogProduct,
  CatalogUnit,
  ShoppingListItem,
  ShoppingListItemRequest
} from "../types";

const session = useSessionStore();

const items = ref<ShoppingListItem[]>([]);
const catalogUnits = ref<CatalogUnit[]>([]);
const catalogCategories = ref<CatalogCategory[]>([]);
const catalogProducts = ref<CatalogProduct[]>([]);
const errorMessage = ref("");

const form = reactive<ShoppingListItemRequest>({
  productName: "",
  quantity: 1,
  unit: "ud",
  category: "GENERAL"
});

const householdId = () => session.activeHouseholdId;
const defaultUnitCode = () => catalogUnits.value[0]?.code ?? "ud";
const defaultCategoryName = () => catalogCategories.value[0]?.name ?? "GENERAL";
const defaultProductName = () => catalogProducts.value[0]?.name ?? "";
const canCreateShoppingItem = computed(() =>
  catalogProducts.value.length > 0
  && catalogUnits.value.length > 0
  && catalogCategories.value.length > 0
);

const sourceLabel = (source: ShoppingListItem["sourceType"]) => {
  if (source === "MANUAL") return "Manual";
  if (source === "RECIPE_SHORTAGE") return "Faltante receta";
  return "Stock minimo";
};

const loadCatalog = async () => {
  const id = householdId();
  if (!id) {
    catalogUnits.value = [];
    catalogCategories.value = [];
    catalogProducts.value = [];
    return;
  }

  try {
    const catalog = await fetchHouseholdCatalog(id);
    catalogUnits.value = catalog.units.filter((unit) => unit.active);
    catalogCategories.value = catalog.categories.filter((category) => category.active);
    catalogProducts.value = catalog.products.filter((product) => product.active);

    if (!catalogUnits.value.some((unit) => unit.code === form.unit)) {
      form.unit = defaultUnitCode();
    }
    if (!catalogCategories.value.some((category) => category.name === form.category)) {
      form.category = defaultCategoryName();
    }
    if (!catalogProducts.value.some((product) => product.name === form.productName)) {
      form.productName = defaultProductName();
      onProductChange(form.productName);
    }
  } catch {
    catalogUnits.value = [];
    catalogCategories.value = [];
    catalogProducts.value = [];
  }
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

const onProductChange = (productName: string) => {
  const product = findProductInCatalog(catalogProducts.value, productName);
  if (!product) {
    return;
  }

  if (product.defaultUnitCode) {
    form.unit = product.defaultUnitCode;
  }
  if (product.defaultCategoryName) {
    form.category = product.defaultCategoryName;
  }
};

const addManualItem = async () => {
  const id = householdId();
  if (!id || !canCreateShoppingItem.value) return;

  errorMessage.value = "";
  try {
    onProductChange(form.productName);
    await api.post(`/api/households/${id}/shopping-list-items`, form);
    form.productName = defaultProductName();
    form.quantity = 1;
    form.unit = defaultUnitCode();
    form.category = defaultCategoryName();
    await load();
  } catch {
    errorMessage.value = "No se pudo anadir el producto a compra.";
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
    void Promise.all([loadCatalog(), load()]);
  },
  { immediate: true }
);
</script>
