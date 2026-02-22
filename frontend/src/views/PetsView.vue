<template>
  <section class="space-y-5">
    <div>
      <h2 class="text-xl font-semibold">Mascotas</h2>
      <p class="mt-1 text-sm text-slate-600">Gestion de mascotas, alimentacion, cuidados y control de peso.</p>
    </div>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <header class="flex items-center justify-between">
        <h3 class="text-sm font-medium text-slate-800">{{ editingPetId ? "Editar mascota" : "Nueva mascota" }}</h3>
        <button
          v-if="editingPetId"
          type="button"
          class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
          @click="resetPetForm"
        >
          Cancelar edicion
        </button>
      </header>

      <form class="grid gap-3 md:grid-cols-3" @submit.prevent="savePet">
        <div>
          <label for="pet-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Nombre
          </label>
          <input
            id="pet-name"
            v-model="petForm.name"
            required
            maxlength="80"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Toby"
          />
        </div>

        <div>
          <label for="pet-type" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Tipo
          </label>
          <SearchableSelect
            id="pet-type"
            v-model="petForm.type"
            :options="petTypeSelectOptions"
          />
        </div>

        <div>
          <label for="pet-weight" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Peso actual (kg)
          </label>
          <input
            id="pet-weight"
            v-model.number="petForm.currentWeightKg"
            min="0"
            step="0.01"
            type="number"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="pet-chip" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Chip
          </label>
          <input
            id="pet-chip"
            v-model="petForm.chipCode"
            maxlength="80"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="pet-vet" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Veterinario
          </label>
          <input
            id="pet-vet"
            v-model="petForm.veterinarian"
            maxlength="120"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="pet-photo" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Foto URL
          </label>
          <input
            id="pet-photo"
            v-model="petForm.photoUrl"
            maxlength="400"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="https://..."
          />
        </div>

        <div>
          <label for="pet-food-name" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Comida
          </label>
          <input
            id="pet-food-name"
            v-model="petForm.foodName"
            maxlength="120"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Pienso adulto"
          />
        </div>

        <div>
          <label for="pet-food-stock" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Stock comida
          </label>
          <input
            id="pet-food-stock"
            v-model.number="petForm.foodStockQuantity"
            min="0"
            step="0.01"
            type="number"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="pet-food-daily" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Consumo diario
          </label>
          <input
            id="pet-food-daily"
            v-model.number="petForm.foodDailyConsumptionQuantity"
            min="0"
            step="0.01"
            type="number"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="pet-food-unit" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Unidad comida
          </label>
          <SearchableSelect
            id="pet-food-unit"
            v-model="petForm.foodUnit"
            :options="foodUnitSelectOptions"
            placeholder="Sin unidad"
          />
        </div>

        <label class="mt-6 inline-flex items-center gap-2 text-sm text-slate-700">
          <input v-model="petForm.active" type="checkbox" class="toggle-modern" />
          Activa
        </label>

        <button
          type="submit"
          class="mt-6 rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700"
        >
          {{ editingPetId ? "Actualizar mascota" : "Crear mascota" }}
        </button>
      </form>
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <h3 class="text-sm font-medium text-slate-800">Mascotas del hogar</h3>
      <div class="grid gap-3 md:grid-cols-2">
        <div
          v-for="pet in pets"
          :key="pet.id"
          class="rounded-lg border p-3"
          :class="pet.id === selectedPetId ? 'border-slate-900 bg-slate-50' : 'border-slate-200'"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="font-medium text-slate-900">{{ pet.name }} ({{ pet.type }})</p>
              <p class="text-xs text-slate-500">{{ pet.veterinarian || "Sin veterinario" }}</p>
              <p v-if="pet.foodDaysRemaining != null" class="mt-1 text-xs" :class="pet.foodLow ? 'text-amber-700' : 'text-slate-600'">
                Comida: {{ pet.foodDaysRemaining.toFixed(1) }} dias
              </p>
            </div>
            <img
              v-if="pet.photoUrl"
              :src="pet.photoUrl"
              alt="foto mascota"
              class="h-12 w-12 rounded-full object-cover"
            />
          </div>

          <div class="mt-3 flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
              @click="selectPet(pet.id)"
            >
              Ver detalle
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded border border-amber-300 px-2 py-1 text-xs text-amber-700 hover:bg-amber-50"
              @click="editPet(pet)"
            >
              <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 20h9" />
                <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
              </svg>
              Editar
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-1 rounded border border-red-300 px-2 py-1 text-xs text-red-700 hover:bg-red-50"
              @click="deletePet(pet.id)"
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
        </div>

        <p v-if="pets.length === 0" class="text-sm text-slate-500">Todavia no hay mascotas registradas.</p>
      </div>
    </article>

    <template v-if="selectedPet">
      <article class="space-y-3 rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium text-slate-800">Alimentacion - {{ selectedPet.name }}</h3>
        <form class="grid gap-3 md:grid-cols-4" @submit.prevent="addFeeding">
          <div>
            <label for="feed-food-type" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Comida
            </label>
            <input
              id="feed-food-type"
              v-model="feedingForm.foodType"
              required
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label for="feed-quantity" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Cantidad
            </label>
            <input
              id="feed-quantity"
              v-model.number="feedingForm.quantity"
              required
              min="0.01"
              step="0.01"
              type="number"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label for="feed-unit" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Unidad
            </label>
            <SearchableSelect
              id="feed-unit"
              v-model="feedingForm.unit"
              :options="foodUnitOnlyOptions"
            />
          </div>
          <button type="submit" class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700">
            Registrar comida
          </button>
        </form>

        <ul class="space-y-1 text-sm">
          <li v-for="feeding in feedings" :key="feeding.id" class="rounded bg-slate-50 px-3 py-2">
            {{ formatDateTime(feeding.fedAt) }} - {{ feeding.foodType }}: {{ feeding.quantity }} {{ feeding.unit }}
          </li>
          <li v-if="feedings.length === 0" class="text-slate-500">Sin registros de alimentacion.</li>
        </ul>
      </article>

      <article class="space-y-3 rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium text-slate-800">Cuidados - {{ selectedPet.name }}</h3>
        <form class="grid gap-3 md:grid-cols-4" @submit.prevent="saveCareTask">
          <div>
            <label for="care-type" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Tipo
            </label>
            <SearchableSelect
              id="care-type"
              v-model="careForm.careType"
              :options="careTypeOptions"
            />
          </div>
          <div>
            <label for="care-frequency" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Frecuencia (dias)
            </label>
            <input
              id="care-frequency"
              v-model.number="careForm.frequencyDays"
              required
              min="1"
              type="number"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label for="care-notify" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Aviso antes (dias)
            </label>
            <input
              id="care-notify"
              v-model.number="careForm.notifyDaysBefore"
              required
              min="0"
              type="number"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <button type="submit" class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700">
            Guardar cuidado
          </button>
          <div class="md:col-span-4">
            <label for="care-description" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Descripcion
            </label>
            <input
              id="care-description"
              v-model="careForm.description"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
        </form>

        <ul class="space-y-1 text-sm">
          <li v-for="task in careTasks" :key="task.id" class="rounded px-3 py-2" :class="task.dueSoon ? 'bg-amber-50' : 'bg-slate-50'">
            <div class="flex items-center justify-between gap-3">
              <span>{{ careLabel(task.careType) }} - proximo: {{ formatDate(task.nextDueAt) }}</span>
              <button
                type="button"
                class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-white"
                @click="completeCareTask(task.id)"
              >
                Marcar realizado
              </button>
            </div>
          </li>
          <li v-if="careTasks.length === 0" class="text-slate-500">Sin cuidados definidos.</li>
        </ul>
      </article>

      <article class="space-y-3 rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium text-slate-800">Paseos - {{ selectedPet.name }}</h3>
        <p class="text-sm text-slate-600">Los paseos se registran desde Actividades y aparecen aqui automaticamente.</p>

        <div class="grid gap-3 md:grid-cols-3">
          <div class="rounded-lg bg-slate-50 p-3">
            <p class="text-xs uppercase tracking-wide text-slate-500">Paseos semana</p>
            <p class="text-lg font-semibold text-slate-900">{{ petWalkStats.activitiesCount }}</p>
          </div>
          <div class="rounded-lg bg-slate-50 p-3">
            <p class="text-xs uppercase tracking-wide text-slate-500">Distancia</p>
            <p class="text-lg font-semibold text-slate-900">{{ petWalkStats.distanceKm.toFixed(2) }} km</p>
          </div>
          <div class="rounded-lg bg-slate-50 p-3">
            <p class="text-xs uppercase tracking-wide text-slate-500">Duracion</p>
            <p class="text-lg font-semibold text-slate-900">{{ formatDurationMinutes(petWalkStats.durationMinutes) }}</p>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full border-collapse text-sm">
            <thead>
              <tr class="border-b border-slate-200 text-left text-slate-500">
                <th class="px-3 py-2">Fecha</th>
                <th class="px-3 py-2">Distancia</th>
                <th class="px-3 py-2">Duracion</th>
                <th class="px-3 py-2">Recorrido</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="walk in petWalks" :key="walk.id" class="border-b border-slate-100">
                <td class="px-3 py-2">{{ formatDateTime(walk.startedAt) }}</td>
                <td class="px-3 py-2">{{ walk.distanceKm.toFixed(2) }} km</td>
                <td class="px-3 py-2">{{ formatDurationSeconds(walk.durationSeconds) }}</td>
                <td class="px-3 py-2">
                  <button
                    type="button"
                    class="rounded border border-slate-300 px-2 py-1 text-xs enabled:hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                    :disabled="walk.points.length < 2"
                    @click="selectedPetWalkId = walk.id"
                  >
                    Ver mapa
                  </button>
                </td>
              </tr>
              <tr v-if="petWalks.length === 0">
                <td colspan="4" class="px-3 py-6 text-center text-slate-500">No hay paseos registrados.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article v-if="selectedPetWalk && selectedPetWalk.points.length > 0" class="space-y-3 rounded-xl border border-slate-200 p-4">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-medium text-slate-800">
            Recorrido - {{ formatDateTime(selectedPetWalk.startedAt) }}
          </h3>
          <button
            type="button"
            class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
            @click="selectedPetWalkId = ''"
          >
            Cerrar
          </button>
        </div>
        <RouteMap :points="selectedPetWalk.points" height-class="h-80" />
      </article>

      <article class="space-y-3 rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium text-slate-800">Peso - {{ selectedPet.name }}</h3>
        <form class="grid gap-3 md:grid-cols-3" @submit.prevent="addWeight">
          <div>
            <label for="weight-kg" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Peso (kg)
            </label>
            <input
              id="weight-kg"
              v-model.number="weightForm.weightKg"
              required
              min="0.01"
              step="0.01"
              type="number"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <button type="submit" class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700">
            Registrar peso
          </button>
        </form>

        <div v-if="weights.length > 1" class="rounded-lg border border-slate-200 p-3">
          <svg viewBox="0 0 300 100" class="h-28 w-full">
            <polyline
              fill="none"
              stroke="#0f172a"
              stroke-width="2"
              :points="weightPolyline"
            />
          </svg>
        </div>

        <ul class="space-y-1 text-sm">
          <li v-for="weight in [...weights].reverse()" :key="weight.id" class="rounded bg-slate-50 px-3 py-2">
            {{ formatDateTime(weight.recordedAt) }} - {{ weight.weightKg.toFixed(2) }} kg
          </li>
          <li v-if="weights.length === 0" class="text-slate-500">Sin registros de peso.</li>
        </ul>
      </article>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import RouteMap from "../components/RouteMap.vue";
import SearchableSelect from "../components/SearchableSelect.vue";
import type {
  Activity,
  ActivityWeeklyStats,
  Pet,
  PetCareTask,
  PetCareTaskRequest,
  PetFeeding,
  PetFeedingRequest,
  PetRequest,
  PetWeight,
  PetWeightRequest,
  PetCareType
} from "../types";

const session = useSessionStore();
const errorMessage = ref("");

const pets = ref<Pet[]>([]);
const selectedPetId = ref("");
const editingPetId = ref<string | null>(null);

const feedings = ref<PetFeeding[]>([]);
const careTasks = ref<PetCareTask[]>([]);
const weights = ref<PetWeight[]>([]);
const petWalks = ref<Activity[]>([]);
const selectedPetWalkId = ref("");
const petWalkStats = ref<ActivityWeeklyStats>({
  weekStart: "",
  weekEnd: "",
  activitiesCount: 0,
  distanceKm: 0,
  durationMinutes: 0,
  daily: []
});

const petTypeOptions = ["DOG", "CAT", "BIRD", "OTHER"];
const foodUnitOptions = ["G", "KG", "ML", "L", "UD"];
const petTypeSelectOptions = petTypeOptions.map((type) => ({ value: type, label: type }));
const foodUnitSelectOptions = [
  { value: "", label: "Sin unidad" },
  ...foodUnitOptions.map((unit) => ({ value: unit, label: unit }))
];
const foodUnitOnlyOptions = foodUnitOptions.map((unit) => ({ value: unit, label: unit }));
const careTypeOptions = [
  { value: "VACCINATION", label: "Vacunacion" },
  { value: "GROOMING", label: "Peluqueria" },
  { value: "DEWORMING", label: "Desparasitacion" },
  { value: "VET_VISIT", label: "Veterinario" },
  { value: "OTHER", label: "Otro" }
];

const emptyStringToNull = (value: string | null | undefined): string | null => {
  if (!value) return null;
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
};

const petForm = reactive<PetRequest>({
  name: "",
  type: "DOG",
  chipCode: null,
  veterinarian: null,
  photoUrl: null,
  currentWeightKg: null,
  foodName: null,
  foodStockQuantity: null,
  foodDailyConsumptionQuantity: null,
  foodUnit: null,
  active: true
});

const feedingForm = reactive<PetFeedingRequest>({
  foodType: "",
  quantity: 1,
  unit: "G",
  fedAt: null,
  notes: null
});

const careForm = reactive<PetCareTaskRequest>({
  careType: "VACCINATION",
  description: null,
  frequencyDays: 30,
  notifyDaysBefore: 3,
  lastPerformedAt: null,
  active: true
});

const weightForm = reactive<PetWeightRequest>({
  weightKg: 1,
  recordedAt: null
});

const householdId = () => session.activeHouseholdId;
const selectedPet = computed(() => pets.value.find((pet) => pet.id === selectedPetId.value) ?? null);
const selectedPetWalk = computed(() => petWalks.value.find((walk) => walk.id === selectedPetWalkId.value) ?? null);

const weightPolyline = computed(() => {
  if (weights.value.length < 2) {
    return "";
  }

  const values = weights.value.map((entry) => entry.weightKg);
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;

  return weights.value
    .map((entry, index) => {
      const x = (index / (weights.value.length - 1)) * 300;
      const y = 95 - ((entry.weightKg - min) / span) * 80;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(" ");
});

const loadPets = async () => {
  const id = householdId();
  if (!id) {
    pets.value = [];
    selectedPetId.value = "";
    return;
  }

  const { data } = await api.get<Pet[]>(`/api/households/${id}/pets`);
  pets.value = data;

  if (!pets.value.some((pet) => pet.id === selectedPetId.value)) {
    selectedPetId.value = pets.value[0]?.id ?? "";
  }
};

const loadSelectedPetDetails = async () => {
  const id = householdId();
  const petId = selectedPetId.value;
  if (!id || !petId) {
    feedings.value = [];
    careTasks.value = [];
    weights.value = [];
    petWalks.value = [];
    petWalkStats.value = { weekStart: "", weekEnd: "", activitiesCount: 0, distanceKm: 0, durationMinutes: 0, daily: [] };
    selectedPetWalkId.value = "";
    return;
  }

  const [feedingResponse, careResponse, weightsResponse, walksResponse, walkStatsResponse] = await Promise.all([
    api.get<PetFeeding[]>(`/api/households/${id}/pets/${petId}/feedings`),
    api.get<PetCareTask[]>(`/api/households/${id}/pets/${petId}/care-tasks`),
    api.get<PetWeight[]>(`/api/households/${id}/pets/${petId}/weights`),
    api.get<Activity[]>(`/api/households/${id}/activities`, { params: { petId, type: "PET_WALK" } }),
    api.get<ActivityWeeklyStats>(`/api/households/${id}/activities/stats/weekly`, { params: { petId, type: "PET_WALK" } })
  ]);

  feedings.value = feedingResponse.data;
  careTasks.value = careResponse.data;
  weights.value = weightsResponse.data;
  petWalks.value = walksResponse.data;
  petWalkStats.value = walkStatsResponse.data;

  if (!petWalks.value.some((walk) => walk.id === selectedPetWalkId.value)) {
    selectedPetWalkId.value = "";
  }
};

const resetPetForm = () => {
  editingPetId.value = null;
  petForm.name = "";
  petForm.type = "DOG";
  petForm.chipCode = null;
  petForm.veterinarian = null;
  petForm.photoUrl = null;
  petForm.currentWeightKg = null;
  petForm.foodName = null;
  petForm.foodStockQuantity = null;
  petForm.foodDailyConsumptionQuantity = null;
  petForm.foodUnit = null;
  petForm.active = true;
};

const selectPet = (petId: string) => {
  selectedPetId.value = petId;
};

const editPet = (pet: Pet) => {
  editingPetId.value = pet.id;
  petForm.name = pet.name;
  petForm.type = pet.type;
  petForm.chipCode = pet.chipCode;
  petForm.veterinarian = pet.veterinarian;
  petForm.photoUrl = pet.photoUrl;
  petForm.currentWeightKg = pet.currentWeightKg;
  petForm.foodName = pet.foodName;
  petForm.foodStockQuantity = pet.foodStockQuantity;
  petForm.foodDailyConsumptionQuantity = pet.foodDailyConsumptionQuantity;
  petForm.foodUnit = pet.foodUnit;
  petForm.active = pet.active;
};

const savePet = async () => {
  const id = householdId();
  if (!id) return;

  errorMessage.value = "";
  const payload: PetRequest = {
    name: petForm.name.trim(),
    type: petForm.type.trim(),
    chipCode: emptyStringToNull(petForm.chipCode),
    veterinarian: emptyStringToNull(petForm.veterinarian),
    photoUrl: emptyStringToNull(petForm.photoUrl),
    currentWeightKg: petForm.currentWeightKg,
    foodName: emptyStringToNull(petForm.foodName),
    foodStockQuantity: petForm.foodStockQuantity,
    foodDailyConsumptionQuantity: petForm.foodDailyConsumptionQuantity,
    foodUnit: emptyStringToNull(petForm.foodUnit),
    active: petForm.active
  };

  try {
    if (editingPetId.value) {
      await api.put(`/api/households/${id}/pets/${editingPetId.value}`, payload);
    } else {
      await api.post(`/api/households/${id}/pets`, payload);
    }
    await loadPets();
    resetPetForm();
  } catch {
    errorMessage.value = "No se pudo guardar la mascota.";
  }
};

const deletePet = async (petId: string) => {
  const id = householdId();
  if (!id) return;
  if (!window.confirm("Se eliminara la mascota con sus registros. Continuar?")) return;

  errorMessage.value = "";
  try {
    await api.delete(`/api/households/${id}/pets/${petId}`);
    if (editingPetId.value === petId) {
      resetPetForm();
    }
    await loadPets();
    await loadSelectedPetDetails();
  } catch {
    errorMessage.value = "No se pudo eliminar la mascota.";
  }
};

const addFeeding = async () => {
  const id = householdId();
  const petId = selectedPetId.value;
  if (!id || !petId) return;

  errorMessage.value = "";
  try {
    const payload: PetFeedingRequest = {
      foodType: feedingForm.foodType.trim(),
      quantity: feedingForm.quantity,
      unit: feedingForm.unit,
      fedAt: null,
      notes: emptyStringToNull(feedingForm.notes)
    };
    await api.post(`/api/households/${id}/pets/${petId}/feedings`, payload);
    feedingForm.foodType = selectedPet.value?.foodName ?? "";
    feedingForm.quantity = 1;
    feedingForm.notes = null;
    await Promise.all([loadPets(), loadSelectedPetDetails()]);
  } catch {
    errorMessage.value = "No se pudo registrar la alimentacion.";
  }
};

const saveCareTask = async () => {
  const id = householdId();
  const petId = selectedPetId.value;
  if (!id || !petId) return;

  errorMessage.value = "";
  try {
    const payload: PetCareTaskRequest = {
      careType: careForm.careType,
      description: emptyStringToNull(careForm.description),
      frequencyDays: careForm.frequencyDays,
      notifyDaysBefore: careForm.notifyDaysBefore,
      lastPerformedAt: null,
      active: careForm.active
    };
    await api.post(`/api/households/${id}/pets/${petId}/care-tasks`, payload);
    careForm.description = null;
    careForm.frequencyDays = 30;
    careForm.notifyDaysBefore = 3;
    await loadSelectedPetDetails();
  } catch {
    errorMessage.value = "No se pudo guardar el cuidado.";
  }
};

const completeCareTask = async (taskId: string) => {
  const id = householdId();
  const petId = selectedPetId.value;
  if (!id || !petId) return;

  await api.post(`/api/households/${id}/pets/${petId}/care-tasks/${taskId}/complete`);
  await loadSelectedPetDetails();
};

const addWeight = async () => {
  const id = householdId();
  const petId = selectedPetId.value;
  if (!id || !petId) return;

  errorMessage.value = "";
  try {
    const payload: PetWeightRequest = {
      weightKg: weightForm.weightKg,
      recordedAt: null
    };
    await api.post(`/api/households/${id}/pets/${petId}/weights`, payload);
    await Promise.all([loadPets(), loadSelectedPetDetails()]);
  } catch {
    errorMessage.value = "No se pudo registrar el peso.";
  }
};

const careLabel = (careType: PetCareType) => {
  if (careType === "VACCINATION") return "Vacunacion";
  if (careType === "GROOMING") return "Peluqueria";
  if (careType === "DEWORMING") return "Desparasitacion";
  if (careType === "VET_VISIT") return "Veterinario";
  return "Otro";
};

const formatDateTime = (value: string) => {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const formatDate = (value: string) => {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString();
};

const formatDurationSeconds = (seconds: number) => formatDurationMinutes(Math.round(seconds / 60));

const formatDurationMinutes = (minutes: number) => {
  const hours = Math.floor(minutes / 60);
  const remaining = minutes % 60;
  if (hours <= 0) {
    return `${remaining} min`;
  }
  return `${hours} h ${remaining} min`;
};

watch(
  () => session.activeHouseholdId,
  () => {
    resetPetForm();
    errorMessage.value = "";
    void (async () => {
      await loadPets();
      await loadSelectedPetDetails();
      if (selectedPet.value) {
        feedingForm.foodType = selectedPet.value.foodName ?? "";
      }
    })();
  },
  { immediate: true }
);

watch(
  () => selectedPetId.value,
  () => {
    void loadSelectedPetDetails();
    feedingForm.foodType = selectedPet.value?.foodName ?? "";
    feedingForm.unit = selectedPet.value?.foodUnit ?? "G";
  }
);
</script>
