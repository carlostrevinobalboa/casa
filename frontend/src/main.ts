import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import "./assets/main.css";
import { useSessionStore } from "./stores/session";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);

const session = useSessionStore();
session.bootstrap();

app.use(router);
app.mount("#app");
