import { defineStore } from "pinia";
import type { AuthResponse, HouseholdSummary, MeResponse, UserSummary } from "../types";

const STORAGE_KEY = "casa.session.v1";

interface SessionState {
  token: string | null;
  user: UserSummary | null;
  households: HouseholdSummary[];
  activeHouseholdId: string | null;
  activityUserFilter: string | "all";
  initialized: boolean;
}

interface PersistedSession {
  token: string | null;
  user: UserSummary | null;
  households: HouseholdSummary[];
  activeHouseholdId: string | null;
  activityUserFilter: string | "all";
}

export const useSessionStore = defineStore("session", {
  state: (): SessionState => ({
    token: null,
    user: null,
    households: [],
    activeHouseholdId: null,
    activityUserFilter: "all",
    initialized: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user),
    activeHousehold: (state) => state.households.find((h) => h.id === state.activeHouseholdId) ?? null
  },
  actions: {
    bootstrap() {
      if (this.initialized) {
        return;
      }

      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        this.initialized = true;
        return;
      }

      try {
        const parsed = JSON.parse(raw) as PersistedSession;
        this.token = parsed.token ?? null;
        this.user = parsed.user ?? null;
        this.households = parsed.households ?? [];
        this.activeHouseholdId = parsed.activeHouseholdId ?? this.households[0]?.id ?? null;
        this.activityUserFilter = parsed.activityUserFilter ?? "all";
      } catch {
        localStorage.removeItem(STORAGE_KEY);
      }

      this.initialized = true;
    },
    applyAuthResponse(payload: AuthResponse) {
      this.token = payload.accessToken;
      this.user = payload.user;
      this.households = payload.households;
      this.activeHouseholdId = payload.activeHouseholdId ?? payload.households[0]?.id ?? null;
      this.persist();
    },
    applyMeResponse(payload: MeResponse) {
      this.user = payload.user;
      this.households = payload.households;
      this.activeHouseholdId = payload.activeHouseholdId ?? payload.households[0]?.id ?? null;
      this.persist();
    },
    setToken(token: string) {
      this.token = token;
      this.persist();
    },
    setActiveHousehold(householdId: string) {
      this.activeHouseholdId = householdId;
      this.persist();
    },
    setActivityUserFilter(value: string | "all") {
      this.activityUserFilter = value;
      this.persist();
    },
    logout() {
      this.token = null;
      this.user = null;
      this.households = [];
      this.activeHouseholdId = null;
      this.activityUserFilter = "all";
      localStorage.removeItem(STORAGE_KEY);
    },
    persist() {
      const payload: PersistedSession = {
        token: this.token,
        user: this.user,
        households: this.households,
        activeHouseholdId: this.activeHouseholdId,
        activityUserFilter: this.activityUserFilter
      };

      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    }
  }
});
