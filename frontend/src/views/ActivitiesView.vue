<template>
  <section class="space-y-4">
    <div>
      <h2 class="text-xl font-semibold">Actividades deportivas</h2>
      <p class="mt-1 text-sm text-slate-600">
        Historico con filtro por usuario, registro de recorridos y estadisticas semanales.
      </p>
    </div>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

    <article class="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-3">
      <div>
        <label for="activity-user-filter" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Usuario
        </label>
        <select
          id="activity-user-filter"
          v-model="selectedUserFilter"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="all">Todos</option>
          <option v-for="member in members" :key="member.userId" :value="member.userId">
            {{ member.displayName }}
          </option>
        </select>
      </div>

      <div>
        <label for="activity-type-filter" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Tipo actividad
        </label>
        <select
          id="activity-type-filter"
          v-model="activityTypeFilter"
          class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="all">Todos</option>
          <option value="RUN">Correr</option>
          <option value="WALK">Caminar</option>
          <option value="BIKE">Bici</option>
          <option value="PET_WALK">Paseo mascota</option>
        </select>
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
      <h3 class="text-sm font-medium text-slate-800">Registrar actividad</h3>
      <form class="grid gap-3 md:grid-cols-3" @submit.prevent="createActivity">
        <div>
          <label for="activity-type" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Tipo
          </label>
          <select id="activity-type" v-model="activityForm.type" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
            <option value="RUN">Correr</option>
            <option value="WALK">Caminar</option>
            <option value="BIKE">Bici</option>
            <option value="PET_WALK">Paseo mascota</option>
          </select>
        </div>

        <div>
          <label for="activity-user" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Usuario
          </label>
          <select id="activity-user" v-model="activityForm.performedByUserId" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
            <option value="">Yo</option>
            <option v-for="member in members" :key="member.userId" :value="member.userId">
              {{ member.displayName }}
            </option>
          </select>
        </div>

        <div v-if="activityForm.type === 'PET_WALK'">
          <label for="activity-pet" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Mascota
          </label>
          <select id="activity-pet" v-model="activityForm.petId" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
            <option value="">Selecciona mascota</option>
            <option v-for="pet in pets" :key="pet.id" :value="pet.id">{{ pet.name }}</option>
          </select>
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
          <input v-model="activityForm.gpsTracked" type="checkbox" class="rounded border-slate-300" />
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

          <div v-if="activityForm.points.length > 1" class="rounded-lg border border-slate-200 p-2">
            <svg viewBox="0 0 300 100" class="h-28 w-full">
              <polyline fill="none" stroke="#0f172a" stroke-width="2" :points="formRoutePolyline"></polyline>
            </svg>
          </div>
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
                <svg v-if="activity.points.length > 1" viewBox="0 0 80 28" class="h-7 w-24">
                  <polyline fill="none" stroke="#0f172a" stroke-width="1.5" :points="routePolyline(activity.points, 80, 28)"></polyline>
                </svg>
                <span v-else class="text-xs text-slate-400">Sin GPS</span>
              </td>
            </tr>
            <tr v-if="activities.length === 0">
              <td colspan="7" class="px-3 py-6 text-center text-slate-500">No hay actividades registradas.</td>
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
  Activity,
  ActivityPoint,
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

const householdId = () => session.activeHouseholdId;

const formRoutePolyline = computed(() => routePolyline(activityForm.points, 300, 100));

const loadMembers = async () => {
  const id = householdId();
  if (!id) {
    members.value = [];
    return;
  }

  const { data } = await api.get<HouseholdMember[]>(`/api/households/${id}/members`);
  members.value = data;
};

const loadPets = async () => {
  const id = householdId();
  if (!id) {
    pets.value = [];
    return;
  }

  const { data } = await api.get<Pet[]>(`/api/households/${id}/pets`);
  pets.value = data;
};

const loadActivities = async () => {
  const id = householdId();
  if (!id) {
    activities.value = [];
    return;
  }

  const params: Record<string, string> = {};
  if (selectedUserFilter.value !== "all") {
    params.userId = selectedUserFilter.value;
  }
  if (activityTypeFilter.value !== "all") {
    params.type = activityTypeFilter.value;
  }

  const { data } = await api.get<Activity[]>(`/api/households/${id}/activities`, { params });
  activities.value = data;
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
    await api.post(`/api/households/${id}/activities`, payload);
    resetForm();
    await Promise.all([loadActivities(), loadStats()]);
  } catch {
    errorMessage.value = "No se pudo guardar la actividad.";
  }
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

const routePolyline = (points: Array<ActivityPoint | ActivityPointRequest>, width: number, height: number) => {
  if (!points || points.length < 2) {
    return "";
  }

  const latitudes = points.map((point) => point.latitude);
  const longitudes = points.map((point) => point.longitude);
  const minLat = Math.min(...latitudes);
  const maxLat = Math.max(...latitudes);
  const minLng = Math.min(...longitudes);
  const maxLng = Math.max(...longitudes);
  const latSpan = maxLat - minLat || 1;
  const lngSpan = maxLng - minLng || 1;

  return points
    .map((point) => {
      const x = ((point.longitude - minLng) / lngSpan) * (width - 4) + 2;
      const y = height - (((point.latitude - minLat) / latSpan) * (height - 4) + 2);
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(" ");
};

const cleanNullable = (value: string) => {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
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

watch(
  () => session.activeHouseholdId,
  () => {
    selectedUserFilter.value = session.activityUserFilter ?? "all";
    activityTypeFilter.value = "all";
    resetForm();
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
  () => activityForm.type,
  (newType) => {
    if (newType !== "PET_WALK") {
      activityForm.petId = "";
    }
  }
);
</script>
