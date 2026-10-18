import { emitPagesEvent } from "@casehubio/pages-data";

let eventSource: EventSource | null = null;

export function connectToReconciliationEvents(container: HTMLElement): void {
  if (eventSource) return;
  eventSource = new EventSource("/api/ops/reconciliation/events");

  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      emitPagesEvent(container, "reconciliation-event", data);
    } catch (e) {
      console.warn("Failed to parse reconciliation event:", e);
    }
  };

  eventSource.onerror = () => {
    console.warn("Reconciliation SSE connection error, will retry");
  };
}

export function disconnectReconciliationEvents(): void {
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
}
