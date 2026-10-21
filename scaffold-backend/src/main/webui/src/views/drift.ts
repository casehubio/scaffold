import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const driftView = rows(
  title("Drift", "h2"),
  dataTable({
    lookup: lookup("reconciliation", groupBy(null,
      col("clusterId", "Cluster"),
      col("clusterName", "Name"),
      col("active", "Active"),
      col("nodeCount", "Nodes"),
    )),
    sortable: true,
  }),
);
