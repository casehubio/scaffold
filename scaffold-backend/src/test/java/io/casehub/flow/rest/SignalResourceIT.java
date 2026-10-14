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
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SignalResourceIT extends CaseHubIntegrationTestBase {

    @Test
    void sendSignal_nonExistentCase_returns404() {
        UUID nonExistentCaseId = UUID.randomUUID();

        given()
                .contentType(ContentType.JSON)
                .body(
                        """
                        {
                          "path": "test.path",
                          "value": "test"
                        }
                        """)
                .when()
                .post("/api/engine/control/send-signal/{caseId}", nonExistentCaseId)
                .then()
                .statusCode(404)
                .body("title", equalTo("Not found"));
    }

    @Test
    void sendSignal_updatesContextAndTriggersWorkers() {
        UUID caseId = startTestCase();

        given()
                .contentType(ContentType.JSON)
                .body(
                        """
                        {
                          "path": "approval.status",
                          "value": "approved"
                        }
                        """)
                .when()
                .post("/api/engine/control/send-signal/{caseId}", caseId)
                .then()
                .statusCode(200);

        await()
                .atMost(5, SECONDS)
                .untilAsserted(
                        () -> {
                            String contextValue =
                                    given()
                                            .when()
                                            .get("/api/engine/cases/get-case-context-path/{caseId}?path=approval.status", caseId)
                                            .then()
                                            .statusCode(200)
                                            .extract()
                                            .path("value");

                            assertThat(contextValue).isEqualTo("approved");
                        });
    }
}
