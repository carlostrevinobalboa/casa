<template>
  <section class="space-y-4">
    <div>
      <h2 class="text-xl font-semibold">Actividades deportivas</h2>
      <p class="mt-1 text-sm text-slate-600">
        Historico con filtro por usuario, seguimiento GPS en vivo y estadisticas semanales.
      </p>
    </div>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

    <article class="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-4">
      <div>
        <label for="activity-user-filter" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Usuario
        </label>
        <SearchableSelect
          id="activity-user-filter"
          v-model="selectedUserFilter"
          :options="userFilterOptions"
          placeholder="Todos"
        />
      </div>

      <div>
        <label for="activity-type-filter" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Tipo actividad
        </label>
        <SearchableSelect
          id="activity-type-filter"
          v-model="activityTypeFilter"
          :options="typeFilterOptions"
          placeholder="Todos"
        />
      </div>

      <div>
        <label for="activity-pet-filter" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Mascota
        </label>
        <SearchableSelect
          id="activity-pet-filter"
          v-model="selectedPetFilter"
          :options="petFilterOptions"
          placeholder="Todas"
        />
      </div>

      <button
        type="button"
        class="self-end rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
        @click="reloadAll"
      >
        Recargar
      </button>
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-medium text-slate-800">GPS en vivo</h3>
        <p class="text-xs text-slate-500" v-if="!supportsGeolocation">Este navegador no soporta geolocalizacion.</p>
      </div>

      <div class="grid gap-3 md:grid-cols-4">
        <div>
          <label for="tracking-mode" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Modo
          </label>
          <SearchableSelect
            id="tracking-mode"
            v-model="trackingMode"
            :options="trackingModeOptions"
            :disabled="trackingInProgress"
          />
        </div>

        <div v-if="trackingMode === 'PET_WALK'">
          <label for="tracking-pet" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Mascota
          </label>
          <SearchableSelect
            id="tracking-pet"
            v-model="trackingPetId"
            :options="trackingPetOptions"
            :disabled="trackingInProgress"
            placeholder="Selecciona mascota"
          />
        </div>

        <div class="rounded-lg bg-slate-50 px-3 py-2">
          <p class="text-xs uppercase tracking-wide text-slate-500">Tiempo</p>
          <p class="text-sm font-semibold text-slate-900">{{ formatDurationSeconds(trackingElapsedSeconds) }}</p>
        </div>

        <div class="rounded-lg bg-slate-50 px-3 py-2">
          <p class="text-xs uppercase tracking-wide text-slate-500">Distancia</p>
          <p class="text-sm font-semibold text-slate-900">{{ trackingDistanceKm.toFixed(3) }} km</p>
        </div>
      </div>

      <div class="flex flex-wrap gap-2">
        <button
          type="button"
          class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white enabled:hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="trackingInProgress || !supportsGeolocation"
          @click="startTracking"
        >
          Iniciar GPS
        </button>
        <button
          type="button"
          class="rounded-lg border border-emerald-300 px-3 py-2 text-sm text-emerald-700 enabled:hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="!trackingInProgress"
          @click="stopTrackingAndSave"
        >
          Detener y guardar
        </button>
        <button
          type="button"
          class="rounded-lg border border-slate-300 px-3 py-2 text-sm enabled:hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="!trackingInProgress"
          @click="cancelTracking"
        >
          Cancelar
        </button>
      </div>

      <p class="text-xs text-slate-500">
        {{ trackingStatus }}
      </p>

      <RouteMap v-if="trackingPoints.length > 0" :points="trackingPoints" height-class="h-72" />
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <h3 class="text-sm font-medium text-slate-800">Resumen semanal</h3>
      <div class="grid gap-3 md:grid-cols-3">
        <div class="rounded-lg bg-slate-50 p-3">
          <p class="text-xs uppercase tracking-wide text-slate-500">Actividades</p>
          <p class="text-lg font-semibold text-slate-900">{{ stats.activitiesCount }}</p>
        </div>
        <div class="rounded-lg bg-slate-50 p-3">
          <p class="text-xs uppercase tracking-wide text-slate-500">Distancia</p>
          <p class="text-lg font-semibold text-slate-900">{{ stats.distanceKm.toFixed(2) }} km</p>
        </div>
        <div class="rounded-lg bg-slate-50 p-3">
          <p class="text-xs uppercase tracking-wide text-slate-500">Duracion</p>
          <p class="text-lg font-semibold text-slate-900">{{ formatDurationMinutes(stats.durationMinutes) }}</p>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full border-collapse text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-slate-500">
              <th class="px-3 py-2">Dia</th>
              <th class="px-3 py-2">Actividades</th>
              <th class="px-3 py-2">Km</th>
              <th class="px-3 py-2">Min</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="day in stats.daily" :key="day.day" class="border-b border-slate-100">
              <td class="px-3 py-2">{{ formatDate(day.day) }}</td>
              <td class="px-3 py-2">{{ day.activitiesCount }}</td>
              <td class="px-3 py-2">{{ day.distanceKm.toFixed(2) }}</td>
              <td class="px-3 py-2">{{ day.durationMinutes }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <h3 class="text-sm font-medium text-slate-800">Registrar actividad manual</h3>
      <form class="grid gap-3 md:grid-cols-3" @submit.prevent="createActivity">
        <div>
          <label for="activity-type" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Tipo
          </label>
          <SearchableSelect
            id="activity-type"
            v-model="activityForm.type"
            :options="activityTypeOptions"
          />
        </div>

        <div>
          <label for="activity-user" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Usuario
          </label>
          <SearchableSelect
            id="activity-user"
            v-model="activityForm.performedByUserId"
            :options="activityUserOptions"
            placeholder="Yo"
          />
        </div>

        <div v-if="activityForm.type === 'PET_WALK'">
          <label for="activity-pet" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Mascota
          </label>
          <SearchableSelect
            id="activity-pet"
            v-model="activityForm.petId"
            :options="activityPetOptions"
            placeholder="Selecciona mascota"
          />
        </div>

        <div>
          <label for="activity-title" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Titulo
          </label>
          <input
            id="activity-title"
            v-model="activityForm.title"
            maxlength="120"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Entreno tarde"
          />
        </div>

        <div>
          <label for="activity-started-at" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Inicio
          </label>
          <input
            id="activity-started-at"
            v-model="activityForm.startedAt"
            type="datetime-local"
            required
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="activity-ended-at" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Fin
          </label>
          <input
            id="activity-ended-at"
            v-model="activityForm.endedAt"
            type="datetime-local"
            required
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label for="activity-distance-km" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Distancia (km)
          </label>
          <input
            id="activity-distance-km"
            v-model.number="activityForm.distanceKm"
            min="0"
            step="0.01"
            type="number"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>

        <div class="md:col-span-2">
          <label for="activity-notes" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Notas
          </label>
          <input
            id="activity-notes"
            v-model="activityForm.notes"
            maxlength="600"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            placeholder="Sensaciones, ritmo, etc."
          />
        </div>

        <label class="mt-6 inline-flex items-center gap-2 text-sm text-slate-700">
          <input v-model="activityForm.gpsTracked" type="checkbox" class="toggle-modern" />
          Seguimiento GPS
        </label>

        <div class="md:col-span-3 space-y-2 rounded-lg border border-slate-200 p-3">
          <div class="flex items-center justify-between">
            <p class="text-sm font-medium text-slate-700">Puntos de recorrido</p>
            <button
              type="button"
              class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
              @click="addPoint"
            >
              Anadir punto
            </button>
          </div>

          <div
            v-for="(point, index) in activityForm.points"
            :key="`point-${index}`"
            class="grid gap-2 md:grid-cols-[1fr_1fr_auto]"
          >
            <input
              v-model.number="point.latitude"
              step="0.000001"
              type="number"
              class="rounded-lg border border-slate-300 px-3 py-2 text-sm"
              placeholder="Latitud"
            />
            <input
              v-model.number="point.longitude"
              step="0.000001"
              type="number"
              class="rounded-lg border border-slate-300 px-3 py-2 text-sm"
              placeholder="Longitud"
            />
            <button
              type="button"
              class="rounded border border-red-300 px-2 py-2 text-xs text-red-700 hover:bg-red-50"
              @click="removePoint(index)"
            >
              Quitar
            </button>
          </div>

          <RouteMap v-if="activityForm.points.length > 0" :points="activityForm.points" height-class="h-60" />
        </div>

        <button type="submit" class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700">
          Guardar actividad
        </button>
      </form>
    </article>

    <article class="space-y-3 rounded-xl border border-slate-200 p-4">
      <h3 class="text-sm font-medium text-slate-800">Historico de actividades</h3>
      <div class="overflow-x-auto">
        <table class="min-w-full border-collapse text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-slate-500">
              <th class="px-3 py-2">Fecha</th>
              <th class="px-3 py-2">Tipo</th>
              <th class="px-3 py-2">Usuario</th>
              <th class="px-3 py-2">Mascota</th>
              <th class="px-3 py-2">Distancia</th>
              <th class="px-3 py-2">Duracion</th>
              <th class="px-3 py-2">Recorrido</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="activity in activities" :key="activity.id" class="border-b border-slate-100">
              <td class="px-3 py-2">{{ formatDateTime(activity.startedAt) }}</td>
              <td class="px-3 py-2">{{ activityTypeLabel(activity.type) }}</td>
              <td class="px-3 py-2">{{ memberName(activity.performedByUserId) }}</td>
              <td class="px-3 py-2">{{ petName(activity.petId) }}</td>
              <td class="px-3 py-2">{{ activity.distanceKm.toFixed(2) }} km</td>
              <td class="px-3 py-2">{{ formatDurationSeconds(activity.durationSeconds) }}</td>
              <td class="px-3 py-2">
                <button
                  type="button"
                  class="rounded border border-slate-300 px-2 py-1 text-xs enabled:hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="activity.points.length < 2"
                  @click="selectedActivityId = activity.id"
                >
                  Ver mapa
                </button>
              </td>
            </tr>
            <tr v-if="activities.length === 0">
              <td colspan="7" class="px-3 py-6 text-center text-slate-500">No hay actividades registradas.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article v-if="selectedActivity && selectedActivity.points.length > 0" class="space-y-3 rounded-xl border border-slate-200 p-4">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-medium text-slate-800">
          Recorrido: {{ activityTypeLabel(selectedActivity.type) }} - {{ formatDateTime(selectedActivity.startedAt) }}
        </h3>
        <button
          type="button"
          class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
          @click="selectedActivityId = ''"
        >
          Cerrar
        </button>
      </div>
      <RouteMap :points="selectedActivity.points" height-class="h-80" />
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from "vue";
import RouteMap from "../components/RouteMap.vue";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import SearchableSelect from "../components/SearchableSelect.vue";
import type {
  Activity,
  ActivityPointRequest,
  ActivityRequest,
  ActivityType,
  ActivityWeeklyStats,
  HouseholdMember,
  Pet
} from "../types";

interface ActivityFormState {
  type: ActivityType;
  performedByUserId: string;
  petId: string;
  startedAt: string;
  endedAt: string;
  distanceKm: number | null;
  gpsTracked: boolean;
  title: string;
  notes: string;
  points: ActivityPointRequest[];
}

const session = useSessionStore();

const members = ref<HouseholdMember[]>([]);
const pets = ref<Pet[]>([]);
const activities = ref<Activity[]>([]);
const errorMessage = ref("");

const selectedUserFilter = ref<string | "all">(session.activityUserFilter ?? "all");
const activityTypeFilter = ref<ActivityType | "all">("all");
const selectedPetFilter = ref<string | "all">("all");
const selectedActivityId = ref("");

const stats = ref<ActivityWeeklyStats>({
  weekStart: "",
  weekEnd: "",
  activitiesCount: 0,
  distanceKm: 0,
  durationMinutes: 0,
  daily: []
});

function toLocalInputValue(value: Date) {
  const offsetMs = value.getTimezoneOffset() * 60 * 1000;
  const localDate = new Date(value.getTime() - offsetMs);
  return localDate.toISOString().slice(0, 16);
}

function toIso(localDateTime: string): string | null {
  if (!localDateTime) return null;
  const date = new Date(localDateTime);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

const now = new Date();
const plusMinutes = new Date(now.getTime() + 30 * 60 * 1000);

const activityForm = reactive<ActivityFormState>({
  type: "RUN",
  performedByUserId: "",
  petId: "",
  startedAt: toLocalInputValue(now),
  endedAt: toLocalInputValue(plusMinutes),
  distanceKm: null,
  gpsTracked: false,
  title: "",
  notes: "",
  points: []
});

const supportsGeolocation = typeof navigator !== "undefined" && "geolocation" in navigator;
const trackingMode = ref<ActivityType>("WALK");
const trackingPetId = ref("");
const trackingInProgress = ref(false);
const trackingPoints = ref<ActivityPointRequest[]>([]);
const trackingStartedAt = ref<Date | null>(null);
const trackingElapsedSeconds = ref(0);
const trackingStatus = ref("Listo para iniciar seguimiento GPS.");
let trackingWatchId: number | null = null;
let trackingTimer: ReturnType<typeof setInterval> | null = null;

const householdId = () => session.activeHouseholdId;
const selectedActivity = computed(() => activities.value.find((activity) => activity.id === selectedActivityId.value) ?? null);
const trackingDistanceKm = computed(() => roundDistanceKm(calculateDistanceKm(trackingPoints.value)));
const userFilterOptions = computed(() => [
  { value: "all", label: "Todos" },
  ...members.value.map((member) => ({ value: member.userId, label: member.displayName }))
]);
const typeFilterOptions = computed(() => [
  { value: "all", label: "Todos" },
  { value: "RUN", label: "Correr" },
  { value: "WALK", label: "Caminar" },
  { value: "BIKE", label: "Bici" },
  { value: "PET_WALK", label: "Paseo mascota" }
]);
const petFilterOptions = computed(() => [
  { value: "all", label: "Todas" },
  ...pets.value.map((pet) => ({ value: pet.id, label: pet.name }))
]);
const trackingModeOptions = computed(() => [
  { value: "RUN", label: "Correr" },
  { value: "WALK", label: "Caminar" },
  { value: "BIKE", label: "Bici" },
  { value: "PET_WALK", label: "Paseo mascota" }
]);
const trackingPetOptions = computed(() => [
  { value: "", label: "Selecciona mascota" },
  ...pets.value.map((pet) => ({ value: pet.id, label: pet.name }))
]);
const activityTypeOptions = trackingModeOptions;
const activityUserOptions = computed(() => [
  { value: "", label: "Yo" },
  ...members.value.map((member) => ({ value: member.userId, label: member.displayName }))
]);
const activityPetOptions = computed(() => [
  { value: "", label: "Selecciona mascota" },
  ...pets.value.map((pet) => ({ value: pet.id, label: pet.name }))
]);

const loadMembers = async () => {
  const id = householdId();
  if (!id) {
    members.value = [];
    return;
  }

  const { data } = await api.get<HouseholdMember[]>(`/api/households/${id}/members`);
  members.value = data;

  if (selectedUserFilter.value !== "all" && !members.value.some((member) => member.userId === selectedUserFilter.value)) {
    selectedUserFilter.value = "all";
  }
};

const loadPets = async () => {
  const id = householdId();
  if (!id) {
    pets.value = [];
    selectedPetFilter.value = "all";
    return;
  }

  const { data } = await api.get<Pet[]>(`/api/households/${id}/pets`);
  pets.value = data;
  if (selectedPetFilter.value !== "all" && !pets.value.some((pet) => pet.id === selectedPetFilter.value)) {
    selectedPetFilter.value = "all";
  }
};

const loadActivities = async () => {
  const id = householdId();
  if (!id) {
    activities.value = [];
    selectedActivityId.value = "";
    return;
  }

  const params: Record<string, string> = {};
  if (selectedUserFilter.value !== "all") {
    params.userId = selectedUserFilter.value;
  }
  if (activityTypeFilter.value !== "all") {
    params.type = activityTypeFilter.value;
  }
  if (selectedPetFilter.value !== "all") {
    params.petId = selectedPetFilter.value;
  }

  const { data } = await api.get<Activity[]>(`/api/households/${id}/activities`, { params });
  activities.value = data;

  if (!activities.value.some((activity) => activity.id === selectedActivityId.value)) {
    selectedActivityId.value = "";
  }
};

const loadStats = async () => {
  const id = householdId();
  if (!id) {
    stats.value = { weekStart: "", weekEnd: "", activitiesCount: 0, distanceKm: 0, durationMinutes: 0, daily: [] };
    return;
  }

  const params: Record<string, string> = {};
  if (selectedUserFilter.value !== "all") {
    params.userId = selectedUserFilter.value;
  }
  if (activityTypeFilter.value !== "all") {
    params.type = activityTypeFilter.value;
  }
  if (selectedPetFilter.value !== "all") {
    params.petId = selectedPetFilter.value;
  }

  const { data } = await api.get<ActivityWeeklyStats>(`/api/households/${id}/activities/stats/weekly`, { params });
  stats.value = data;
};

const reloadAll = async () => {
  errorMessage.value = "";
  try {
    await Promise.all([loadMembers(), loadPets(), loadActivities(), loadStats()]);
  } catch {
    errorMessage.value = "No se pudo cargar el modulo de actividades.";
  }
};

const addPoint = () => {
  activityForm.points.push({
    latitude: 0,
    longitude: 0,
    recordedAt: null
  });
  activityForm.gpsTracked = true;
};

const removePoint = (index: number) => {
  activityForm.points.splice(index, 1);
  if (activityForm.points.length === 0) {
    activityForm.gpsTracked = false;
  }
};

const resetForm = () => {
  activityForm.type = "RUN";
  activityForm.performedByUserId = "";
  activityForm.petId = "";
  activityForm.startedAt = toLocalInputValue(new Date());
  activityForm.endedAt = toLocalInputValue(new Date(Date.now() + 30 * 60 * 1000));
  activityForm.distanceKm = null;
  activityForm.gpsTracked = false;
  activityForm.title = "";
  activityForm.notes = "";
  activityForm.points = [];
};

const cleanNullable = (value: string) => {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
};

const submitActivity = async (payload: ActivityRequest) => {
  const id = householdId();
  if (!id) return;
  await api.post(`/api/households/${id}/activities`, payload);
  await Promise.all([loadActivities(), loadStats()]);
};

const createActivity = async () => {
  const id = householdId();
  if (!id) return;

  errorMessage.value = "";
  if (activityForm.type === "PET_WALK" && !activityForm.petId) {
    errorMessage.value = "Selecciona una mascota para registrar el paseo.";
    return;
  }

  const startedAt = toIso(activityForm.startedAt);
  const endedAt = toIso(activityForm.endedAt);
  if (!startedAt || !endedAt) {
    errorMessage.value = "Debes indicar inicio y fin validos.";
    return;
  }

  const payload: ActivityRequest = {
    type: activityForm.type,
    performedByUserId: activityForm.performedByUserId || null,
    petId: activityForm.petId || null,
    startedAt,
    endedAt,
    distanceKm: activityForm.distanceKm,
    gpsTracked: activityForm.gpsTracked,
    title: cleanNullable(activityForm.title),
    notes: cleanNullable(activityForm.notes),
    points: activityForm.points
      .filter((point) => Number.isFinite(point.latitude) && Number.isFinite(point.longitude))
      .map((point) => ({
        latitude: point.latitude,
        longitude: point.longitude,
        recordedAt: null
      }))
  };

  try {
    await submitActivity(payload);
    resetForm();
  } catch {
    errorMessage.value = "No se pudo guardar la actividad.";
  }
};

const clearTrackingWatcher = () => {
  if (trackingWatchId != null && supportsGeolocation) {
    navigator.geolocation.clearWatch(trackingWatchId);
    trackingWatchId = null;
  }
  if (trackingTimer) {
    clearInterval(trackingTimer);
    trackingTimer = null;
  }
};

const appendTrackingPoint = (latitude: number, longitude: number, recordedAt: Date) => {
  const last = trackingPoints.value[trackingPoints.value.length - 1];
  if (last) {
    const distanceMeters = haversineMeters(last.latitude, last.longitude, latitude, longitude);
    if (distanceMeters < 2) {
      return;
    }
  }

  trackingPoints.value.push({
    latitude,
    longitude,
    recordedAt: recordedAt.toISOString()
  });
};

const startTracking = () => {
  errorMessage.value = "";
  if (!supportsGeolocation) {
    errorMessage.value = "Tu navegador no soporta geolocalizacion.";
    return;
  }
  if (trackingInProgress.value) {
    return;
  }
  if (trackingMode.value === "PET_WALK" && !trackingPetId.value) {
    errorMessage.value = "Selecciona mascota para iniciar el paseo GPS.";
    return;
  }

  trackingPoints.value = [];
  trackingStartedAt.value = new Date();
  trackingElapsedSeconds.value = 0;
  trackingInProgress.value = true;
  trackingStatus.value = "Solicitando posicion GPS...";

  navigator.geolocation.getCurrentPosition(
    (position) => {
      appendTrackingPoint(position.coords.latitude, position.coords.longitude, new Date(position.timestamp));
      trackingStatus.value = "GPS activo. Registrando recorrido.";
    },
    () => {
      trackingStatus.value = "Esperando primera posicion...";
    },
    { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
  );

  trackingWatchId = navigator.geolocation.watchPosition(
    (position) => {
      appendTrackingPoint(position.coords.latitude, position.coords.longitude, new Date(position.timestamp));
      trackingStatus.value = "GPS activo. Registrando recorrido.";
    },
    (error) => {
      trackingStatus.value = `GPS error: ${error.message}`;
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  );

  trackingTimer = setInterval(() => {
    if (!trackingStartedAt.value) {
      trackingElapsedSeconds.value = 0;
      return;
    }
    trackingElapsedSeconds.value = Math.max(0, Math.floor((Date.now() - trackingStartedAt.value.getTime()) / 1000));
  }, 1000);
};

const stopTrackingAndSave = async () => {
  if (!trackingInProgress.value || !trackingStartedAt.value) {
    return;
  }

  clearTrackingWatcher();
  trackingInProgress.value = false;

  if (trackingPoints.value.length < 2) {
    errorMessage.value = "No hay suficientes puntos GPS para guardar la actividad.";
    trackingStatus.value = "Seguimiento detenido sin guardar.";
    trackingPoints.value = [];
    return;
  }

  const startedAt = trackingStartedAt.value.toISOString();
  const endedAt = new Date().toISOString();

  const payload: ActivityRequest = {
    type: trackingMode.value,
    performedByUserId: null,
    petId: trackingMode.value === "PET_WALK" ? trackingPetId.value || null : null,
    startedAt,
    endedAt,
    distanceKm: trackingDistanceKm.value,
    gpsTracked: true,
    title: null,
    notes: null,
    points: trackingPoints.value.map((point) => ({
      latitude: point.latitude,
      longitude: point.longitude,
      recordedAt: point.recordedAt ?? null
    }))
  };

  try {
    await submitActivity(payload);
    trackingStatus.value = "Actividad GPS guardada correctamente.";
  } catch {
    errorMessage.value = "No se pudo guardar la actividad GPS.";
    trackingStatus.value = "Seguimiento detenido con error al guardar.";
  } finally {
    trackingPoints.value = [];
    trackingStartedAt.value = null;
    trackingElapsedSeconds.value = 0;
  }
};

const cancelTracking = () => {
  clearTrackingWatcher();
  trackingInProgress.value = false;
  trackingPoints.value = [];
  trackingStartedAt.value = null;
  trackingElapsedSeconds.value = 0;
  trackingStatus.value = "Seguimiento cancelado.";
};

const activityTypeLabel = (type: ActivityType) => {
  if (type === "RUN") return "Correr";
  if (type === "WALK") return "Caminar";
  if (type === "BIKE") return "Bici";
  return "Paseo mascota";
};

const memberName = (userId: string) =>
  members.value.find((member) => member.userId === userId)?.displayName ?? userId.slice(0, 8);

const petName = (petId: string | null) => {
  if (!petId) return "-";
  return pets.value.find((pet) => pet.id === petId)?.name ?? "Mascota";
};

const formatDate = (value: string) => {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString();
};

const formatDateTime = (value: string) => {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
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

const haversineMeters = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
  const earthRadius = 6371000;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2)
    + Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180)
    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadius * c;
};

const calculateDistanceKm = (points: ActivityPointRequest[]): number => {
  if (points.length < 2) {
    return 0;
  }
  let totalMeters = 0;
  for (let i = 1; i < points.length; i++) {
    const previous = points[i - 1];
    const current = points[i];
    totalMeters += haversineMeters(previous.latitude, previous.longitude, current.latitude, current.longitude);
  }
  return totalMeters / 1000;
};

const roundDistanceKm = (distanceKm: number) => Math.round(distanceKm * 1000) / 1000;

watch(
  () => session.activeHouseholdId,
  () => {
    selectedUserFilter.value = session.activityUserFilter ?? "all";
    activityTypeFilter.value = "all";
    selectedPetFilter.value = "all";
    selectedActivityId.value = "";
    resetForm();
    cancelTracking();
    void reloadAll();
  },
  { immediate: true }
);

watch(
  () => selectedUserFilter.value,
  (value) => {
    session.setActivityUserFilter(value);
    void Promise.all([loadActivities(), loadStats()]);
  }
);

watch(
  () => activityTypeFilter.value,
  () => {
    void Promise.all([loadActivities(), loadStats()]);
  }
);

watch(
  () => selectedPetFilter.value,
  () => {
    void Promise.all([loadActivities(), loadStats()]);
  }
);

watch(
  () => activityForm.type,
  (newType) => {
    if (newType !== "PET_WALK") {
      activityForm.petId = "";
    }
  }
);

watch(
  () => trackingMode.value,
  (newType) => {
    if (newType !== "PET_WALK") {
      trackingPetId.value = "";
    }
  }
);

onBeforeUnmount(() => {
  clearTrackingWatcher();
});
</script>
