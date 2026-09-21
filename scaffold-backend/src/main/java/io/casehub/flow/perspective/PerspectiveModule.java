package io.casehub.flow.perspective;

import java.util.List;

public record PerspectiveModule(
    List<String> required,
    List<String> optional
) {
    public PerspectiveModule {
        required = required != null ? List.copyOf(required) : List.of();
        optional = optional != null ? List.copyOf(optional) : List.of();
    }
}
