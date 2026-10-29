package io.casehub.flow.service;

import java.util.List;

public record GraphNode(String id, String type, List<String> dependsOn) {}
