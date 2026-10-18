import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const driftView = rows(
  title("Drift", "h2"),
  dataTable({
    lookup: lookup("reconciliation", groupBy(null,
      col("loopId", "Loop"),
      col("tenancyId", "Tenancy"),
      col("state", "State"),
      col("nodeCount", "Nodes"),
    )),
    sortable: true,
  }),
);
