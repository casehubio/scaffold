import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const driftView = rows(
  title("Drift", "h2"),
  dataTable({
    lookup: lookup("drift", groupBy(null,
      col("deploymentId"), col("node"), col("expected"),
      col("actual"), col("severity"),
    )),
    sortable: true,
  }),
);
