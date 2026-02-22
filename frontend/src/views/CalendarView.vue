<template>
  <section class="space-y-5">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-xl font-semibold">Calendario inteligente</h2>
        <p class="mt-1 text-sm text-slate-600">Eventos con recurrencia, recordatorios y categorias.</p>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <button
          type="button"
          class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
          @click="goToday"
        >
          Hoy
        </button>
        <button type="button" class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50" @click="shift(-1)">
          ◀
        </button>
        <button type="button" class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50" @click="shift(1)">
          ▶
        </button>
        <div class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white">
          {{ periodLabel }}
        </div>
      </div>
    </div>

    <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{{ errorMessage }}</p>

    <article class="grid gap-3 rounded-xl border border-slate-200 p-4 md:grid-cols-5">
      <div class="flex gap-2">
        <button
          type="button"
          class="rounded-lg px-3 py-2 text-sm"
          :class="viewMode === 'month' ? 'bg-slate-900 text-white' : 'border border-slate-300 hover:bg-slate-50'"
          @click="viewMode = 'month'"
        >
          Mes
        </button>
        <button
          type="button"
          class="rounded-lg px-3 py-2 text-sm"
          :class="viewMode === 'week' ? 'bg-slate-900 text-white' : 'border border-slate-300 hover:bg-slate-50'"
          @click="viewMode = 'week'"
        >
          Semana
        </button>
        <button
          type="button"
          class="rounded-lg px-3 py-2 text-sm"
          :class="viewMode === 'day' ? 'bg-slate-900 text-white' : 'border border-slate-300 hover:bg-slate-50'"
          @click="viewMode = 'day'"
        >
          Dia
        </button>
      </div>

      <div>
        <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Filtrar por usuario
        </label>
        <SearchableSelect v-model="selectedUserFilter" :options="userOptions" placeholder="Todos" />
      </div>

      <div>
        <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
          Tipo
        </label>
        <SearchableSelect v-model="selectedTypeFilter" :options="typeOptions" placeholder="Todos" />
      </div>

      <button
        type="button"
        class="self-end rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
        @click="loadEvents"
      >
        Recargar
      </button>

      <div class="md:col-span-5 flex flex-wrap items-center justify-between gap-2">
        <div class="text-xs text-slate-500">
          Google Calendar:
          <span :class="googleStatus.linked ? 'text-emerald-600' : 'text-slate-500'">
            {{ googleStatus.linked ? "Conectado" : "Sin conectar" }}
          </span>
        </div>
        <div class="flex flex-wrap gap-2">
          <a
            v-if="!googleStatus.linked"
            class="inline-flex items-center gap-2 rounded border border-slate-300 px-3 py-2 text-xs hover:bg-slate-50"
            :href="googleAuthUrl"
          >
            Conectar Google Calendar
          </a>
          <button
            v-else
            type="button"
            class="inline-flex items-center gap-2 rounded border border-slate-300 px-3 py-2 text-xs hover:bg-slate-50"
            @click="syncFromGoogle"
          >
            Sincronizar desde Google
          </button>
        </div>
      </div>
    </article>

    <div class="grid gap-4 lg:grid-cols-[2fr_1fr]">
      <section class="space-y-3 rounded-xl border border-slate-200 p-4">
        <div v-if="viewMode === 'month'" class="grid grid-cols-7 gap-2 text-xs uppercase text-slate-500">
          <span v-for="day in weekDays" :key="day">{{ day }}</span>
        </div>

        <div
          v-if="viewMode === 'month'"
          class="grid grid-cols-7 gap-2"
        >
          <button
            v-for="day in monthDays"
            :key="day.toISOString()"
            type="button"
            class="min-h-[72px] rounded-lg border px-2 py-2 text-left text-sm transition"
            :class="dayButtonClass(day)"
            @click="selectDate(day)"
          >
            <div class="flex items-center justify-between">
              <span>{{ day.getDate() }}</span>
              <span v-if="isToday(day)" class="text-xs font-semibold text-emerald-600">Hoy</span>
            </div>
            <ul class="mt-2 space-y-1">
              <li
                v-for="event in eventsForDay(day).slice(0, 2)"
                :key="event.id + event.occurrenceStartAt"
                class="truncate rounded px-1 text-xs"
                :style="{ color: event.colorHex || '#0f172a' }"
              >
                {{ event.title }}
              </li>
              <li v-if="eventsForDay(day).length > 2" class="text-[10px] text-slate-400">
                +{{ eventsForDay(day).length - 2 }} mas
              </li>
            </ul>
          </button>
        </div>

        <div v-else class="space-y-2">
          <div v-for="day in rangeDays" :key="day.toISOString()" class="rounded-lg border border-slate-200 p-3">
            <div class="flex items-center justify-between">
              <p class="text-sm font-semibold text-slate-900">{{ formatDate(day) }}</p>
              <button
                type="button"
                class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
                @click="selectDate(day)"
              >
                Ver dia
              </button>
            </div>
            <ul class="mt-2 space-y-1 text-sm text-slate-700">
              <li v-for="event in eventsForDay(day)" :key="event.id + event.occurrenceStartAt" class="flex gap-2">
                <span class="text-xs text-slate-400">{{ formatTime(event.occurrenceStartAt) }}</span>
                <span :style="{ color: event.colorHex || '#0f172a' }">{{ event.title }}</span>
              </li>
              <li v-if="eventsForDay(day).length === 0" class="text-xs text-slate-400">Sin eventos.</li>
            </ul>
          </div>
        </div>
      </section>

      <aside class="space-y-3">
        <article class="rounded-xl border border-slate-200 p-4">
          <h3 class="text-sm font-medium text-slate-800">
            {{ editingEventId ? "Editar evento" : "Nuevo evento" }}
          </h3>

          <form class="mt-3 space-y-3" @submit.prevent="saveEvent">
            <div>
              <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Titulo</label>
              <input v-model="form.title" required class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            </div>

            <div>
              <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Descripcion</label>
              <textarea v-model="form.description" rows="2" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"></textarea>
            </div>

            <div class="grid gap-2 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Inicio</label>
                <input v-model="form.startAt" type="datetime-local" required class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              </div>
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Fin</label>
                <input v-model="form.endAt" type="datetime-local" required class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              </div>
            </div>

            <div class="grid gap-2 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Tipo</label>
                <SearchableSelect v-model="form.type" :options="calendarTypeOptions" />
              </div>
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Color</label>
                <input v-model="form.colorHex" type="color" class="h-10 w-full rounded-lg border border-slate-300" />
              </div>
            </div>

            <label class="inline-flex items-center gap-2 text-sm text-slate-700">
              <input v-model="form.allDay" type="checkbox" class="toggle-modern" />
              Todo el dia
            </label>

            <div class="grid gap-2 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Recurrencia</label>
                <SearchableSelect v-model="form.recurrenceFrequency" :options="recurrenceOptions" />
              </div>
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Cada (intervalo)</label>
                <input v-model.number="form.recurrenceInterval" min="1" type="number" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              </div>
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Repeticiones</label>
                <input v-model.number="form.recurrenceCount" min="1" type="number" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              </div>
              <div>
                <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Hasta</label>
                <input v-model="form.recurrenceUntil" type="date" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              </div>
            </div>

            <div>
              <label class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">Recordatorio (min)</label>
              <input v-model.number="form.reminderMinutesBefore" min="0" type="number" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            </div>

            <div class="flex flex-wrap gap-2">
              <button type="submit" class="rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700">
                {{ editingEventId ? "Actualizar" : "Crear" }}
              </button>
              <button
                v-if="editingEventId"
                type="button"
                class="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
                @click="resetForm"
              >
                Cancelar
              </button>
            </div>
          </form>
        </article>

        <article class="rounded-xl border border-slate-200 p-4">
          <h3 class="text-sm font-medium text-slate-800">Eventos del dia</h3>
          <p class="text-xs text-slate-500">{{ formatDate(selectedDate) }}</p>
          <ul class="mt-3 space-y-2 text-sm">
            <li v-for="event in eventsForDay(selectedDate)" :key="event.id + event.occurrenceStartAt" class="rounded-lg border border-slate-100 p-2">
              <div class="flex items-center justify-between gap-2">
                <div>
                  <p class="font-medium" :style="{ color: event.colorHex || '#0f172a' }">
                    {{ event.title }}
                  </p>
                  <p class="text-xs text-slate-500">
                    {{ formatTime(event.occurrenceStartAt) }} - {{ formatTime(event.occurrenceEndAt) }}
                  </p>
                  <p class="text-xs">
                    <span :style="{ color: memberColor(event.createdByUserId) }">
                      {{ memberName(event.createdByUserId) }}
                    </span>
                  </p>
                </div>
                <div class="flex gap-2">
                  <button
                    type="button"
                    class="inline-flex items-center gap-1 rounded border border-amber-300 px-2 py-1 text-xs text-amber-700 hover:bg-amber-50"
                    @click="editEvent(event)"
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
                    @click="deleteEvent(event.id)"
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
            </li>
            <li v-if="eventsForDay(selectedDate).length === 0" class="text-xs text-slate-500">
              Sin eventos.
            </li>
          </ul>
        </article>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { api, apiBaseUrl } from "../lib/api";
import { useSessionStore } from "../stores/session";
import SearchableSelect from "../components/SearchableSelect.vue";
import type {
  CalendarEvent,
  CalendarEventRequest,
  CalendarEventType,
  CalendarRecurrenceFrequency,
  GoogleCalendarStatus,
  HouseholdMember
} from "../types";

const session = useSessionStore();
const errorMessage = ref("");

const viewMode = ref<"month" | "week" | "day">("month");
const cursorDate = ref(new Date());
const selectedDate = ref(new Date());

const members = ref<HouseholdMember[]>([]);
const events = ref<CalendarEvent[]>([]);
const selectedUserFilter = ref<string | "all">("all");
const selectedTypeFilter = ref<CalendarEventType | "all">("all");
const editingEventId = ref<string | null>(null);
const googleStatus = ref<GoogleCalendarStatus>({
  linked: false,
  calendarId: null,
  calendarName: "casaChiquitos"
});

const weekDays = ["Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"];
const calendarTypeOptions = [
  { value: "WORK", label: "Trabajo" },
  { value: "TRAINING", label: "Entrenamiento" },
  { value: "TASK", label: "Tarea" },
  { value: "PERSONAL", label: "Personal" },
  { value: "OTHER", label: "Otro" }
];
const recurrenceOptions = [
  { value: "NONE", label: "Sin recurrencia" },
  { value: "DAILY", label: "Diario" },
  { value: "WEEKLY", label: "Semanal" },
  { value: "MONTHLY", label: "Mensual" }
];

const form = reactive({
  title: "",
  description: "",
  startAt: "",
  endAt: "",
  type: "TASK" as CalendarEventType,
  colorHex: "#0f172a",
  allDay: false,
  recurrenceFrequency: "NONE" as CalendarRecurrenceFrequency,
  recurrenceInterval: 1,
  recurrenceCount: null as number | null,
  recurrenceUntil: "" as string,
  reminderMinutesBefore: 15,
  assignedToUserId: null as string | null
});

const periodLabel = computed(() => {
  const date = cursorDate.value;
  if (viewMode.value === "month") {
    return date.toLocaleDateString(undefined, { month: "long", year: "numeric" });
  }
  if (viewMode.value === "week") {
    const start = startOfWeek(date);
    const end = addDays(start, 6);
    return `${formatDate(start)} - ${formatDate(end)}`;
  }
  return formatDate(date);
});

const userOptions = computed(() => [
  { value: "all", label: "Todos" },
  ...members.value.map((member) => ({ value: member.userId, label: member.displayName }))
]);

const typeOptions = computed(() => [
  { value: "all", label: "Todos" },
  ...calendarTypeOptions
]);

const householdId = () => session.activeHouseholdId;
const googleAuthUrl = computed(() => `${apiBaseUrl}/oauth2/authorization/google?calendar=1`);

const monthDays = computed(() => {
  const start = startOfWeek(startOfMonth(cursorDate.value));
  const end = endOfWeek(endOfMonth(cursorDate.value));
  const days: Date[] = [];
  let current = new Date(start);
  while (current <= end) {
    days.push(new Date(current));
    current = addDays(current, 1);
  }
  return days;
});

const rangeDays = computed(() => {
  if (viewMode.value === "day") {
    return [new Date(cursorDate.value)];
  }
  const start = startOfWeek(cursorDate.value);
  return Array.from({ length: 7 }, (_, idx) => addDays(start, idx));
});

const loadMembers = async () => {
  const id = householdId();
  if (!id) {
    members.value = [];
    return;
  }

  const { data } = await api.get<HouseholdMember[]>(`/api/households/${id}/members`);
  members.value = data;
};

const loadEvents = async () => {
  const id = householdId();
  if (!id) {
    events.value = [];
    return;
  }

  errorMessage.value = "";

  const range = rangeForView();
  const params: Record<string, string> = {
    from: range.from.toISOString(),
    to: range.to.toISOString()
  };
  if (selectedUserFilter.value !== "all") {
    params.createdByUserId = selectedUserFilter.value;
  }
  if (selectedTypeFilter.value !== "all") {
    params.type = selectedTypeFilter.value;
  }

  try {
    const { data } = await api.get<CalendarEvent[]>(`/api/households/${id}/calendar/events`, { params });
    events.value = data;
  } catch {
    errorMessage.value = "No se pudieron cargar los eventos.";
  }
};

const loadGoogleStatus = async () => {
  const id = householdId();
  if (!id) {
    googleStatus.value = { linked: false, calendarId: null, calendarName: "casaChiquitos" };
    return;
  }

  try {
    const { data } = await api.get<GoogleCalendarStatus>(`/api/households/${id}/calendar/events/google/status`);
    googleStatus.value = data;
  } catch {
    googleStatus.value = { linked: false, calendarId: null, calendarName: "casaChiquitos" };
  }
};

const syncFromGoogle = async () => {
  const id = householdId();
  if (!id) return;
  await api.post(`/api/households/${id}/calendar/events/google/sync`);
  await loadEvents();
};

const saveEvent = async () => {
  const id = householdId();
  if (!id) return;

  errorMessage.value = "";
  const payload: CalendarEventRequest = {
    title: form.title.trim(),
    description: form.description.trim() || null,
    startAt: toIso(form.startAt),
    endAt: toIso(form.endAt),
    type: form.type,
    colorHex: form.colorHex || null,
    allDay: form.allDay,
    recurrenceFrequency: form.recurrenceFrequency,
    recurrenceInterval: form.recurrenceInterval || 1,
    recurrenceCount: form.recurrenceCount,
    recurrenceUntil: form.recurrenceUntil ? toIso(`${form.recurrenceUntil}T23:59`) : null,
    reminderMinutesBefore: form.reminderMinutesBefore,
    assignedToUserId: form.assignedToUserId
  };

  try {
    if (editingEventId.value) {
      await api.put(`/api/households/${id}/calendar/events/${editingEventId.value}`, payload);
    } else {
      await api.post(`/api/households/${id}/calendar/events`, payload);
    }
    resetForm();
    await loadEvents();
  } catch {
    errorMessage.value = "No se pudo guardar el evento.";
  }
};

const editEvent = (event: CalendarEvent) => {
  editingEventId.value = event.id;
  form.title = event.title;
  form.description = event.description ?? "";
  form.startAt = toLocalInputValue(new Date(event.startAt));
  form.endAt = toLocalInputValue(new Date(event.endAt));
  form.type = event.type;
  form.colorHex = event.colorHex ?? "#0f172a";
  form.allDay = event.allDay;
  form.recurrenceFrequency = event.recurrenceFrequency ?? "NONE";
  form.recurrenceInterval = event.recurrenceInterval || 1;
  form.recurrenceCount = event.recurrenceCount;
  form.recurrenceUntil = event.recurrenceUntil ? event.recurrenceUntil.slice(0, 10) : "";
  form.reminderMinutesBefore = event.reminderMinutesBefore ?? 15;
  form.assignedToUserId = event.assignedToUserId ?? null;
};

const deleteEvent = async (eventId: string) => {
  const id = householdId();
  if (!id) return;
  if (!window.confirm("Se eliminara el evento. Quieres continuar?")) return;

  await api.delete(`/api/households/${id}/calendar/events/${eventId}`);
  await loadEvents();
};

const resetForm = () => {
  editingEventId.value = null;
  form.title = "";
  form.description = "";
  const now = new Date();
  form.startAt = toLocalInputValue(now);
  form.endAt = toLocalInputValue(addMinutes(now, 60));
  form.type = "TASK";
  form.colorHex = "#0f172a";
  form.allDay = false;
  form.recurrenceFrequency = "NONE";
  form.recurrenceInterval = 1;
  form.recurrenceCount = null;
  form.recurrenceUntil = "";
  form.reminderMinutesBefore = 15;
  form.assignedToUserId = null;
};

const rangeForView = () => {
  if (viewMode.value === "month") {
    return {
      from: startOfWeek(startOfMonth(cursorDate.value)),
      to: endOfWeek(endOfMonth(cursorDate.value))
    };
  }
  if (viewMode.value === "week") {
    const start = startOfWeek(cursorDate.value);
    return { from: start, to: endOfWeek(start) };
  }
  const day = startOfDay(cursorDate.value);
  return { from: day, to: endOfDay(day) };
};

const shift = (delta: number) => {
  if (viewMode.value === "month") {
    cursorDate.value = new Date(cursorDate.value.getFullYear(), cursorDate.value.getMonth() + delta, 1);
  } else if (viewMode.value === "week") {
    cursorDate.value = addDays(cursorDate.value, delta * 7);
  } else {
    cursorDate.value = addDays(cursorDate.value, delta);
  }
  loadEvents();
};

const goToday = () => {
  cursorDate.value = new Date();
  selectedDate.value = new Date();
  loadEvents();
};

const selectDate = (day: Date) => {
  selectedDate.value = new Date(day);
  if (viewMode.value === "day") {
    cursorDate.value = new Date(day);
  }
};

const eventsForDay = (day: Date) => {
  const start = startOfDay(day);
  const end = endOfDay(day);
  return events.value.filter((event) => overlaps(event.occurrenceStartAt, event.occurrenceEndAt, start, end));
};

const dayButtonClass = (day: Date) => {
  const isCurrentMonth = day.getMonth() === cursorDate.value.getMonth();
  const isSelected = isSameDay(day, selectedDate.value);
  return [
    isSelected ? "border-slate-900 bg-slate-50" : "border-slate-200 hover:bg-slate-50",
    isCurrentMonth ? "" : "opacity-50"
  ].join(" ");
};

const memberName = (userId: string) =>
  members.value.find((member) => member.userId === userId)?.displayName ?? userId.slice(0, 8);

const memberColor = (userId: string) =>
  members.value.find((member) => member.userId === userId)?.colorHex ?? "#0f172a";

const formatDate = (value: Date) => value.toLocaleDateString();

const formatTime = (value: string) => {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const toIso = (localDateTime: string) => {
  const date = new Date(localDateTime);
  return date.toISOString();
};

const toLocalInputValue = (value: Date) => {
  const offsetMs = value.getTimezoneOffset() * 60 * 1000;
  const localDate = new Date(value.getTime() - offsetMs);
  return localDate.toISOString().slice(0, 16);
};

const isSameDay = (a: Date, b: Date) =>
  a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();

const isToday = (day: Date) => isSameDay(day, new Date());

const startOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth(), 1);
const endOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);

const startOfDay = (date: Date) => new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0);
const endOfDay = (date: Date) => new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59);

const startOfWeek = (date: Date) => {
  const day = date.getDay();
  const diff = (day === 0 ? -6 : 1) - day;
  return startOfDay(addDays(date, diff));
};

const endOfWeek = (date: Date) => endOfDay(addDays(startOfWeek(date), 6));

const addDays = (date: Date, days: number) => {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + days);
  return copy;
};

const addMinutes = (date: Date, minutes: number) => new Date(date.getTime() + minutes * 60 * 1000);

const overlaps = (startIso: string, endIso: string, from: Date, to: Date) => {
  const start = new Date(startIso);
  const end = new Date(endIso);
  return !(end < from || start > to);
};

watch(
  () => [session.activeHouseholdId, viewMode.value],
  () => {
    resetForm();
    void loadMembers();
    void loadGoogleStatus();
    void loadEvents();
  },
  { immediate: true }
);

watch(
  () => [selectedUserFilter.value, selectedTypeFilter.value],
  () => {
    void loadEvents();
  }
);

onMounted(() => {
  resetForm();
});
</script>
