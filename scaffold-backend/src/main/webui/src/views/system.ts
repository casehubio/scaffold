import { tabs, hostPanel, rows, dataTable, lookup, col, groupBy } from "@casehubio/pages-ui";
import { registerPanel } from "@casehubio/pages-runtime";
import "@casehubio/mosaic-preferences-editor";
import "@casehubio/mosaic-kpi-metric-row";

registerPanel("preferences-editor", "blocks-preferences-editor");

export const systemView = tabs(
  ["Definitions", rows(
    dataTable({
      lookup: lookup("case-definitions", groupBy(null,
        col("namespace"), col("name"), col("version"),
      )),
      sortable: true,
      filter: { enabled: true },
    }),
  )],
  ["Preferences", hostPanel("preferences-editor", {
    endpoint: "/api/preferences",
  })],
  ["Health", hostPanel("kpi-metric-row", {
    endpoint: "/q/health",
  })],
);
