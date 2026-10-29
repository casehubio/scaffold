package io.casehub.flow.service;

import java.util.List;

public record GraphView(List<GraphNode> nodes, List<GraphEdge> dependencies) {}
