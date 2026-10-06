import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const fleetView = rows(
  title("Fleet", "h2"),
  dataTable({
    lookup: lookup("applications", groupBy(null,
      col("id"), col("name"), col("status"),
      col("version"), col("lastDeployed"),
    )),
    sortable: true,
    selectionTopic: "selected-deployment",
  }),
);
