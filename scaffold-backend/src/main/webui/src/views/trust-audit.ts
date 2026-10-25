import { columns, rows, dataTable, hostPanel } from "@casehubio/pages-ui";
import { lookup, col, groupBy } from "@casehubio/pages-ui";
import { registerPanel } from "@casehubio/pages-runtime";
import "@casehubio/mosaic-trust-workbench";
import "@casehubio/mosaic-audit-trail-viewer";
import "@casehubio/mosaic-compliance-summary";
import "@casehubio/mosaic-routing-rationale";

registerPanel("trust-workbench", "blocks-trust-workbench");
registerPanel("audit-trail-viewer", "blocks-audit-trail-viewer");
registerPanel("compliance-summary", "blocks-compliance-summary");
registerPanel("routing-rationale", "blocks-routing-rationale");

export const trustAuditView = columns([4, 8],
  [dataTable({
    lookup: lookup("actors", groupBy(null,
      col("actorId"), col("actorType"), col("trustScore"),
    )),
    sortable: true,
    selectionTopic: "selected-actor",
  })],
  [rows(
    hostPanel("trust-workbench", {
      endpoint: "/workitems/actors",
      "actor-id": "#{row.actorId}",
    }),
    hostPanel("audit-trail-viewer", {
      endpoint: "/audit",
      "subject-id": "#{row.actorId}",
    }),
    hostPanel("compliance-summary", {
      endpoint: "/audit/compliance",
    }),
    hostPanel("routing-rationale", {
      endpoint: "/workitems/actors",
      "actor-id": "#{row.actorId}",
    }),
  )],
);
