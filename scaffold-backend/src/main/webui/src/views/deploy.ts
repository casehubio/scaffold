import { rows, columns, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const deployView = rows(
  columns([6, 6],
    [rows(
      title("Deployments", "h3"),
      dataTable({
        lookup: lookup("deployments", groupBy(null,
          col("applicationId"), col("version"),
          col("status"), col("timestamp"),
        )),
        sortable: true,
      }),
    )],
    [rows(
      title("Approvals", "h3"),
      dataTable({
        lookup: lookup("approvals", groupBy(null,
          col("id"), col("planId"),
          col("status"), col("reviewer"),
        )),
        sortable: true,
      }),
    )],
  ),
);
