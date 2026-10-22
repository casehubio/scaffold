import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const fleetView = rows(
  title("Fleet", "h2"),
  dataTable({
    lookup: lookup("topology", groupBy(null,
      col("nodeId", "Node"),
      col("nodeType", "Type"),
      col("actualStatus", "Status"),
      col("loopKey", "Reconciliation Loop"),
      col("caseStatus", "Case"),
    )),
    sortable: true,
    selectionTopic: "selected-node",
  }),
);
