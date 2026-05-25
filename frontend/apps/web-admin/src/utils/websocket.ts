type MessageHandler = (data: any) => void;

let ws: WebSocket | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_DELAY = 30_000;
const handlers: Set<MessageHandler> = new Set();
let currentToken: string | null = null;

function getWsUrl(token: string): string {
  const { protocol, host } = window.location;
  const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:';
  return `${wsProtocol}//${host}/ws?token=${encodeURIComponent(token)}`;
}

function handleMessage(event: MessageEvent) {
  try {
    const data = JSON.parse(event.data);
    handlers.forEach((handler) => handler(data));
  } catch {
    // 非 JSON 消息忽略
  }
}

function scheduleReconnect() {
  if (reconnectTimer || !currentToken) return;
  const delay = Math.min(1000 * 2 ** reconnectAttempts, MAX_RECONNECT_DELAY);
  reconnectAttempts++;
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    if (currentToken) {
      doConnect(currentToken);
    }
  }, delay);
}

function doConnect(token: string) {
  if (ws) {
    ws.onclose = null;
    ws.close();
  }
  ws = new WebSocket(getWsUrl(token));
  ws.onopen = () => {
    reconnectAttempts = 0;
  };
  ws.onmessage = handleMessage;
  ws.onclose = () => {
    ws = null;
    scheduleReconnect();
  };
  ws.onerror = () => {
    ws?.close();
  };
}

export function connectWebSocket(token: string) {
  currentToken = token;
  reconnectAttempts = 0;
  doConnect(token);
}

export function disconnectWebSocket() {
  currentToken = null;
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  if (ws) {
    ws.onclose = null;
    ws.close();
    ws = null;
  }
}

export function onWebSocketMessage(handler: MessageHandler) {
  handlers.add(handler);
  return () => handlers.delete(handler);
}
