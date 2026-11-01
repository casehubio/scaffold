import { describe, it, expect } from "vitest";
import { toGraphModel, toPlanRows, renderPlaybookYaml } from "./catalogue-conversions";
import type { GraphViewData, PlanViewData } from "./catalogue-conversions";

describe("toGraphModel", () => {
  it("converts nodes with id, type, and properties", () => {
    const input: GraphViewData = {
      nodes: [
        { id: "web", type: "AppNode", dependsOn: ["dns"] },
        { id: "dns", type: "InfraNode", dependsOn: [] },
      ],
      dependencies: [{ from: "dns", to: "web" }],
    };
    const result = toGraphModel(input);

    expect(result.nodes).toHaveLength(2);
    expect(result.nodes[0]).toEqual({
      id: "web",
      type: "topology-node",
      properties: { label: "web", nodeType: "AppNode" },
    });
    expect(result.nodes[1]).toEqual({
      id: "dns",
      type: "topology-node",
      properties: { label: "dns", nodeType: "InfraNode" },
    });
  });

  it("synthesizes edge IDs and maps source/target", () => {
    const input: GraphViewData = {
      nodes: [
        { id: "web", type: "AppNode", dependsOn: ["dns"] },
        { id: "dns", type: "InfraNode", dependsOn: [] },
      ],
      dependencies: [{ from: "dns", to: "web" }],
    };
    const result = toGraphModel(input);

    expect(result.edges).toHaveLength(1);
    expect(result.edges[0]).toEqual({
      id: "dns->web",
      type: "dependency",
      source: "dns",
      target: "web",
    });
  });

  it("handles empty graph", () => {
    const input: GraphViewData = { nodes: [], dependencies: [] };
    const result = toGraphModel(input);
    expect(result.nodes).toHaveLength(0);
    expect(result.edges).toHaveLength(0);
  });

  it("handles multiple edges", () => {
    const input: GraphViewData = {
      nodes: [
        { id: "lb", type: "InfraNode", dependsOn: ["web", "api"] },
        { id: "web", type: "AppNode", dependsOn: [] },
        { id: "api", type: "AppNode", dependsOn: [] },
      ],
      dependencies: [
        { from: "web", to: "lb" },
        { from: "api", to: "lb" },
      ],
    };
    const result = toGraphModel(input);
    expect(result.edges).toHaveLength(2);
    expect(result.edges[0].id).toBe("web->lb");
    expect(result.edges[1].id).toBe("api->lb");
  });
});

describe("toPlanRows", () => {
  it("converts single-category additions to grouped rows", () => {
    const input: PlanViewData = {
      additions: [
        [{ nodeId: "dns", action: "ADD", type: "InfraNode" }],
        [
          { nodeId: "web", action: "ADD", type: "AppNode" },
          { nodeId: "db", action: "ADD", type: "Store" },
        ],
      ],
      removals: [],
      suspensions: [],
      resumptions: [],
      empty: false,
    };
    const result = toPlanRows(input);

    expect(result).toHaveLength(3);
    expect(result[0]).toEqual({
      group: "Layer 1",
      section: "",
      nodeId: "dns",
      action: "ADD",
      type: "InfraNode",
    });
    expect(result[1].group).toBe("Layer 2");
    expect(result[2].group).toBe("Layer 2");
  });

  it("includes section headers when multiple categories present", () => {
    const input: PlanViewData = {
      additions: [[{ nodeId: "web", action: "ADD", type: "AppNode" }]],
      removals: [[{ nodeId: "old", action: "REMOVE", type: "AppNode" }]],
      suspensions: [],
      resumptions: [],
      empty: false,
    };
    const result = toPlanRows(input);

    expect(result).toHaveLength(2);
    expect(result[0].section).toBe("Additions");
    expect(result[1].section).toBe("Removals");
  });

  it("returns empty array when plan is empty", () => {
    const input: PlanViewData = {
      additions: [],
      removals: [],
      suspensions: [],
      resumptions: [],
      empty: true,
    };
    const result = toPlanRows(input);
    expect(result).toHaveLength(0);
  });

  it("handles all four categories", () => {
    const input: PlanViewData = {
      additions: [[{ nodeId: "a", action: "ADD", type: "AppNode" }]],
      removals: [[{ nodeId: "b", action: "REMOVE", type: "AppNode" }]],
      suspensions: [[{ nodeId: "c", action: "SUSPEND", type: "InfraNode" }]],
      resumptions: [[{ nodeId: "d", action: "RESUME", type: "Store" }]],
      empty: false,
    };
    const result = toPlanRows(input);
    expect(result).toHaveLength(4);
    expect(result[0].section).toBe("Additions");
    expect(result[1].section).toBe("Removals");
    expect(result[2].section).toBe("Suspensions");
    expect(result[3].section).toBe("Resumptions");
  });
});

describe("renderPlaybookYaml", () => {
  it("returns highlighted YAML for valid input", () => {
    const yaml = "steps:\n  - name: Install\n    run: npm install";
    const result = renderPlaybookYaml(yaml);
    expect(result.highlighted).toContain("steps:");
    expect(result.metadata).toBeNull();
  });

  it("extracts playbook front matter", () => {
    const yaml = "playbook: '1.0'\nschema: client\nname: deploy-web";
    const result = renderPlaybookYaml(yaml);
    expect(result.metadata).toEqual({
      version: "1.0",
      schema: "client",
      name: "deploy-web",
    });
  });

  it("returns empty string for null input", () => {
    const result = renderPlaybookYaml(null);
    expect(result.highlighted).toBe("");
    expect(result.metadata).toBeNull();
  });

  it("returns raw string for invalid YAML", () => {
    const result = renderPlaybookYaml("{{invalid: yaml: [}");
    expect(result.highlighted).toContain("{{invalid");
    expect(result.metadata).toBeNull();
  });

  it("handles front matter without name field", () => {
    const yaml = "playbook: '2.0'\nschema: server";
    const result = renderPlaybookYaml(yaml);
    expect(result.metadata).toEqual({
      version: "2.0",
      schema: "server",
    });
    expect(result.metadata?.name).toBeUndefined();
  });
});
