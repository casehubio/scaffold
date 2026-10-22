package io.casehub.flow.case_;

import io.casehub.api.model.converter.CaseDefinitionYamlMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDefinitionYamlTest {

    @ParameterizedTest
    @CsvSource({
        "deployment-approval, scaffold, deployment-approval, 1.0.0",
        "drift-remediation,   scaffold, drift-remediation,   1.0.0",
        "compliance-audit,    scaffold, compliance-audit,     1.0.0",
        "change-management,   scaffold, change-management,    1.0.0",
        "incident-response,   scaffold, incident-response,    1.0.0",
    })
    void yamlDefinitionParsesCorrectly(String file, String namespace, String name, String version) throws Exception {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("casehub/ops/" + file + ".yaml")) {
            assertThat(is).as("YAML file casehub/ops/%s.yaml not found on classpath", file).isNotNull();

            var definition = CaseDefinitionYamlMapper.load(is);

            assertThat(definition.getNamespace()).isEqualTo(namespace);
            assertThat(definition.getName()).isEqualTo(name);
            assertThat(definition.getVersion()).isEqualTo(version);
            assertThat(definition.getBindings()).isNotEmpty();
        }
    }
}
