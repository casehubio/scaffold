package io.casehub.flow.perspective;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PerspectiveLoader {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    public static Optional<PerspectiveConfig> parse(String yaml) {
        if (yaml == null || yaml.isBlank()) return Optional.empty();
        try {
            var tree = YAML.readTree(yaml);
            var perspective = tree.get("perspective");
            if (perspective == null) return Optional.empty();

            var name = perspective.path("name").asText();
            var description = perspective.path("description").asText("");
            var version = perspective.path("version").asText("1.0");

            var modulesNode = perspective.path("modules");
            var required = readStringList(modulesNode, "required");
            var optional = readStringList(modulesNode, "optional");
            var modules = new PerspectiveModule(required, optional);

            var tabsList = new ArrayList<PerspectiveTab>();
            var viewsNode = perspective.path("views").path("tabs");
            if (viewsNode.isArray()) {
                for (var tabNode : viewsNode) {
                    tabsList.add(new PerspectiveTab(
                        tabNode.path("id").asText(),
                        tabNode.path("label").asText(),
                        tabNode.path("view").asText()
                    ));
                }
            }

            var datasetsList = new ArrayList<PerspectiveDataset>();
            var datasetsNode = perspective.path("datasets");
            if (datasetsNode.isObject()) {
                var it = datasetsNode.fields();
                while (it.hasNext()) {
                    var entry = it.next();
                    datasetsList.add(new PerspectiveDataset(
                        entry.getKey(),
                        entry.getValue().path("endpoint").asText(),
                        entry.getValue().path("source").asText()
                    ));
                }
            }

            return Optional.of(new PerspectiveConfig(name, description, version, modules,
                List.copyOf(tabsList), List.copyOf(datasetsList)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<PerspectiveConfig> loadFromClasspath() {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/perspective.yaml")) {
            if (is == null) return Optional.empty();
            return parse(new String(is.readAllBytes()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static List<String> readStringList(JsonNode parent, String field) {
        var node = parent.path(field);
        if (!node.isArray()) return List.of();
        var list = new ArrayList<String>();
        for (var item : node) list.add(item.asText());
        return list;
    }
}
