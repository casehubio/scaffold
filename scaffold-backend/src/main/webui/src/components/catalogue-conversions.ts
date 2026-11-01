import jsyaml from "js-yaml";

export interface GraphNodeData {
  id: string;
  type: string;
  dependsOn: string[];
}

export interface GraphEdgeData {
  from: string;
  to: string;
}

export interface GraphViewData {
  nodes: GraphNodeData[];
  dependencies: GraphEdgeData[];
}

export interface PlanStepData {
  nodeId: string;
  action: string;
  type: string;
}

export interface PlanViewData {
  additions: PlanStepData[][];
  removals: PlanStepData[][];
  suspensions: PlanStepData[][];
  resumptions: PlanStepData[][];
  empty: boolean;
}

export interface TopologyPlanData {
  graph: GraphViewData;
  plan: PlanViewData;
  playbookYaml: string | null;
  readme: string | null;
}

export interface GraphModelNode {
  id: string;
  type: string;
  properties: Record<string, unknown>;
}

export interface GraphModelEdge {
  id: string;
  type: string;
  source: string;
  target: string;
}

export interface GraphModelData {
  nodes: GraphModelNode[];
  edges: GraphModelEdge[];
}

export interface PlanRow {
  group: string;
  section: string;
  nodeId: string;
  action: string;
  type: string;
}

export interface PlaybookMetadata {
  version: string;
  schema: string;
  name?: string;
}

export function toGraphModel(graph: GraphViewData): GraphModelData {
  const nodes: GraphModelNode[] = graph.nodes.map((n) => ({
    id: n.id,
    type: "topology-node",
    properties: { label: n.id, nodeType: n.type },
  }));

  const edges: GraphModelEdge[] = graph.dependencies.map((e) => ({
    id: `${e.from}->${e.to}`,
    type: "dependency",
    source: e.from,
    target: e.to,
  }));

  return { nodes, edges };
}

export function toPlanRows(plan: PlanViewData): PlanRow[] {
  if (plan.empty) return [];

  const categories: Array<{ label: string; layers: PlanStepData[][] }> = [];
  if (plan.additions.length > 0) categories.push({ label: "Additions", layers: plan.additions });
  if (plan.removals.length > 0) categories.push({ label: "Removals", layers: plan.removals });
  if (plan.suspensions.length > 0) categories.push({ label: "Suspensions", layers: plan.suspensions });
  if (plan.resumptions.length > 0) categories.push({ label: "Resumptions", layers: plan.resumptions });

  const singleCategory = categories.length === 1;
  const rows: PlanRow[] = [];

  for (const cat of categories) {
    for (let i = 0; i < cat.layers.length; i++) {
      for (const step of cat.layers[i]) {
        rows.push({
          group: `Layer ${i + 1}`,
          section: singleCategory ? "" : cat.label,
          nodeId: step.nodeId,
          action: step.action,
          type: step.type,
        });
      }
    }
  }

  return rows;
}

export function renderPlaybookYaml(yaml: string | null): {
  metadata: PlaybookMetadata | null;
  highlighted: string;
} {
  if (!yaml) return { metadata: null, highlighted: "" };

  let metadata: PlaybookMetadata | null = null;

  try {
    const docs = jsyaml.loadAll(yaml);
    if (docs.length > 0 && typeof docs[0] === "object" && docs[0] !== null) {
      const first = docs[0] as Record<string, unknown>;
      if ("playbook" in first && "schema" in first) {
        metadata = {
          version: String(first.playbook),
          schema: String(first.schema),
          ...(typeof first.name === "string" ? { name: first.name } : {}),
        };
      }
    }
  } catch {
    // Invalid YAML — fall through to raw display
  }

  return { metadata, highlighted: yaml };
}
