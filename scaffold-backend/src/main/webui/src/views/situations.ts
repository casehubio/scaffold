import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const situationsView = rows(
  title("Situations", "h2"),
  dataTable({
    lookup: lookup("situations", groupBy(null,
      col("id"), col("deploymentId"), col("category"),
      col("status"), col("resolution"),
    )),
    sortable: true,
  }),
);
