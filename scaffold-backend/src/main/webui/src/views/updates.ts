import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const updatesView = rows(
  title("Updates", "h2"),
  dataTable({
    lookup: lookup("deployments", groupBy(null,
      col("deploymentId", "ID"),
      col("status", "Status"),
      col("message", "Message"),
    )),
    sortable: true,
  }),
);
