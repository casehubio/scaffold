import { rows, columns, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const deployView = rows(
  title("Topology Catalogue", "h2"),
  dataTable({
    lookup: lookup("catalogue", groupBy(null,
      col("name", "Topology"),
      col("description", "Description"),
      col("methodologyStep", "Step"),
      col("prerequisites", "Prerequisites"),
    )),
    sortable: true,
  }),
  title("Deployment Records", "h2"),
  columns([6, 6],
    [rows(
      title("Deployments", "h3"),
      dataTable({
        lookup: lookup("deployments", groupBy(null,
          col("deploymentId", "ID"),
          col("status", "Status"),
          col("message", "Message"),
          col("caseStatus", "Case"),
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
