<template>
  <main class="mx-auto flex min-h-screen w-full max-w-4xl items-center px-4 py-10">
    <section class="grid w-full gap-6 rounded-3xl bg-white p-6 shadow-lg md:grid-cols-2 md:p-10">
      <div class="space-y-4">
        <h1 class="text-3xl font-semibold text-slate-900">Casa</h1>
        <p class="text-sm text-slate-600">
          Gestion del hogar compartido con despensa inteligente y alertas por usuario.
        </p>
        <ul class="space-y-2 text-sm text-slate-600">
          <li>Registro de productos y avisos de minimo/caducidad.</li>
          <li>Hogar comun con multiples usuarios.</li>
          <li>Notificaciones dirigidas al usuario correspondiente.</li>
        </ul>
      </div>

      <div class="space-y-4">
        <div class="flex rounded-xl bg-slate-100 p-1 text-sm">
          <button
            type="button"
            class="w-1/2 rounded-lg px-3 py-2"
            :class="isRegister ? 'text-slate-600' : 'bg-white text-slate-900 shadow-sm'"
            @click="isRegister = false"
          >
            Iniciar sesion
          </button>
          <button
            type="button"
            class="w-1/2 rounded-lg px-3 py-2"
            :class="isRegister ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600'"
            @click="isRegister = true"
          >
            Registrarse
          </button>
        </div>

        <form class="space-y-3" @submit.prevent="submit">
          <div v-if="isRegister">
            <label
              for="login-display-name"
              class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600"
            >
              Nombre
            </label>
            <input
              id="login-display-name"
              v-model="displayName"
              type="text"
              required
              class="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="Tu nombre"
            />
          </div>

          <div v-if="isRegister">
            <label
              for="login-household-name"
              class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600"
            >
              Nombre del hogar
            </label>
            <input
              id="login-household-name"
              v-model="householdName"
              type="text"
              required
              class="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="Casa principal"
            />
          </div>

          <div>
            <label for="login-email" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Email
            </label>
            <input
              id="login-email"
              v-model="email"
              type="email"
              required
              class="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="tu@email.com"
            />
          </div>

          <div>
            <label for="login-password" class="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-600">
              Password
            </label>
            <input
              id="login-password"
              v-model="password"
              type="password"
              required
              minlength="8"
              class="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500"
              placeholder="Minimo 8 caracteres"
            />
          </div>

          <p v-if="errorMessage" class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {{ errorMessage }}
          </p>

          <button
            type="submit"
            class="w-full rounded-xl bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-700"
            :disabled="submitting"
          >
            {{ submitting ? 'Procesando...' : isRegister ? 'Crear cuenta' : 'Entrar' }}
          </button>
        </form>

        <div class="relative py-1 text-center text-xs text-slate-500">
          <span class="bg-white px-2">o</span>
          <div class="absolute left-0 right-0 top-1/2 -z-10 h-px bg-slate-200"></div>
        </div>

        <a
          :href="googleLoginUrl"
          class="block rounded-xl border border-slate-300 px-3 py-2 text-center text-sm font-medium text-slate-700 hover:bg-slate-50"
        >
          Entrar con Google
        </a>

        <p class="text-xs text-slate-500">
          Para Google, configura `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` en backend.
        </p>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { AxiosError } from "axios";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, apiBaseUrl } from "../lib/api";
import { useSessionStore } from "../stores/session";
import type { AuthResponse } from "../types";

const router = useRouter();
const route = useRoute();
const session = useSessionStore();

const isRegister = ref(false);
const submitting = ref(false);

const email = ref("");
const password = ref("");
const displayName = ref("");
const householdName = ref("");

const routeOauthError = computed(() => String(route.query.oauthError ?? ""));
const errorMessage = ref(routeOauthError.value ? "Error en autenticacion con Google" : "");

const googleLoginUrl = `${apiBaseUrl}/oauth2/authorization/google`;

const submit = async () => {
  errorMessage.value = "";
  submitting.value = true;

  try {
    const endpoint = isRegister.value ? "/api/auth/register" : "/api/auth/login";

    const payload = isRegister.value
      ? {
          email: email.value,
          password: password.value,
          displayName: displayName.value,
          householdName: householdName.value
        }
      : {
          email: email.value,
          password: password.value
        };

    const { data } = await api.post<AuthResponse>(endpoint, payload);
    session.applyAuthResponse(data);
    await router.replace({ name: "dashboard" });
  } catch (error) {
    const axiosError = error as AxiosError<{ message?: string }>;
    errorMessage.value = axiosError.response?.data?.message ?? "No se pudo completar la operacion";
  } finally {
    submitting.value = false;
  }
};
</script>
