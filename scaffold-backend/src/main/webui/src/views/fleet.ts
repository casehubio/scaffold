import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const fleetView = rows(
  title("Fleet", "h2"),
  dataTable({
    lookup: lookup("topology", groupBy(null,
      col("nodeId", "Node"),
      col("type", "Type"),
      col("status", "Status"),
      col("loopId", "Reconciliation Loop"),
    )),
    sortable: true,
    selectionTopic: "selected-node",
  }),
);
