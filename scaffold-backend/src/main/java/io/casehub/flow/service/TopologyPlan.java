package io.casehub.flow.service;

public record TopologyPlan(GraphView graph, PlanView plan,
                            String playbookYaml, String readme) {}
