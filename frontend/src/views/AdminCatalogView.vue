<template>
  <section class="space-y-6">
    <div>
      <h2 class="text-xl font-semibold">Admin maestro</h2>
      <p class="mt-1 text-sm text-slate-600">
        Gestiona unidades, categorias y productos del hogar para reutilizarlos en despensa, recetas y compra.
      </p>
    </div>

    <p v-if="!isAdmin" class="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
      Tu rol en este hogar es <strong>{{ activeRoleLabel }}</strong>. Solo OWNER o ADMIN pueden editar el maestro.
    </p>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
      {{ errorMessage }}
    </p>
    <p v-if="successMessage" class="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
      {{ successMessage }}
    </p>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <header>
        <h3 class="text-base font-semibold text-slate-900">Unidades</h3>
        <p class="text-sm text-slate-600">Ejemplo: UD, KG, L.</p>
      </header>

      <form class="grid gap-3 md:grid-cols-[120px_1fr_auto_auto]" @submit.prevent="saveUnit">
        <div>
          <label for="unit-code" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Codigo
          </label>
          <input
            id="unit-code"
            v-model="unitForm.code"
            required
            maxlength="20"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="UD"
          />
        </div>

        <div>
          <label for="unit-label" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Nombre
          </label>
          <input
            id="unit-label"
            v-model="unitForm.label"
            required
            maxlength="80"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Unidad"
          />
        </div>

        <label class="mt-6 inline-flex items-center gap-2 text-sm text-slate-700">
          <input v-model="unitForm.active" type="checkbox" class="rounded border-slate-300" />
          Activa
        </label>

        <div class="mt-6 flex gap-2">
          <button
            type="submit"
            :disabled="!isAdmin"
            class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white enabled:hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {{ unitForm.id ? "Actualizar" : "Crear" }}
          </button>
          <button
            v-if="unitForm.id"
            type="button"
            class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
            @click="resetUnitForm"
          >
            Cancelar
          </button>
        </div>
      </form>

      <div class="overflow-x-auto">
        <table class="min-w-full border-collapse text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-slate-500">
              <th class="px-3 py-2">Codigo</th>
              <th class="px-3 py-2">Nombre</th>
              <th class="px-3 py-2">Estado</th>
              <th class="px-3 py-2">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="unit in units" :key="unit.id" class="border-b border-slate-100">
              <td class="px-3 py-2 font-medium text-slate-900">{{ unit.code }}</td>
              <td class="px-3 py-2">{{ unit.label }}</td>
              <td class="px-3 py-2">
                <span
                  class="rounded px-2 py-1 text-xs font-medium"
                  :class="unit.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'"
                >
                  {{ unit.active ? "Activa" : "Inactiva" }}
                </span>
              </td>
              <td class="px-3 py-2">
                <div class="flex gap-2">
                  <button
                    type="button"
                    :disabled="!isAdmin"
                    class="rounded border border-slate-300 px-2 py-1 text-xs enabled:hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                    @click="editUnit(unit)"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    :disabled="!isAdmin"
                    class="rounded border border-red-300 px-2 py-1 text-xs text-red-700 enabled:hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                    @click="deleteUnit(unit.id)"
                  >
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="units.length === 0">
              <td colspan="4" class="px-3 py-6 text-center text-slate-500">No hay unidades registradas.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <header>
        <h3 class="text-base font-semibold text-slate-900">Categorias</h3>
        <p class="text-sm text-slate-600">Ejemplo: GENERAL, LACTEOS, LIMPIEZA.</p>
      </header>

      <form class="grid gap-3 md:grid-cols-[1fr_auto_auto]" @submit.prevent="saveCategory">
        <div>
          <label for="category-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Nombre categoria
          </label>
          <input
            id="category-name"
            v-model="categoryForm.name"
            required
            maxlength="80"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="GENERAL"
          />
        </div>

        <label class="mt-6 inline-flex items-center gap-2 text-sm text-slate-700">
          <input v-model="categoryForm.active" type="checkbox" class="rounded border-slate-300" />
          Activa
        </label>

        <div class="mt-6 flex gap-2">
          <button
            type="submit"
            :disabled="!isAdmin"
            class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white enabled:hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {{ categoryForm.id ? "Actualizar" : "Crear" }}
          </button>
          <button
            v-if="categoryForm.id"
            type="button"
            class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
            @click="resetCategoryForm"
          >
            Cancelar
          </button>
        </div>
      </form>

      <div class="overflow-x-auto">
        <table class="min-w-full border-collapse text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-slate-500">
              <th class="px-3 py-2">Categoria</th>
              <th class="px-3 py-2">Estado</th>
              <th class="px-3 py-2">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in categories" :key="category.id" class="border-b border-slate-100">
              <td class="px-3 py-2 font-medium text-slate-900">{{ category.name }}</td>
              <td class="px-3 py-2">
                <span
                  class="rounded px-2 py-1 text-xs font-medium"
                  :class="category.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'"
                >
                  {{ category.active ? "Activa" : "Inactiva" }}
                </span>
              </td>
              <td class="px-3 py-2">
                <div class="flex gap-2">
                  <button
                    type="button"
                    :disabled="!isAdmin"
                    class="rounded border border-slate-300 px-2 py-1 text-xs enabled:hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                    @click="editCategory(category)"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    :disabled="!isAdmin"
                    class="rounded border border-red-300 px-2 py-1 text-xs text-red-700 enabled:hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                    @click="deleteCategory(category.id)"
                  >
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="categories.length === 0">
              <td colspan="3" class="px-3 py-6 text-center text-slate-500">No hay categorias registradas.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <header>
        <h3 class="text-base font-semibold text-slate-900">Productos</h3>
        <p class="text-sm text-slate-600">Puedes definir unidad y categoria por defecto para autocompletar formularios.</p>
      </header>

      <form class="grid gap-3 md:grid-cols-2" @submit.prevent="saveProduct">
        <div>
          <label for="product-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Nombre producto
          </label>
          <input
            id="product-name"
            v-model="productForm.name"
            required
            maxlength="120"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Leche semidesnatada"
          />
        </div>

        <div>
          <label for="product-default-unit" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Unidad por defecto
          </label>
          <select
            id="product-default-unit"
            v-model="productForm.defaultUnitCode"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          >
            <option value="">Sin unidad por defecto</option>
            <option v-for="unit in units" :key="unit.id" :value="unit.code">
              {{ unit.code }} - {{ unit.label }}
            </option>
          </select>
        </div>

        <div>
          <label for="product-default-category" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Categoria por defecto
          </label>
          <select
            id="product-default-category"
            v-model="productForm.defaultCategoryName"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          >
            <option value="">Sin categoria por defecto</option>
            <option v-for="category in categories" :key="category.id" :value="category.name">
              {{ category.name }}
            </option>
          </select>
        </div>

        <label class="mt-6 inline-flex items-center gap-2 text-sm text-slate-700">
          <input v-model="productForm.active" type="checkbox" class="rounded border-slate-300" />
          Activo
        </label>

        <div class="mt-6 flex gap-2">
          <button
            type="submit"
            :disabled="!isAdmin"
            class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white enabled:hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {{ productForm.id ? "Actualizar" : "Crear" }}
          </button>
          <button
            v-if="productForm.id"
            type="button"
            class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
            @click="resetProductForm"
          >
            Cancelar
          </button>
        </div>
      </form>

      <div class="overflow-x-auto">
        <table class="min-w-full border-collapse text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-slate-500">
              <th class="px-3 py-2">Producto</th>
              <th class="px-3 py-2">Unidad por defecto</th>
              <th class="px-3 py-2">Categoria por defecto</th>
              <th class="px-3 py-2">Estado</th>
              <th class="px-3 py-2">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id" class="border-b border-slate-100">
              <td class="px-3 py-2 font-medium text-slate-900">{{ product.name }}</td>
              <td class="px-3 py-2">{{ product.defaultUnitCode || "-" }}</td>
              <td class="px-3 py-2">{{ product.defaultCategoryName || "-" }}</td>
              <td class="px-3 py-2">
                <span
                  class="rounded px-2 py-1 text-xs font-medium"
                  :class="product.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'"
                >
                  {{ product.active ? "Activo" : "Inactivo" }}
                </span>
              </td>
              <td class="px-3 py-2">
                <div class="flex gap-2">
                  <button
                    type="button"
                    :disabled="!isAdmin"
                    class="rounded border border-slate-300 px-2 py-1 text-xs enabled:hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                    @click="editProduct(product)"
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    :disabled="!isAdmin"
                    class="rounded border border-red-300 px-2 py-1 text-xs text-red-700 enabled:hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                    @click="deleteProduct(product.id)"
                  >
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="products.length === 0">
              <td colspan="5" class="px-3 py-6 text-center text-slate-500">No hay productos registrados.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import type {
  CatalogCategory,
  CatalogCategoryRequest,
  CatalogProduct,
  CatalogProductRequest,
  CatalogUnit,
  CatalogUnitRequest
} from "../types";

interface UnitFormState extends CatalogUnitRequest {
  id: string | null;
}

interface CategoryFormState extends CatalogCategoryRequest {
  id: string | null;
}

interface ProductFormState {
  id: string | null;
  name: string;
  defaultUnitCode: string;
  defaultCategoryName: string;
  active: boolean;
}

const session = useSessionStore();

const units = ref<CatalogUnit[]>([]);
const categories = ref<CatalogCategory[]>([]);
const products = ref<CatalogProduct[]>([]);
const errorMessage = ref("");
const successMessage = ref("");

const unitForm = reactive<UnitFormState>({
  id: null,
  code: "",
  label: "",
  active: true
});

const categoryForm = reactive<CategoryFormState>({
  id: null,
  name: "",
  active: true
});

const productForm = reactive<ProductFormState>({
  id: null,
  name: "",
  defaultUnitCode: "",
  defaultCategoryName: "",
  active: true
});

const householdId = () => session.activeHouseholdId;

const isAdmin = computed(() => {
  const role = session.activeHousehold?.role;
  return role === "OWNER" || role === "ADMIN";
});

const activeRoleLabel = computed(() => session.activeHousehold?.role ?? "MEMBER");

const resetUnitForm = () => {
  unitForm.id = null;
  unitForm.code = "";
  unitForm.label = "";
  unitForm.active = true;
};

const resetCategoryForm = () => {
  categoryForm.id = null;
  categoryForm.name = "";
  categoryForm.active = true;
};

const resetProductForm = () => {
  productForm.id = null;
  productForm.name = "";
  productForm.defaultUnitCode = "";
  productForm.defaultCategoryName = "";
  productForm.active = true;
};

const clearMessages = () => {
  errorMessage.value = "";
  successMessage.value = "";
};

const ensureAdmin = () => {
  if (isAdmin.value) {
    return true;
  }
  errorMessage.value = "Solo OWNER o ADMIN puede editar el maestro.";
  return false;
};

const loadCatalog = async () => {
  clearMessages();
  const id = householdId();
  if (!id) {
    units.value = [];
    categories.value = [];
    products.value = [];
    return;
  }

  try {
    const [unitsResponse, categoriesResponse, productsResponse] = await Promise.all([
      api.get<CatalogUnit[]>(`/api/households/${id}/admin/catalog/units`),
      api.get<CatalogCategory[]>(`/api/households/${id}/admin/catalog/categories`),
      api.get<CatalogProduct[]>(`/api/households/${id}/admin/catalog/products`)
    ]);

    units.value = unitsResponse.data;
    categories.value = categoriesResponse.data;
    products.value = productsResponse.data;
  } catch {
    errorMessage.value = "No se pudo cargar el maestro del hogar.";
  }
};

const saveUnit = async () => {
  const id = householdId();
  if (!id || !ensureAdmin()) {
    return;
  }
  clearMessages();

  const payload: CatalogUnitRequest = {
    code: unitForm.code.trim().toUpperCase(),
    label: unitForm.label.trim(),
    active: unitForm.active
  };

  try {
    if (unitForm.id) {
      await api.put(`/api/households/${id}/admin/catalog/units/${unitForm.id}`, payload);
      successMessage.value = "Unidad actualizada.";
    } else {
      await api.post(`/api/households/${id}/admin/catalog/units`, payload);
      successMessage.value = "Unidad creada.";
    }
    resetUnitForm();
    await loadCatalog();
  } catch {
    errorMessage.value = "No se pudo guardar la unidad.";
  }
};

const editUnit = (unit: CatalogUnit) => {
  unitForm.id = unit.id;
  unitForm.code = unit.code;
  unitForm.label = unit.label;
  unitForm.active = unit.active;
  clearMessages();
};

const deleteUnit = async (unitId: string) => {
  const id = householdId();
  if (!id || !ensureAdmin()) {
    return;
  }
  if (!window.confirm("Se eliminara la unidad. Quieres continuar?")) {
    return;
  }

  clearMessages();
  try {
    await api.delete(`/api/households/${id}/admin/catalog/units/${unitId}`);
    successMessage.value = "Unidad eliminada.";
    if (unitForm.id === unitId) {
      resetUnitForm();
    }
    await loadCatalog();
  } catch {
    errorMessage.value = "No se pudo eliminar la unidad.";
  }
};

const saveCategory = async () => {
  const id = householdId();
  if (!id || !ensureAdmin()) {
    return;
  }
  clearMessages();

  const payload: CatalogCategoryRequest = {
    name: categoryForm.name.trim().toUpperCase(),
    active: categoryForm.active
  };

  try {
    if (categoryForm.id) {
      await api.put(`/api/households/${id}/admin/catalog/categories/${categoryForm.id}`, payload);
      successMessage.value = "Categoria actualizada.";
    } else {
      await api.post(`/api/households/${id}/admin/catalog/categories`, payload);
      successMessage.value = "Categoria creada.";
    }
    resetCategoryForm();
    await loadCatalog();
  } catch {
    errorMessage.value = "No se pudo guardar la categoria.";
  }
};

const editCategory = (category: CatalogCategory) => {
  categoryForm.id = category.id;
  categoryForm.name = category.name;
  categoryForm.active = category.active;
  clearMessages();
};

const deleteCategory = async (categoryId: string) => {
  const id = householdId();
  if (!id || !ensureAdmin()) {
    return;
  }
  if (!window.confirm("Se eliminara la categoria. Quieres continuar?")) {
    return;
  }

  clearMessages();
  try {
    await api.delete(`/api/households/${id}/admin/catalog/categories/${categoryId}`);
    successMessage.value = "Categoria eliminada.";
    if (categoryForm.id === categoryId) {
      resetCategoryForm();
    }
    await loadCatalog();
  } catch {
    errorMessage.value = "No se pudo eliminar la categoria.";
  }
};

const saveProduct = async () => {
  const id = householdId();
  if (!id || !ensureAdmin()) {
    return;
  }
  clearMessages();

  const payload: CatalogProductRequest = {
    name: productForm.name.trim(),
    defaultUnitCode: productForm.defaultUnitCode.trim() || null,
    defaultCategoryName: productForm.defaultCategoryName.trim() || null,
    active: productForm.active
  };

  try {
    if (productForm.id) {
      await api.put(`/api/households/${id}/admin/catalog/products/${productForm.id}`, payload);
      successMessage.value = "Producto actualizado.";
    } else {
      await api.post(`/api/households/${id}/admin/catalog/products`, payload);
      successMessage.value = "Producto creado.";
    }
    resetProductForm();
    await loadCatalog();
  } catch {
    errorMessage.value = "No se pudo guardar el producto.";
  }
};

const editProduct = (product: CatalogProduct) => {
  productForm.id = product.id;
  productForm.name = product.name;
  productForm.defaultUnitCode = product.defaultUnitCode ?? "";
  productForm.defaultCategoryName = product.defaultCategoryName ?? "";
  productForm.active = product.active;
  clearMessages();
};

const deleteProduct = async (productId: string) => {
  const id = householdId();
  if (!id || !ensureAdmin()) {
    return;
  }
  if (!window.confirm("Se eliminara el producto. Quieres continuar?")) {
    return;
  }

  clearMessages();
  try {
    await api.delete(`/api/households/${id}/admin/catalog/products/${productId}`);
    successMessage.value = "Producto eliminado.";
    if (productForm.id === productId) {
      resetProductForm();
    }
    await loadCatalog();
  } catch {
    errorMessage.value = "No se pudo eliminar el producto.";
  }
};

watch(
  () => session.activeHouseholdId,
  () => {
    resetUnitForm();
    resetCategoryForm();
    resetProductForm();
    void loadCatalog();
  },
  { immediate: true }
);
</script>
