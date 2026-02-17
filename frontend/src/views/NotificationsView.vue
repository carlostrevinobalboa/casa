<template>
  <section class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold">Notificaciones</h2>
        <p class="mt-1 text-sm text-slate-600">Alertas asignadas al usuario actual.</p>
      </div>
      <p class="rounded-lg bg-slate-100 px-3 py-2 text-xs text-slate-700">No leidas: {{ unreadCount }}</p>
    </div>

    <ul v-if="notifications.length" class="space-y-2">
      <li
        v-for="notification in notifications"
        :key="notification.id"
        class="rounded-xl border border-slate-200 px-4 py-3"
      >
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="font-medium text-slate-900">{{ notification.title }}</p>
            <p class="text-sm text-slate-600">{{ notification.body }}</p>
            <p class="mt-1 text-xs text-slate-500">{{ notification.scheduledFor }}</p>
          </div>

          <button
            v-if="!notification.readAt"
            type="button"
            class="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-50"
            @click="markRead(notification.id)"
          >
            Marcar leida
          </button>

          <span v-else class="text-xs text-emerald-700">Leida</span>
        </div>
      </li>
    </ul>

    <p v-else class="text-sm text-slate-500">No hay notificaciones por ahora.</p>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import type { UserNotification } from "../types";

const session = useSessionStore();

const notifications = ref<UserNotification[]>([]);
const unreadCount = ref(0);

const load = async () => {
  const householdId = session.activeHouseholdId;
  if (!householdId) {
    notifications.value = [];
    unreadCount.value = 0;
    return;
  }

  const [notificationsResponse, unreadResponse] = await Promise.all([
    api.get<UserNotification[]>(`/api/households/${householdId}/notifications`),
    api.get<{ unreadCount: number }>(`/api/households/${householdId}/notifications/unread-count`)
  ]);

  notifications.value = notificationsResponse.data;
  unreadCount.value = unreadResponse.data.unreadCount;
};

const markRead = async (notificationId: string) => {
  const householdId = session.activeHouseholdId;
  if (!householdId) {
    return;
  }

  await api.post(`/api/households/${householdId}/notifications/${notificationId}/read`);
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
