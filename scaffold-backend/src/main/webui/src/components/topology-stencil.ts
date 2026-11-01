import { html } from "lit-html";
import { registerStencil, registerEdgeType } from "@casehubio/graph-renderer";
import type { GraphNode } from "@casehubio/graph-core";

const NODE_TYPE_COLORS: Record<string, string> = {
  InfraNode: "#636e7b",
  AppNode: "#2d8a4e",
  Store: "#d4a017",
};
const DEFAULT_COLOR = "#6e7781";

function renderTopologyNode(node: GraphNode) {
  const label = String(node.properties.label ?? node.id);
  const nodeType = String(node.properties.nodeType ?? "");
  const color = NODE_TYPE_COLORS[nodeType] ?? DEFAULT_COLOR;

  return html`
    <div style="
      width: 100%;
      height: 100%;
      background: ${color};
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: white;
      font-family: system-ui, sans-serif;
      cursor: pointer;
    ">
      <div style="font-size: 13px; font-weight: 600;">${label}</div>
      <div style="font-size: 10px; opacity: 0.8;">${nodeType}</div>
    </div>
  `;
}

const topologyNodeGrammar = {
  type: "topology-node",
  connections: {
    inbound: { min: 0, max: Infinity, allowedFrom: ["topology-node"] },
    outbound: { min: 0, max: Infinity, allowedTo: ["topology-node"] },
  },
};

let registered = false;

export function registerTopologyStencils() {
  if (registered) return;
  registered = true;

  registerStencil({
    type: "topology-node",
    label: "Topology Node",
    icon: "server",
    grammar: topologyNodeGrammar,
    render: renderTopologyNode,
  });

  registerEdgeType({
    type: "dependency",
    label: "Dependency",
    markerEnd: { type: "arrowclosed", color: "#656d76" },
    defaultStyle: "stroke: #656d76; stroke-width: 1.5;",
  });
}
