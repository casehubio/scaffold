package io.casehub.flow.app;

import java.util.List;

public record AppConfig(
        String name,
        String description,
        String version,
        AppModule modules,
        List<AppTab> tabs,
        List<AppDataset> datasets,
        List<AppConnection> connections
) {}
