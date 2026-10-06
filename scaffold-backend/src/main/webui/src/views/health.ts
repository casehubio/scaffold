import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const healthView = rows(
  title("Health", "h2"),
  dataTable({
    lookup: lookup("clusters", groupBy(null,
      col("id"), col("name"), col("status"),
      col("nodeCount"),
    )),
    sortable: true,
  }),
);
