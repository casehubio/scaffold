import { rows, columns, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const deployView = rows(
  title("Deploy", "h2"),
  columns([6, 6],
    [rows(
      title("Deployments", "h3"),
      dataTable({
        lookup: lookup("deployments", groupBy(null,
          col("deploymentId", "ID"),
          col("status", "Status"),
          col("message", "Message"),
        )),
        sortable: true,
      }),
    )],
    [rows(
      title("Approvals", "h3"),
      dataTable({
        lookup: lookup("approvals", groupBy(null,
          col("id", "ID"),
          col("planId", "Plan"),
          col("status", "Status"),
          col("reviewer", "Reviewer"),
        )),
        sortable: true,
      }),
    )],
  ),
);
