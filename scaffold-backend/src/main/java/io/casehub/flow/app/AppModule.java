package io.casehub.flow.app;

import java.util.List;

public record AppModule(
    List<String> required,
    List<String> optional
) {
    public AppModule {
        required = required != null ? List.copyOf(required) : List.of();
        optional = optional != null ? List.copyOf(optional) : List.of();
    }
}
