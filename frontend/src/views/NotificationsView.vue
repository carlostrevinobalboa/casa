<template>
  <section class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold">Notificaciones</h2>
        <p class="mt-1 text-sm text-slate-600">Alertas asignadas al usuario actual.</p>
      </div>
      <div class="flex items-center gap-2">
        <p class="rounded-lg bg-slate-100 px-3 py-2 text-xs text-slate-700">No leidas: {{ unreadCount }}</p>
        <p
          class="rounded-lg px-3 py-2 text-xs"
          :class="socketConnected ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'"
        >
          {{ socketConnected ? "Tiempo real activo" : "Tiempo real reconectando" }}
        </p>
      </div>
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
            <p class="mt-1 text-xs text-slate-500">{{ formatDateTime(notification.scheduledFor) }}</p>
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
import { onBeforeUnmount, ref, watch } from "vue";
import { api } from "../lib/api";
import { connectNotificationSocket } from "../lib/notificationSocket";
import { useSessionStore } from "../stores/session";
import type { NotificationRealtimeEvent, UserNotification } from "../types";

const session = useSessionStore();

const notifications = ref<UserNotification[]>([]);
const unreadCount = ref(0);
const socketConnected = ref(false);

let disconnectSocket: (() => void) | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
let unmounted = false;

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

const handleRealtimeEvent = (event: NotificationRealtimeEvent) => {
  if (event.householdId !== session.activeHouseholdId) {
    return;
  }

  unreadCount.value = event.unreadCount;

  const incoming = event.notification;
  const existingIndex = notifications.value.findIndex((notification) => notification.id === incoming.id);
  if (existingIndex >= 0) {
    notifications.value[existingIndex] = incoming;
    return;
  }

  notifications.value = [incoming, ...notifications.value].slice(0, 30);
};

const clearReconnectTimer = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
};

const closeSocket = () => {
  clearReconnectTimer();
  if (disconnectSocket) {
    disconnectSocket();
    disconnectSocket = null;
  }
  socketConnected.value = false;
};

const scheduleReconnect = () => {
  clearReconnectTimer();
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    void connectSocket();
  }, 2500);
};

const connectSocket = async () => {
  closeSocket();

  const token = session.token;
  if (!token || !session.activeHouseholdId || unmounted) {
    return;
  }

  disconnectSocket = connectNotificationSocket({
    token,
    onEvent: handleRealtimeEvent,
    onConnected: () => {
      socketConnected.value = true;
    },
    onDisconnected: () => {
      socketConnected.value = false;
      if (!unmounted) {
        scheduleReconnect();
      }
    }
  });
};

const formatDateTime = (value: string) => {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

watch(
  () => [session.activeHouseholdId, session.token] as const,
  () => {
    void load();
    void connectSocket();
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  unmounted = true;
  closeSocket();
});
</script>
