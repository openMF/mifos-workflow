///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/// Test access to the remote engine over plain REST, independent of the client under test.
///
/// Results are JSON maps; values of typed variables are unwrapped.
final class CibsevenRemoteTestEngine {
    /// The CIB seven Run image; override with `-Dcibseven.image=...` to pin a version.
    static final String IMAGE = System.getProperty("cibseven.image", "cibseven/cibseven:latest");

    private static final int PORT = 8080;
    private static final String REST_PATH = "/engine-rest";
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {};

    private final RestClient rest;

    CibsevenRemoteTestEngine(RestClient rest) {
        this.rest = rest;
    }

    static GenericContainer<?> container() {
        return new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withExposedPorts(PORT)
                .waitingFor(Wait.forHttp(REST_PATH + "/engine")
                        .forPort(PORT)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
    }

    static String baseUrl(GenericContainer<?> container) {
        return "http://" + container.getHost() + ":" + container.getMappedPort(PORT) + REST_PATH;
    }

    String deploy(String key, String bpmn) {
        var parts = new MultipartBodyBuilder();
        parts.part("deployment-name", key);
        parts.part("data", bpmn.getBytes(StandardCharsets.UTF_8))
                .filename(key + ".bpmn")
                .contentType(MediaType.APPLICATION_XML);
        var deployment = rest.post()
                .uri("/deployment/create")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts.build())
                .retrieve()
                .body(MAP);
        return (String) deployment.get("id");
    }

    String start(String key) {
        return start(key, Map.of());
    }

    String start(String key, Map<String, Object> variables) {
        var typed = new HashMap<String, Object>();
        variables.forEach((name, value) -> typed.put(name, typed(value)));
        var instance = rest.post()
                .uri("/process-definition/key/{key}/start", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("variables", typed))
                .retrieve()
                .body(MAP);
        return (String) instance.get("id");
    }

    boolean isRunning(String processId) {
        return !list("/process-instance?processInstanceIds={id}", processId).isEmpty();
    }

    List<Map<String, Object>> processInstances(String key) {
        return list("/process-instance?processDefinitionKey={key}", key);
    }

    Map<String, Object> processInstance(String processId) {
        return rest.get().uri("/process-instance/{id}", processId).retrieve().body(MAP);
    }

    List<Map<String, Object>> historicProcessInstances(String key) {
        return list("/history/process-instance?processDefinitionKey={key}", key);
    }

    Map<String, Object> historicProcessInstance(String processId) {
        return rest.get()
                .uri("/history/process-instance/{id}", processId)
                .retrieve()
                .body(MAP);
    }

    Map<String, Object> variables(String processId) {
        var variables = new HashMap<String, Object>();
        var typed = rest.get()
                .uri("/process-instance/{id}/variables", processId)
                .retrieve()
                .body(MAP);
        typed.forEach((name, value) -> variables.put(name, ((Map<?, ?>) value).get("value")));
        return variables;
    }

    Map<String, Object> historicVariables(String processId) {
        var variables = new HashMap<String, Object>();
        list("/history/variable-instance?processInstanceId={id}", processId)
                .forEach(v -> variables.put((String) v.get("name"), v.get("value")));
        return variables;
    }

    List<Map<String, Object>> tasks(String processId) {
        return list("/task?processInstanceId={id}", processId);
    }

    long taskCount(String taskId) {
        return list("/task?taskId={id}", taskId).size();
    }

    void completeTask(String taskId) {
        rest.post()
                .uri("/task/{id}/complete", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .retrieve()
                .toBodilessEntity();
    }

    List<Map<String, Object>> deployments(String name) {
        return list("/deployment?name={name}", name);
    }

    List<Map<String, Object>> deploymentsById(String deploymentId) {
        return list("/deployment?id={id}", deploymentId);
    }

    List<String> deploymentResourceNames(String deploymentId) {
        return list("/deployment/{id}/resources", deploymentId).stream()
                .map(resource -> (String) resource.get("name"))
                .toList();
    }

    List<Map<String, Object>> processDefinitions(String key) {
        return list("/process-definition?key={key}", key);
    }

    List<Map<String, Object>> processDefinitions(String key, String deploymentId) {
        return list("/process-definition?key={key}&deploymentId={deploymentId}", key, deploymentId);
    }

    private List<Map<String, Object>> list(String uri, Object... variables) {
        return rest.get().uri(uri, variables).retrieve().body(LIST);
    }

    private static Map<String, Object> typed(Object value) {
        var type = switch (value) {
            case Integer _ -> "Integer";
            case Boolean _ -> "Boolean";
            case Long _ -> "Long";
            case Double _ -> "Double";
            default -> "String";
        };
        return Map.of("value", value, "type", type);
    }
}
