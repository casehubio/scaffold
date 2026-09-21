import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const fleetView = rows(
  title("Fleet", "h2"),
  dataTable({
    lookup: lookup("deployments", groupBy(null,
      col("id"), col("name"), col("archetype"),
      col("status"), col("health"),
    )),
    sortable: true,
    selectionTopic: "selected-deployment",
  }),
);
