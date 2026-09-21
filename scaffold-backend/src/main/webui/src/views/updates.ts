import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const updatesView = rows(
  title("Updates", "h2"),
  dataTable({
    lookup: lookup("updates", groupBy(null,
      col("deploymentId"), col("currentVersion"),
      col("availableVersion"), col("status"),
    )),
    sortable: true,
  }),
);
