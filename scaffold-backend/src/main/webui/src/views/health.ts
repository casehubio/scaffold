import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const healthView = rows(
  title("Health", "h2"),
  dataTable({
    lookup: lookup("health", groupBy(null,
      col("deploymentId"), col("name"), col("status"),
      col("uptime"), col("lastChecked"),
    )),
    sortable: true,
  }),
);
