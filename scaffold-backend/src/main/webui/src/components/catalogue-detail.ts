import { LitElement, html, css, nothing } from "lit";
import { customElement, property, state } from "lit/decorators.js";
import { marked } from "marked";
import DOMPurify from "dompurify";
import { registerPanel } from "@casehubio/pages-runtime";
import {
  toGraphModel,
  toPlanRows,
  renderPlaybookYaml,
  type TopologyPlanData,
  type GraphModelData,
  type PlanRow,
  type PlaybookMetadata,
} from "./catalogue-conversions";

@customElement("catalogue-detail")
export class CatalogueDetail extends LitElement {
  static override styles = css`
    :host {
      display: block;
      border: 1px solid var(--pages-border-color, #30363d);
      border-radius: var(--pages-radius-md, 6px);
      padding: 16px;
      margin-top: 16px;
    }
    .detail-header {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 12px;
      color: var(--pages-text-color, #e6edf3);
    }
    .panels {
      display: grid;
      grid-template-columns: 5fr 7fr;
      gap: 16px;
    }
    .panel {
      border: 1px solid var(--pages-border-color, #30363d);
      border-radius: var(--pages-radius-sm, 4px);
      padding: 12px;
      overflow: auto;
    }
    .panel-title {
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 8px;
      color: var(--pages-text-muted, #8b949e);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .playbook-yaml {
      font-family: var(--pages-font-mono, "SFMono-Regular", monospace);
      font-size: 12px;
      white-space: pre-wrap;
      word-break: break-word;
      color: var(--pages-text-color, #e6edf3);
      background: var(--pages-bg-subtle, #161b22);
      padding: 12px;
      border-radius: 4px;
      max-height: 500px;
      overflow: auto;
    }
    .playbook-meta {
      display: flex;
      gap: 12px;
      margin-bottom: 8px;
      font-size: 12px;
      color: var(--pages-text-muted, #8b949e);
    }
    .playbook-meta span {
      background: var(--pages-bg-subtle, #161b22);
      padding: 2px 8px;
      border-radius: 4px;
    }
    .plan-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;
    }
    .plan-table th {
      text-align: left;
      padding: 6px 8px;
      border-bottom: 2px solid var(--pages-border-color, #30363d);
      color: var(--pages-text-muted, #8b949e);
      font-weight: 600;
    }
    .plan-table td {
      padding: 4px 8px;
      border-bottom: 1px solid var(--pages-border-color, #21262d);
      color: var(--pages-text-color, #e6edf3);
    }
    .plan-group-header td {
      padding: 8px 8px 4px;
      font-weight: 600;
      color: var(--pages-text-muted, #8b949e);
      font-size: 12px;
      border-bottom: 1px solid var(--pages-border-color, #30363d);
    }
    .plan-section-header td {
      padding: 12px 8px 4px;
      font-weight: 700;
      color: var(--pages-text-color, #e6edf3);
      font-size: 14px;
    }
    .empty-message {
      color: var(--pages-text-muted, #8b949e);
      padding: 16px;
      font-style: italic;
    }
    .readme-section {
      margin-top: 12px;
    }
    .readme-section summary {
      cursor: pointer;
      font-weight: 600;
      color: var(--pages-text-muted, #8b949e);
      font-size: 13px;
    }
    .readme-content {
      padding: 12px;
      font-size: 14px;
      color: var(--pages-text-color, #e6edf3);
      line-height: 1.5;
    }
    .graph-container {
      min-height: 300px;
    }
    .loading {
      color: var(--pages-text-muted, #8b949e);
      padding: 24px;
      text-align: center;
    }
  `;

  @property({ type: String }) endpoint = "/api/scaffold/catalogue";
  @property({ type: String, attribute: "topology-name" }) topologyName = "";

  @state() private planData: TopologyPlanData | null = null;
  @state() private graphModel: GraphModelData | null = null;
  @state() private planRows: PlanRow[] = [];
  @state() private playbookMeta: PlaybookMetadata | null = null;
  @state() private playbookYaml = "";
  @state() private readmeHtml = "";
  @state() private loading = false;
  @state() private error: string | null = null;

  configure(props: Record<string, unknown>) {
    if (typeof props.endpoint === "string") this.endpoint = props.endpoint;
    if (typeof props["topology-name"] === "string") this.topologyName = props["topology-name"];
  }

  override updated(changed: Map<string, unknown>) {
    if (changed.has("topologyName") && this.topologyName) {
      this.fetchPlan();
    }
  }

  private async fetchPlan() {
    this.loading = true;
    this.error = null;
    try {
      const resp = await fetch(`${this.endpoint}/${encodeURIComponent(this.topologyName)}/plan`);
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const data: TopologyPlanData = await resp.json();
      this.planData = data;
      this.graphModel = toGraphModel(data.graph);
      this.planRows = toPlanRows(data.plan);
      const pb = renderPlaybookYaml(data.playbookYaml);
      this.playbookMeta = pb.metadata;
      this.playbookYaml = pb.highlighted;
      this.readmeHtml = data.readme ? DOMPurify.sanitize(marked.parse(data.readme) as string) : "";
    } catch (e) {
      this.error = e instanceof Error ? e.message : "Failed to load plan";
    } finally {
      this.loading = false;
    }
  }

  override render() {
    if (!this.topologyName) return nothing;
    if (this.loading) return html`<div class="loading">Loading topology plan...</div>`;
    if (this.error) return html`<div class="empty-message">Error: ${this.error}</div>`;
    if (!this.planData) return nothing;

    return html`
      <div class="detail-header">Detail: ${this.topologyName}</div>
      <div class="panels">
        <div class="panel">
          <div class="panel-title">Playbook</div>
          ${this.renderPlaybook()}
        </div>
        <div>
          <div class="panel">
            <div class="panel-title">Dependencies</div>
            <div class="graph-container">
              ${this.renderGraph()}
            </div>
          </div>
          <div class="panel" style="margin-top: 12px;">
            <div class="panel-title">Deployment Plan</div>
            ${this.renderPlanTable()}
          </div>
        </div>
      </div>
      ${this.renderReadme()}
    `;
  }

  private renderPlaybook() {
    if (!this.playbookYaml) {
      return html`<div class="empty-message">No playbook available</div>`;
    }
    return html`
      ${this.playbookMeta ? html`
        <div class="playbook-meta">
          <span>v${this.playbookMeta.version}</span>
          <span>${this.playbookMeta.schema}</span>
          ${this.playbookMeta.name ? html`<span>${this.playbookMeta.name}</span>` : nothing}
        </div>
      ` : nothing}
      <pre class="playbook-yaml">${this.playbookYaml}</pre>
    `;
  }

  private renderGraph() {
    if (!this.graphModel || this.graphModel.nodes.length === 0) {
      return html`<div class="empty-message">No dependency graph available</div>`;
    }
    return html`
      <graph-canvas-core
        .model=${{ nodes: this.graphModel.nodes, edges: this.graphModel.edges }}
        .layoutOptions=${{
          algorithm: "layered",
          direction: "RIGHT",
          nodeSpacing: 50,
          layerSpacing: 80,
        }}
        style="width: 100%; height: 300px;">
      </graph-canvas-core>
    `;
  }

  private renderPlanTable() {
    if (this.planData?.plan.empty) {
      return html`<div class="empty-message">No transitions needed</div>`;
    }
    if (this.planRows.length === 0) {
      return html`<div class="empty-message">No plan data</div>`;
    }

    let lastGroup = "";
    let lastSection = "";
    const tableRows = this.planRows.map((row) => {
      const parts = [];
      if (row.section && row.section !== lastSection) {
        lastSection = row.section;
        lastGroup = "";
        parts.push(html`<tr class="plan-section-header"><td colspan="3">${row.section}</td></tr>`);
      }
      if (row.group !== lastGroup) {
        lastGroup = row.group;
        parts.push(html`<tr class="plan-group-header"><td colspan="3">${row.group}</td></tr>`);
      }
      parts.push(html`
        <tr>
          <td>${row.nodeId}</td>
          <td>${row.action}</td>
          <td>${row.type}</td>
        </tr>
      `);
      return parts;
    });

    return html`
      <table class="plan-table">
        <thead><tr><th>Node</th><th>Action</th><th>Type</th></tr></thead>
        <tbody>${tableRows}</tbody>
      </table>
    `;
  }

  private renderReadme() {
    if (!this.readmeHtml) return nothing;
    return html`
      <details class="readme-section">
        <summary>About</summary>
        <div class="readme-content" .innerHTML=${this.readmeHtml}></div>
      </details>
    `;
  }
}

registerPanel("catalogue-detail", "catalogue-detail");
