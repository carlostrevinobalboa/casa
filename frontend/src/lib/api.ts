import axios from "axios";

const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
const storageKey = "casa.session.v1";

export const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json"
  }
});

api.interceptors.request.use((config) => {
  const rawSession = localStorage.getItem(storageKey);
  if (!rawSession) {
    return config;
  }

  try {
    const session = JSON.parse(rawSession) as { token?: string | null };
    if (session.token) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${session.token}`;
    }
  } catch {
    localStorage.removeItem(storageKey);
  }

  return config;
});

export const apiBaseUrl = baseURL;
