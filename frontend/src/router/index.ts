import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { useSessionStore } from "../stores/session";

const routes: RouteRecordRaw[] = [
  { path: "/login", name: "login", component: () => import("../views/LoginView.vue") },
  { path: "/auth/callback", name: "auth-callback", component: () => import("../views/AuthCallbackView.vue") },
  { path: "/", name: "dashboard", component: () => import("../views/DashboardView.vue"), meta: { requiresAuth: true } },
  { path: "/despensa", name: "despensa", component: () => import("../views/PantryView.vue"), meta: { requiresAuth: true } },
  { path: "/recetas", name: "recetas", component: () => import("../views/RecipesView.vue"), meta: { requiresAuth: true } },
  { path: "/compra", name: "compra", component: () => import("../views/ShoppingView.vue"), meta: { requiresAuth: true } },
  { path: "/mascotas", name: "mascotas", component: () => import("../views/PetsView.vue"), meta: { requiresAuth: true } },
  { path: "/actividad", name: "actividad", component: () => import("../views/ActivitiesView.vue"), meta: { requiresAuth: true } },
  { path: "/calendario", name: "calendario", component: () => import("../views/CalendarView.vue"), meta: { requiresAuth: true } },
  { path: "/notificaciones", name: "notificaciones", component: () => import("../views/NotificationsView.vue"), meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const session = useSessionStore();
  session.bootstrap();

  if (to.meta.requiresAuth && !session.isAuthenticated) {
    return { name: "login" };
  }

  if ((to.name === "login" || to.name === "auth-callback") && session.isAuthenticated) {
    return { name: "dashboard" };
  }

  return true;
});

export default router;
