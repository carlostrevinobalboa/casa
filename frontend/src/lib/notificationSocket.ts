import { apiBaseUrl } from "./api";
import type { NotificationRealtimeEvent } from "../types";

interface NotificationSocketOptions {
  token: string;
  onEvent: (event: NotificationRealtimeEvent) => void;
  onConnected?: () => void;
  onDisconnected?: () => void;
}

const SUBSCRIPTION_ID = "notifications-subscription";

export const connectNotificationSocket = (options: NotificationSocketOptions): (() => void) => {
  let buffer = "";
  let closedManually = false;
  const socket = new WebSocket(toWebSocketUrl(apiBaseUrl));

  socket.onopen = () => {
    sendFrame(socket, "CONNECT", {
      "accept-version": "1.2",
      Authorization: `Bearer ${options.token}`
    });
  };

  socket.onmessage = (event) => {
    const chunk = typeof event.data === "string" ? event.data : "";
    buffer += chunk;

    while (buffer.includes("\u0000")) {
      const frameEnd = buffer.indexOf("\u0000");
      const rawFrame = buffer.slice(0, frameEnd);
      buffer = buffer.slice(frameEnd + 1);

      if (!rawFrame.trim()) {
        continue;
      }

      handleFrame(rawFrame, socket, options);
    }
  };

  socket.onclose = () => {
    if (!closedManually) {
      options.onDisconnected?.();
    }
  };

  socket.onerror = () => {
    socket.close();
  };

  return () => {
    closedManually = true;
    socket.close();
  };
};

const handleFrame = (rawFrame: string, socket: WebSocket, options: NotificationSocketOptions) => {
  const separatorIndex = rawFrame.indexOf("\n\n");
  const headersSection = separatorIndex === -1 ? rawFrame : rawFrame.slice(0, separatorIndex);
  const body = separatorIndex === -1 ? "" : rawFrame.slice(separatorIndex + 2);

  const headerLines = headersSection.split("\n");
  const command = headerLines.shift()?.trim();
  const headers = parseHeaders(headerLines);

  if (command === "CONNECTED") {
    sendFrame(socket, "SUBSCRIBE", {
      id: SUBSCRIPTION_ID,
      destination: "/user/queue/notifications"
    });
    options.onConnected?.();
    return;
  }

  if (command !== "MESSAGE") {
    return;
  }

  try {
    const payload = JSON.parse(body) as NotificationRealtimeEvent;
    options.onEvent(payload);
  } catch {
    // Ignora payloads invalidos para mantener estable el stream.
  }

  if (headers["content-type"] === "application/json") {
    return;
  }
};

const parseHeaders = (lines: string[]): Record<string, string> => {
  const headers: Record<string, string> = {};
  for (const line of lines) {
    const separatorIndex = line.indexOf(":");
    if (separatorIndex <= 0) {
      continue;
    }
    const key = line.slice(0, separatorIndex).trim();
    const value = line.slice(separatorIndex + 1).trim();
    headers[key] = value;
  }
  return headers;
};

const sendFrame = (socket: WebSocket, command: string, headers: Record<string, string>, body = "") => {
  if (socket.readyState !== WebSocket.OPEN) {
    return;
  }

  const frame = [
    command,
    ...Object.entries(headers).map(([key, value]) => `${key}:${value}`),
    "",
    body
  ].join("\n");

  socket.send(`${frame}\u0000`);
};

const toWebSocketUrl = (baseUrl: string): string => {
  const parsed = new URL(baseUrl);
  const protocol = parsed.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${parsed.host}/ws`;
};
