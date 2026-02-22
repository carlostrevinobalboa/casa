<template>
  <div ref="mapContainer" class="w-full rounded-lg border border-slate-200" :class="heightClass"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";

interface RoutePoint {
  latitude: number;
  longitude: number;
}

const props = withDefaults(defineProps<{
  points: RoutePoint[];
  heightClass?: string;
}>(), {
  heightClass: "h-64"
});

const mapContainer = ref<HTMLElement | null>(null);
let mapInstance: any = null;
let polylineLayer: any = null;
let startMarker: any = null;
let endMarker: any = null;

const ensureLeafletLoaded = async (): Promise<any> => {
  if (typeof window === "undefined") {
    throw new Error("Window no disponible");
  }

  if (window.L) {
    return window.L;
  }

  if (!document.querySelector("link[data-leaflet='css']")) {
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
    link.setAttribute("data-leaflet", "css");
    document.head.appendChild(link);
  }

  await new Promise<void>((resolve, reject) => {
    const existingScript = document.querySelector("script[data-leaflet='js']") as HTMLScriptElement | null;
    if (existingScript) {
      if (window.L) {
        resolve();
        return;
      }
      existingScript.addEventListener("load", () => resolve(), { once: true });
      existingScript.addEventListener("error", () => reject(new Error("No se pudo cargar Leaflet")), { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
    script.async = true;
    script.setAttribute("data-leaflet", "js");
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("No se pudo cargar Leaflet"));
    document.body.appendChild(script);
  });

  return window.L;
};

const renderRoute = () => {
  if (!mapInstance || !window.L) {
    return;
  }

  if (polylineLayer) {
    mapInstance.removeLayer(polylineLayer);
    polylineLayer = null;
  }
  if (startMarker) {
    mapInstance.removeLayer(startMarker);
    startMarker = null;
  }
  if (endMarker) {
    mapInstance.removeLayer(endMarker);
    endMarker = null;
  }

  if (props.points.length === 0) {
    mapInstance.setView([40.4168, -3.7038], 12);
    return;
  }

  const latLngs = props.points.map((point) => [point.latitude, point.longitude] as [number, number]);
  polylineLayer = window.L.polyline(latLngs, { color: "#0f172a", weight: 3 }).addTo(mapInstance);

  const first = latLngs[0];
  const last = latLngs[latLngs.length - 1];
  startMarker = window.L.circleMarker(first, { radius: 5, color: "#16a34a" }).addTo(mapInstance);
  endMarker = window.L.circleMarker(last, { radius: 5, color: "#dc2626" }).addTo(mapInstance);

  if (latLngs.length === 1) {
    mapInstance.setView(first, 15);
  } else {
    const bounds = polylineLayer.getBounds();
    mapInstance.fitBounds(bounds, { padding: [20, 20] });
  }
};

onMounted(async () => {
  if (!mapContainer.value) {
    return;
  }

  try {
    const L = await ensureLeafletLoaded();
    mapInstance = L.map(mapContainer.value, {
      zoomControl: true,
      attributionControl: true
    }).setView([40.4168, -3.7038], 12);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap contributors"
    }).addTo(mapInstance);

    renderRoute();
  } catch {
    // Si falla el CDN, no rompemos la vista.
  }
});

watch(
  () => props.points,
  () => {
    renderRoute();
  },
  { deep: true }
);

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});

declare global {
  interface Window {
    L?: any;
  }
}
</script>
