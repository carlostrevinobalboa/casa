<template>
  <section class="space-y-4">
    <div>
      <h2 class="text-xl font-semibold">Recetas</h2>
      <p class="mt-1 text-sm text-slate-600">
        Crea recetas, asocia ingredientes y ejecutalas para descontar despensa y enviar faltantes a compra.
      </p>
    </div>

    <form class="space-y-3 rounded-xl border border-slate-200 p-4" @submit.prevent="createRecipe">
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label for="recipe-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Nombre de receta
          </label>
          <input
            id="recipe-name"
            v-model="form.name"
            required
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Ej: Tortilla de patatas"
          />
        </div>

        <div>
          <label for="recipe-description" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Descripcion
          </label>
          <input
            id="recipe-description"
            v-model="form.description"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Plato rapido para cena"
          />
        </div>
      </div>

      <div>
        <label for="recipe-steps" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Pasos
        </label>
        <textarea
          id="recipe-steps"
          v-model="form.steps"
          rows="3"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          placeholder="Describe los pasos de preparacion"
        ></textarea>
      </div>

      <div class="space-y-2">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-medium text-slate-800">Ingredientes</h3>
          <button
            type="button"
            class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
            @click="addIngredient"
          >
            Anadir ingrediente
          </button>
        </div>

        <div
          v-for="(ingredient, index) in form.ingredients"
          :key="`ingredient-${index}`"
          class="grid gap-2 rounded-lg border border-slate-200 p-3 md:grid-cols-[1fr_120px_100px_auto]"
        >
          <div>
            <label :for="`ingredient-name-${index}`" class="mb-1 block text-xs text-slate-600">
              Producto
            </label>
            <input
              :id="`ingredient-name-${index}`"
              v-model="ingredient.productName"
              list="recipe-product-options"
              required
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              placeholder="Ej: Huevo"
              @change="onIngredientProductChange(ingredient)"
            />
          </div>

          <div>
            <label :for="`ingredient-quantity-${index}`" class="mb-1 block text-xs text-slate-600">
              Cantidad
            </label>
            <input
              :id="`ingredient-quantity-${index}`"
              v-model.number="ingredient.requiredQuantity"
              required
              min="0.01"
              step="0.01"
              type="number"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>

          <div>
            <label :for="`ingredient-unit-${index}`" class="mb-1 block text-xs text-slate-600">
              Unidad
            </label>
            <input
              :id="`ingredient-unit-${index}`"
              v-model="ingredient.unit"
              list="recipe-unit-options"
              required
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              placeholder="UD"
            />
          </div>

          <div class="self-end">
            <button
              type="button"
              class="rounded border border-red-300 px-2 py-2 text-xs text-red-700 hover:bg-red-50"
              :disabled="form.ingredients.length === 1"
              @click="removeIngredient(index)"
            >
              Quitar
            </button>
          </div>
        </div>
      </div>

      <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

      <button
        type="submit"
        class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700"
      >
        Guardar receta
      </button>
    </form>

    <section class="space-y-3">
      <h3 class="text-sm font-medium text-slate-800">Recetas guardadas</h3>

      <article
        v-for="recipe in recipes"
        :key="recipe.id"
        class="space-y-2 rounded-xl border border-slate-200 p-4"
      >
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h4 class="text-base font-semibold text-slate-900">{{ recipe.name }}</h4>
            <p v-if="recipe.description" class="text-sm text-slate-600">{{ recipe.description }}</p>
          </div>

          <div class="flex gap-2">
            <button
              type="button"
              class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
              @click="executeRecipe(recipe.id)"
            >
              Ejecutar receta
            </button>
            <button
              type="button"
              class="rounded border border-red-300 px-2 py-1 text-xs text-red-700 hover:bg-red-50"
              @click="deleteRecipe(recipe.id)"
            >
              Eliminar
            </button>
          </div>
        </div>

        <ul class="list-disc pl-5 text-sm text-slate-700">
          <li v-for="ingredient in recipe.ingredients" :key="ingredient.id">
            {{ ingredient.productName }}: {{ ingredient.requiredQuantity }} {{ ingredient.unit }}
          </li>
        </ul>
      </article>

      <p v-if="recipes.length === 0" class="text-sm text-slate-500">No hay recetas creadas todavia.</p>
    </section>

    <datalist id="recipe-product-options">
      <option v-for="product in catalogProducts" :key="product.id" :value="product.name"></option>
    </datalist>
    <datalist id="recipe-unit-options">
      <option v-for="unit in catalogUnits" :key="unit.id" :value="unit.code"></option>
    </datalist>

    <section
      v-if="lastExecution"
      class="rounded-xl border border-slate-200 bg-slate-50 p-4"
    >
      <h3 class="text-sm font-medium text-slate-900">
        Ultima ejecucion: {{ lastExecution.recipeName }}
      </h3>
      <p class="mt-1 text-sm text-slate-600">
        Faltantes enviados a compra: {{ lastExecution.shortagesCount }}
      </p>
      <ul v-if="lastExecution.shortages.length" class="mt-2 list-disc pl-5 text-sm text-slate-700">
        <li v-for="shortage in lastExecution.shortages" :key="`${shortage.productName}-${shortage.unit}`">
          {{ shortage.productName }}: faltan {{ shortage.missingQuantity }} {{ shortage.unit }}
        </li>
      </ul>
    </section>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { api } from "../lib/api";
import { fetchHouseholdCatalog, findProductInCatalog } from "../lib/catalog";
import { useSessionStore } from "../stores/session";
import type {
  CatalogProduct,
  CatalogUnit,
  Recipe,
  RecipeExecutionResult,
  RecipeIngredientRequest,
  RecipeRequest
} from "../types";

const session = useSessionStore();

const recipes = ref<Recipe[]>([]);
const catalogUnits = ref<CatalogUnit[]>([]);
const catalogProducts = ref<CatalogProduct[]>([]);
const errorMessage = ref("");
const lastExecution = ref<RecipeExecutionResult | null>(null);

const defaultUnitCode = () => catalogUnits.value[0]?.code ?? "ud";

const createEmptyIngredient = (): RecipeIngredientRequest => ({
  productName: "",
  requiredQuantity: 1,
  unit: defaultUnitCode()
});

const form = reactive<RecipeRequest>({
  name: "",
  description: "",
  steps: "",
  ingredients: [createEmptyIngredient()]
});

const householdId = () => session.activeHouseholdId;

const loadCatalog = async () => {
  const id = householdId();
  if (!id) {
    catalogUnits.value = [];
    catalogProducts.value = [];
    return;
  }

  try {
    const catalog = await fetchHouseholdCatalog(id);
    catalogUnits.value = catalog.units.filter((unit) => unit.active);
    catalogProducts.value = catalog.products.filter((product) => product.active);
  } catch {
    catalogUnits.value = [];
    catalogProducts.value = [];
  }
};

const load = async () => {
  errorMessage.value = "";
  const id = householdId();
  if (!id) {
    recipes.value = [];
    return;
  }

  const { data } = await api.get<Recipe[]>(`/api/households/${id}/recipes`);
  recipes.value = data;
};

const addIngredient = () => {
  form.ingredients.push(createEmptyIngredient());
};

const removeIngredient = (index: number) => {
  if (form.ingredients.length === 1) {
    return;
  }
  form.ingredients.splice(index, 1);
};

const resetForm = () => {
  form.name = "";
  form.description = "";
  form.steps = "";
  form.ingredients = [createEmptyIngredient()] as RecipeIngredientRequest[];
};

const onIngredientProductChange = (ingredient: RecipeIngredientRequest) => {
  const product = findProductInCatalog(catalogProducts.value, ingredient.productName);
  if (!product) {
    return;
  }

  if (product.defaultUnitCode) {
    ingredient.unit = product.defaultUnitCode;
  }
};

const createRecipe = async () => {
  const id = householdId();
  if (!id) {
    return;
  }

  try {
    form.ingredients.forEach((ingredient) => onIngredientProductChange(ingredient));
    await api.post(`/api/households/${id}/recipes`, form);
    resetForm();
    await load();
  } catch {
    errorMessage.value = "No se pudo guardar la receta.";
  }
};

const deleteRecipe = async (recipeId: string) => {
  const id = householdId();
  if (!id) {
    return;
  }

  await api.delete(`/api/households/${id}/recipes/${recipeId}`);
  await load();
};

const executeRecipe = async (recipeId: string) => {
  const id = householdId();
  if (!id) {
    return;
  }

  const { data } = await api.post<RecipeExecutionResult>(`/api/households/${id}/recipes/${recipeId}/execute`);
  lastExecution.value = data;
};

watch(
  () => session.activeHouseholdId,
  () => {
    void Promise.all([loadCatalog(), load()]);
  },
  { immediate: true }
);
</script>
