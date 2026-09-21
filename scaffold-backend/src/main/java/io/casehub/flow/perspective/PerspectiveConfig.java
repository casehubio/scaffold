package io.casehub.flow.perspective;

import java.util.List;

public record PerspectiveConfig(
    String name,
    String description,
    String version,
    PerspectiveModule modules,
    List<PerspectiveTab> tabs,
    List<PerspectiveDataset> datasets
) {}
