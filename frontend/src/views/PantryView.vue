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
        <SearchableSelect
          id="pantry-product-name"
          v-model="form.productName"
          :options="productOptions"
          placeholder="Selecciona un producto"
          @update:modelValue="(value) => { form.productName = value; onProductChange(value); }"
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
        <SearchableSelect
          id="pantry-unit"
          v-model="form.unit"
          :options="unitOptions"
          placeholder="Selecciona unidad"
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
        <SearchableSelect
          id="pantry-category"
          v-model="form.category"
          :options="categoryOptions"
          placeholder="Selecciona categoria"
        />
      </div>

      <button
        type="submit"
        :disabled="!canCreateItem"
        class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white enabled:hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
      >
        Anadir
      </button>
    </form>

    <p v-if="!canCreateItem" class="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
      Configura productos, unidades y categorias en Admin para poder anadir items de despensa.
    </p>

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
                  class="inline-flex items-center gap-1 rounded border border-red-300 px-2 py-1 text-xs text-red-700 hover:bg-red-50"
                  @click="removeItem(item.id)"
                >
                  <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 6h18" />
                    <path d="M8 6V4h8v2" />
                    <path d="M6 6l1 14h10l1-14" />
                    <path d="M10 11v6" />
                    <path d="M14 11v6" />
                  </svg>
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
import { computed, reactive, ref, watch } from "vue";
import { api } from "../lib/api";
import { fetchHouseholdCatalog, findProductInCatalog } from "../lib/catalog";
import { useSessionStore } from "../stores/session";
import SearchableSelect from "../components/SearchableSelect.vue";
import type {
  CatalogCategory,
  CatalogProduct,
  CatalogUnit,
  PantryItem,
  PantryItemRequest
} from "../types";

const session = useSessionStore();

const items = ref<PantryItem[]>([]);
const catalogUnits = ref<CatalogUnit[]>([]);
const catalogCategories = ref<CatalogCategory[]>([]);
const catalogProducts = ref<CatalogProduct[]>([]);
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
const defaultUnitCode = () => catalogUnits.value[0]?.code ?? "ud";
const defaultCategoryName = () => catalogCategories.value[0]?.name ?? "GENERAL";
const defaultProductName = () => catalogProducts.value[0]?.name ?? "";
const canCreateItem = computed(() =>
  catalogProducts.value.length > 0
  && catalogUnits.value.length > 0
  && catalogCategories.value.length > 0
);
const productOptions = computed(() =>
  catalogProducts.value.map((product) => ({ value: product.name, label: product.name }))
);
const unitOptions = computed(() =>
  catalogUnits.value.map((unit) => ({ value: unit.code, label: `${unit.code} - ${unit.label}` }))
);
const categoryOptions = computed(() =>
  catalogCategories.value.map((category) => ({ value: category.name, label: category.name }))
);

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
  errorMessage.value = "";
  const id = householdId();
  if (!id) {
    items.value = [];
    return;
  }

  const { data } = await api.get<PantryItem[]>(`/api/households/${id}/pantry-items`);
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

const createItem = async () => {
  const id = householdId();
  if (!id || !canCreateItem.value) {
    return;
  }

  try {
    onProductChange(form.productName);
    await api.post<PantryItem>(`/api/households/${id}/pantry-items`, form);
    form.productName = defaultProductName();
    form.currentQuantity = 0;
    form.minimumQuantity = 0;
    form.unit = defaultUnitCode();
    form.expirationDate = null;
    form.category = defaultCategoryName();
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
    void Promise.all([loadCatalog(), load()]);
  },
  { immediate: true }
);
</script>
