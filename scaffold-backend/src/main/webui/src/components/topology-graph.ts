import { LitElement, html, css, svg } from "lit";
import { customElement, property, state } from "lit/decorators.js";
import ELK from "elkjs/lib/elk.bundled.js";

interface TopologyNode {
  nodeId: string;
  type: string;
  status: string;
  spec: Record<string, unknown>;
}

interface LayoutNode {
  id: string;
  x: number;
  y: number;
  width: number;
  height: number;
  data: TopologyNode;
}

interface LayoutEdge {
  id: string;
  sections: Array<{ startPoint: { x: number; y: number }; endPoint: { x: number; y: number } }>;
}

@customElement("casehub-topology-graph")
export class TopologyGraph extends LitElement {
  static styles = css`
    :host { display: block; width: 100%; height: 600px; overflow: auto; }
    .node { cursor: pointer; }
    .node-rect { rx: 8; ry: 8; stroke-width: 2; }
    .node-label { font-size: 12px; fill: white; text-anchor: middle; dominant-baseline: central; }
    .edge { stroke: #656d76; stroke-width: 1.5; fill: none; }
    .selected .node-rect { stroke: #0969da; stroke-width: 3; }
  `;

  @property({ type: Array }) nodes: TopologyNode[] = [];
  @state() private layoutNodes: LayoutNode[] = [];
  @state() private layoutEdges: LayoutEdge[] = [];
  @state() private selectedNodeId: string | null = null;

  private elk = new ELK();

  updated(changed: Map<string, unknown>) {
    if (changed.has("nodes")) {
      this.computeLayout();
    }
  }

  private async computeLayout() {
    if (this.nodes.length === 0) {
      this.layoutNodes = [];
      this.layoutEdges = [];
      return;
    }

    const elkGraph = {
      id: "root",
      layoutOptions: {
        "elk.algorithm": "layered",
        "elk.direction": "RIGHT",
        "elk.spacing.nodeNode": "50",
        "elk.layered.spacing.nodeNodeBetweenLayers": "80",
      },
      children: this.nodes.map(n => ({
        id: n.nodeId,
        width: 160,
        height: 60,
      })),
      edges: [] as Array<{ id: string; sources: string[]; targets: string[] }>,
    };

    for (const node of this.nodes) {
      const deps = (node.spec?.dependencies as string[]) || [];
      for (const dep of deps) {
        elkGraph.edges.push({
          id: `${node.nodeId}->${dep}`,
          sources: [dep],
          targets: [node.nodeId],
        });
      }
    }

    try {
      const layout = await this.elk.layout(elkGraph);
      this.layoutNodes = (layout.children || []).map(c => ({
        id: c.id,
        x: c.x || 0,
        y: c.y || 0,
        width: c.width || 160,
        height: c.height || 60,
        data: this.nodes.find(n => n.nodeId === c.id)!,
      }));
      this.layoutEdges = (layout.edges || []).map(e => ({
        id: e.id,
        sections: (e as unknown as { sections: LayoutEdge["sections"] }).sections || [],
      }));
    } catch (err) {
      console.error("ELK layout failed:", err);
    }
  }

  private statusColor(status: string): string {
    switch (status) {
      case "CONVERGED": return "#2d8a4e";
      case "DRIFTED": return "#d4a017";
      case "FAULTED": return "#cf222e";
      default: return "#6e7781";
    }
  }

  private onNodeClick(nodeId: string) {
    this.selectedNodeId = this.selectedNodeId === nodeId ? null : nodeId;
    this.dispatchEvent(new CustomEvent("node-selected", {
      detail: { nodeId },
      bubbles: true,
      composed: true,
    }));
  }

  connectedCallback() {
    super.connectedCallback();
    this.addEventListener("reconciliation-event", ((e: CustomEvent) => {
      this.handleReconciliationEvent(e.detail);
    }) as EventListener);
  }

  private handleReconciliationEvent(event: { type: string; subject: string }) {
    const nodeId = event.subject;
    if (!nodeId) return;
    const node = this.nodes.find(n => n.nodeId === nodeId);
    if (node) {
      if (event.type.endsWith("NODE_CONVERGED")) node.status = "CONVERGED";
      else if (event.type.endsWith("NODE_DRIFTED")) node.status = "DRIFTED";
      else if (event.type.endsWith("NODE_FAULTED")) node.status = "FAULTED";
      this.requestUpdate();
    }
  }

  render() {
    if (this.layoutNodes.length === 0) {
      return html`<p style="color: #656d76; padding: 2rem;">No topology data available.</p>`;
    }

    const padding = 40;
    const maxX = Math.max(...this.layoutNodes.map(n => n.x + n.width), 400) + padding * 2;
    const maxY = Math.max(...this.layoutNodes.map(n => n.y + n.height), 300) + padding * 2;

    return html`
      <svg width="${maxX}" height="${maxY}" viewBox="0 0 ${maxX} ${maxY}">
        ${this.layoutEdges.map(edge =>
          edge.sections.map(s => svg`
            <line class="edge"
              x1="${s.startPoint.x + padding}" y1="${s.startPoint.y + padding}"
              x2="${s.endPoint.x + padding}" y2="${s.endPoint.y + padding}" />
          `)
        )}
        ${this.layoutNodes.map(node => svg`
          <g class="node ${this.selectedNodeId === node.id ? "selected" : ""}"
             transform="translate(${node.x + padding}, ${node.y + padding})"
             @click="${() => this.onNodeClick(node.id)}">
            <rect class="node-rect"
              width="${node.width}" height="${node.height}"
              fill="${this.statusColor(node.data.status)}"
              stroke="${this.selectedNodeId === node.id ? "#0969da" : "#30363d"}" />
            <text class="node-label"
              x="${node.width / 2}" y="${node.height / 3}">
              ${node.data.nodeId}
            </text>
            <text class="node-label" style="font-size: 10px; opacity: 0.8"
              x="${node.width / 2}" y="${node.height * 2 / 3}">
              ${node.data.type} · ${node.data.status}
            </text>
          </g>
        `)}
      </svg>
    `;
  }
}
