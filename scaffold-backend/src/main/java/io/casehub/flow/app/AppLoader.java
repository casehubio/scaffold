package io.casehub.flow.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.casehub.yaml.jackson.YamlMappers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppLoader {

    private static final ObjectMapper YAML = YamlMappers.create();

    public static Optional<AppConfig> parse(String yaml) {
        if (yaml == null || yaml.isBlank()) return Optional.empty();
        try {
            var tree = YAML.readTree(yaml);
            var app = tree.get("app");
            if (app == null) return Optional.empty();

            var name = app.path("name").asText();
            var description = app.path("description").asText("");
            var version = app.path("version").asText("1.0");

            var modulesNode = app.path("modules");
            var required = readStringList(modulesNode, "required");
            var optional = readStringList(modulesNode, "optional");
            var modules = new AppModule(required, optional);

            var tabsList = new ArrayList<AppTab>();
            var viewsNode = app.path("views").path("tabs");
            if (viewsNode.isArray()) {
                for (var tabNode : viewsNode) {
                    tabsList.add(new AppTab(
                        tabNode.path("id").asText(),
                        tabNode.path("label").asText(),
                        tabNode.path("view").asText()
                    ));
                }
            }

            var datasetsList = new ArrayList<AppDataset>();
            var datasetsNode = app.path("datasets");
            if (datasetsNode.isObject()) {
                var it = datasetsNode.fields();
                while (it.hasNext()) {
                    var entry = it.next();
                    datasetsList.add(new AppDataset(
                        entry.getKey(),
                        entry.getValue().path("endpoint").asText(),
                        entry.getValue().path("source").asText()
                    ));
                }
            }

            var connectionsList = new ArrayList<AppConnection>();
            var connectionsNode = app.path("connections");
            if (connectionsNode.isObject()) {
                var cit = connectionsNode.fields();
                while (cit.hasNext()) {
                    var entry = cit.next();
                    connectionsList.add(new AppConnection(
                        entry.getKey(),
                        entry.getValue().path("endpoint").asText(),
                        entry.getValue().path("type").asText("sse"),
                        entry.getValue().path("source").asText()
                    ));
                }
            }

            return Optional.of(new AppConfig(name, description, version, modules,
                List.copyOf(tabsList), List.copyOf(datasetsList), List.copyOf(connectionsList)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<AppConfig> loadFromClasspath() {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/app.yaml")) {
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
