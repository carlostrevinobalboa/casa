<template>
  <main class="flex min-h-screen items-center justify-center px-4">
    <section class="w-full max-w-md rounded-2xl bg-white p-6 text-center shadow-lg">
      <h1 class="text-xl font-semibold text-slate-900">Autenticando</h1>
      <p class="mt-2 text-sm text-slate-600">Estamos validando tu sesion de Google.</p>
      <p v-if="errorMessage" class="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
        {{ errorMessage }}
      </p>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "../lib/api";
import { useSessionStore } from "../stores/session";
import type { MeResponse } from "../types";

const router = useRouter();
const route = useRoute();
const session = useSessionStore();
const errorMessage = ref("");

onMounted(async () => {
  const token = String(route.query.token ?? "");

  if (!token) {
    errorMessage.value = "Token no recibido desde Google.";
    await router.replace({ name: "login", query: { oauthError: "missing_token" } });
    return;
  }

  try {
    session.setToken(token);
    const { data } = await api.get<MeResponse>("/api/auth/me");
    session.applyMeResponse(data);
    await router.replace({ name: "dashboard" });
  } catch {
    session.logout();
    await router.replace({ name: "login", query: { oauthError: "profile_error" } });
  }
});
</script>
