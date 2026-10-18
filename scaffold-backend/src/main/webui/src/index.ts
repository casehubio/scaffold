import { loadSite } from "@casehubio/pages-runtime";
import { page, tabs } from "@casehubio/pages-ui";
import "@casehubio/pages-ui-tokens";
import { createDatasets, createAppDatasets } from "./datasets";
import { casesView } from "./views/cases";
import { workItemsView } from "./views/work-items";
import { queuesView } from "./views/queues";
import { orchestrationView } from "./views/orchestration";
import { trustAuditView } from "./views/trust-audit";
import { operationsView } from "./views/operations";
import { iotView } from "./views/iot";
import { notificationsView } from "./views/notifications";
import { systemView } from "./views/system";
import { fleetView } from "./views/fleet";
import { deployView } from "./views/deploy";
import { healthView } from "./views/health";
import { driftView } from "./views/drift";
import { situationsView } from "./views/situations";
import { updatesView } from "./views/updates";
import { setupNavigation, configureAppNavigation } from "./navigation";

type TabEntry = [string, ...unknown[]];

const VIEW_REGISTRY: Record<string, unknown> = {
  "fleet-dashboard": fleetView,
  "deployment-wizard": deployView,
  "health-monitor": healthView,
  "drift-detection": driftView,
  "situation-manager": situationsView,
  "update-manager": updatesView,
};

interface AppTab {
  id: string;
  label: string;
  view: string;
}

interface AppDatasetDef {
  name: string;
  endpoint: string;
  source: string;
}

interface AppResponse {
  name: string;
  tabs: AppTab[];
  datasets: AppDatasetDef[];
}

function buildDefaultTabs(modules: string[]): TabEntry[] {
  return [
    ["Cases", casesView],
    ["Work Items", workItemsView],
    ["Queues", queuesView],
    ["Orchestration", orchestrationView],
    ["Trust & Audit", trustAuditView],
    ...(modules.includes("ops") ? [["Operations", operationsView] as TabEntry] : []),
    ...(modules.includes("iot") ? [["IoT", iotView] as TabEntry] : []),
    ["Notifications", notificationsView],
    ["System", systemView],
  ];
}

async function start() {
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;

  const appResp = await fetch("/api/app");
  let allTabs: TabEntry[];
  let datasets;
  let defaultTab: string;

  if (appResp.ok) {
    const app: AppResponse = await appResp.json();
    allTabs = app.tabs
      .map((t) => {
        const view = VIEW_REGISTRY[t.view];
        if (!view) console.warn(`App tab "${t.label}" references unknown view "${t.view}"`);
        return [t.label, view] as TabEntry;
      })
      .filter(([, v]) => v != null);
    datasets = createAppDatasets(app.datasets);
    configureAppNavigation(app.tabs);
    defaultTab = allTabs.length > 0 ? (allTabs[0][0] as string) : "Fleet";
  } else {
    const { modules } = await fetch("/api/modules").then(r => r.json());
    allTabs = buildDefaultTabs(modules);
    datasets = createDatasets(modules);
    defaultTab = "Cases";
  }

  const app = page("CaseHub Console",
    tabs(...allTabs),
    { settings: { mode: prefersDark ? "dark" : "light" }, datasets },
  );

  const container = document.getElementById("app");
  if (container) {
    const site = await loadSite(container, app);
    site.setTheme(prefersDark ? "dark" : "light");
    setupNavigation(site, container);

    if (!location.hash) {
      site.navigate(defaultTab);
    }

    const picker = document.createElement("pages-theme-picker");
    picker.setAttribute("compact", "");
    picker.target = container;
    container.prepend(picker);

    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", (e) => {
      site.setTheme(e.matches ? "dark" : "light");
    });
  }
}

start().catch(console.error);
