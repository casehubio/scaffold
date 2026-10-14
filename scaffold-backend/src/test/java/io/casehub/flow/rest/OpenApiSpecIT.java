/*
 * Copyright 2026-Present The Case Hub Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.casehub.flow.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class OpenApiSpecIT extends CaseHubIntegrationTestBase {

  @Test
  void openApiEndpointReturnsValidSpec() {
    given()
        .when()
        .get("/q/openapi?format=JSON")
        .then()
        .statusCode(200);
  }

  @Test
  void openApiSpecContainsApiInfo() {
    Response response = given()
        .when()
        .get("/q/openapi?format=JSON")
        .then()
        .statusCode(200)
        .extract().response();

    Map<String, Object> info = response.jsonPath().getMap("info");

    assertThat(info.get("title")).isEqualTo("CaseHub Flow API");
    assertThat(info.get("version")).isEqualTo("1.0.0");
  }

    @Test
    void openApiSpecContainsAllPaths() {
        Response response = given()
                                    .when()
                                    .get("/q/openapi?format=JSON")
                                    .then()
                                    .statusCode(200)
                                    .extract().response();

        Map<String, Object> paths = response.jsonPath().getMap("paths");

        assertThat(paths).containsKey("/api/engine/definitions/list-definitions");
        assertThat(paths).containsKey("/api/engine/definitions/get-definitions-by-name/{namespace}/{name}");
        assertThat(paths).containsKey("/api/engine/definitions/get-definition-by-key/{namespace}/{name}/{version}");
        assertThat(paths).containsKey("/api/engine/cases/start-case");
        assertThat(paths).containsKey("/api/engine/cases/get-case-by-id/{caseId}");
        assertThat(paths).containsKey("/api/engine/cases/get-case-context/{caseId}");
        assertThat(paths).containsKey("/api/engine/control/suspend-case/{caseId}");
        assertThat(paths).containsKey("/api/engine/control/resume-case/{caseId}");
        assertThat(paths).containsKey("/api/engine/control/cancel-case/{caseId}");
        assertThat(paths).containsKey("/api/engine/control/send-signal/{caseId}");
    }

    @Test
    void openApiSpecContainsResponseCodes() {
        Response response = given()
                                    .when()
                                    .get("/q/openapi?format=JSON")
                                    .then()
                                    .statusCode(200)
                                    .extract().response();

        Map<String, Object> listAllResponses = response.jsonPath()
                                                       .getMap("paths.'/api/engine/definitions/list-definitions'.get.responses");
        assertThat(listAllResponses).containsKey("200");

        Map<String, Object> startCaseResponses = response.jsonPath()
                                                         .getMap("paths.'/api/engine/cases/start-case'.post.responses");
        assertThat(startCaseResponses).containsKey("200");
    }

    @Test
    void openApiSpecContainsSchemaComponents() {
        Response response = given()
                                    .when()
                                    .get("/q/openapi?format=JSON")
                                    .then()
                                    .statusCode(200)
                                    .extract().response();

        Map<String, Object> schemas = response.jsonPath()
                                              .getMap("components.schemas");

        assertThat(schemas).containsKey("StartCaseRequest");
        assertThat(schemas).containsKey("SendSignalRequest");
    }

  @Test
  void swaggerUiIsAvailable() {
    given()
        .when()
        .get("/q/swagger-ui")
        .then()
        .statusCode(200)
        .body(containsString("swagger-ui"));
  }
}
