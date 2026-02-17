<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-start justify-between gap-3">
      <div>
        <h2 class="text-2xl font-semibold">Dashboard principal</h2>
        <p class="text-sm text-slate-600">Resumen operativo del hogar activo.</p>
      </div>
      <p v-if="session.activeHousehold" class="rounded-lg bg-slate-100 px-3 py-2 text-xs text-slate-600">
        Codigo de invitacion: <span class="font-semibold text-slate-900">{{ session.activeHousehold.inviteCode }}</span>
      </p>
    </div>

    <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
      <article class="rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium">Bajo minimo</h3>
        <p class="mt-2 text-2xl font-semibold">{{ lowStockCount }}</p>
      </article>
      <article class="rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium">Caducidad proxima</h3>
        <p class="mt-2 text-2xl font-semibold">{{ expiringSoonCount }}</p>
      </article>
      <article class="rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium">No leidas</h3>
        <p class="mt-2 text-2xl font-semibold">{{ unreadNotifications }}</p>
      </article>
      <article class="rounded-xl border border-slate-200 p-4">
        <h3 class="text-sm font-medium">Usuarios en hogar</h3>
        <p class="mt-2 text-2xl font-semibold">{{ session.households.length > 0 ? 'OK' : '-' }}</p>
      </article>
    </div>

    <section class="rounded-xl border border-slate-200 p-4">
      <h3 class="text-sm font-medium">Alertas recientes</h3>
      <ul v-if="recentNotifications.length" class="mt-3 space-y-2 text-sm">
        <li
          v-for="notification in recentNotifications"
          :key="notification.id"
          class="rounded-lg border border-slate-200 px-3 py-2"
        >
          <p class="font-medium text-slate-900">{{ notification.title }}</p>
          <p class="text-slate-600">{{ notification.body }}</p>
        </li>
      </ul>
      <p v-else class="mt-3 text-sm text-slate-500">No hay alertas para este usuario.</p>
    </section>

    <section class="rounded-xl border border-slate-200 p-4">
      <h3 class="text-sm font-medium">Unirse a hogar compartido</h3>
      <form class="mt-3 flex flex-wrap gap-2" @submit.prevent="joinHousehold">
        <div class="min-w-56">
          <label for="join-household-code" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
            Codigo de invitacion
          </label>
          <input
            id="join-household-code"
            v-model="joinCode"
            type="text"
            maxlength="16"
            placeholder="Ej: AB12CD34"
            class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>
        <button
          type="submit"
          class="self-end rounded-lg bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700"
        >
          Unirme
        </button>
      </form>
      <p v-if="joinError" class="mt-2 text-sm text-red-700">{{ joinError }}</p>
      <p v-if="joinSuccess" class="mt-2 text-sm text-emerald-700">{{ joinSuccess }}</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import type { MeResponse, PantryItem, UserNotification } from "../types";

const session = useSessionStore();

const lowStockCount = ref(0);
const expiringSoonCount = ref(0);
const unreadNotifications = ref(0);
const recentNotifications = ref<UserNotification[]>([]);
const joinCode = ref("");
const joinError = ref("");
const joinSuccess = ref("");

const load = async () => {
  const householdId = session.activeHouseholdId;
  if (!householdId) {
    return;
  }

  const [pantryResponse, unreadResponse, notificationsResponse] = await Promise.all([
    api.get<PantryItem[]>(`/api/households/${householdId}/pantry-items`),
    api.get<{ unreadCount: number }>(`/api/households/${householdId}/notifications/unread-count`),
    api.get<UserNotification[]>(`/api/households/${householdId}/notifications`)
  ]);

  const pantry = pantryResponse.data;
  lowStockCount.value = pantry.filter((item) => item.belowMinimum).length;
  expiringSoonCount.value = pantry.filter((item) => item.expiresSoon).length;
  unreadNotifications.value = unreadResponse.data.unreadCount;
  recentNotifications.value = notificationsResponse.data.slice(0, 5);
};

watch(
  () => session.activeHouseholdId,
  () => {
    void load();
  },
  { immediate: true }
);

const joinHousehold = async () => {
  joinError.value = "";
  joinSuccess.value = "";

  const inviteCode = joinCode.value.trim();
  if (!inviteCode) {
    joinError.value = "Introduce un codigo valido.";
    return;
  }

  try {
    await api.post("/api/households/join", { inviteCode });
    const { data } = await api.get<MeResponse>("/api/auth/me");
    session.applyMeResponse(data);
    joinCode.value = "";
    joinSuccess.value = "Te has unido al hogar correctamente.";
  } catch {
    joinError.value = "No se pudo unir al hogar. Revisa el codigo.";
  }
};
</script>
