import { rows, dataTable, title } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";

export const situationsView = rows(
  title("Situations", "h2"),
  dataTable({
    lookup: lookup("cases", groupBy(null,
      col("caseId", "Case"),
      col("type", "Type"),
      col("status", "Status"),
      col("eventCount", "Events"),
    )),
    sortable: true,
  }),
);
