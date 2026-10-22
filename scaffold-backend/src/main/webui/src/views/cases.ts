import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const casesView = rows(
  title("Cases", "h2"),
  dataTable({
    lookup: lookup("scaffold-cases", groupBy(null,
      col("caseType", "Case Type"),
      col("status", "Status"),
      col("applicationId", "Application"),
      col("createdAt", "Created"),
      col("escalationState", "Escalation"),
      col("subCaseCount", "Sub-cases"),
    )),
    sortable: true,
    filter: { enabled: true },
    selectionTopic: "case",
  }),
);
