<template>
  <RouterView v-if="!session.isAuthenticated" />

  <div v-else class="min-h-screen">
    <header class="bg-brand-500 text-white shadow-md">
      <div class="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-4">
        <div>
          <h1 class="text-xl font-semibold">Casa</h1>
          <p class="text-sm opacity-90">Gestion del hogar compartido</p>
        </div>

        <div class="flex items-center gap-3">
          <div class="flex min-w-52 flex-col gap-1">
            <label for="active-household" class="text-[11px] uppercase tracking-wide text-white/80">
              Hogar activo
            </label>
            <select
              id="active-household"
              v-model="selectedHouseholdId"
              class="rounded-lg border border-white/30 bg-white/10 px-3 py-2 text-sm text-white outline-none"
              @change="onHouseholdChange"
            >
              <option
                v-for="household in session.households"
                :key="household.id"
                :value="household.id"
                class="text-slate-900"
              >
                {{ household.name }} ({{ household.inviteCode }})
              </option>
            </select>
          </div>

          <p class="text-sm">
            {{ session.user?.displayName }}
          </p>

          <button
            type="button"
            class="rounded-lg border border-white/30 px-3 py-2 text-sm hover:bg-white/10"
            @click="logout"
          >
            Salir
          </button>
        </div>
      </div>
    </header>

    <main class="mx-auto grid max-w-7xl gap-4 px-4 py-4 md:grid-cols-[240px_1fr]">
      <nav class="rounded-2xl bg-white p-4 shadow-sm">
        <ul class="space-y-2 text-sm">
          <li v-for="item in items" :key="item.to">
            <RouterLink
              :to="item.to"
              class="block rounded-lg px-3 py-2 text-slate-700 transition hover:bg-slate-100"
              active-class="bg-slate-900 text-white"
            >
              {{ item.label }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <section class="rounded-2xl bg-white p-4 shadow-sm">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useSessionStore } from "./stores/session";

const router = useRouter();
const session = useSessionStore();

const selectedHouseholdId = computed({
  get: () => session.activeHouseholdId,
  set: (value) => {
    if (value) {
      session.setActiveHousehold(value);
    }
  }
});

const items = [
  { to: "/", label: "Dashboard" },
  { to: "/despensa", label: "Despensa" },
  { to: "/recetas", label: "Recetas" },
  { to: "/compra", label: "Compra" },
  { to: "/admin/catalogo", label: "Admin" },
  { to: "/mascotas", label: "Mascotas" },
  { to: "/actividad", label: "Actividad" },
  { to: "/calendario", label: "Calendario" },
  { to: "/notificaciones", label: "Notificaciones" }
];

const onHouseholdChange = () => {
  if (!session.activeHouseholdId && session.households[0]) {
    session.setActiveHousehold(session.households[0].id);
  }
};

const logout = async () => {
  session.logout();
  await router.replace({ name: "login" });
};
</script>
