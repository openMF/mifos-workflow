///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/// Test access to the remote engine over plain REST, independent of the client under test.
///
/// Results are JSON maps; query results are the `data` of one large page, values of typed variables are unwrapped.
final class FlowableRemoteTestEngine {
    /// The Flowable REST app image; override with `-Dflowable.image=...` to pin a version.
    static final String IMAGE = System.getProperty("flowable.image", "flowable/flowable-rest:latest");

    /// The default administrator of the Flowable REST app.
    static final String USER = "rest-admin";

    static final String PASSWORD = "test";

    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "/flowable-rest";
    private static final String SERVICE_PATH = CONTEXT_PATH + "/service";
    private static final String PAGE = "size=1000";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {};

    private final RestClient rest;

    FlowableRemoteTestEngine(RestClient rest) {
        this.rest = rest;
    }

    static GenericContainer<?> container() {
        return new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withExposedPorts(PORT)
                .waitingFor(Wait.forHttp(SERVICE_PATH + "/management/engine")
                        .forPort(PORT)
                        .withBasicCredentials(USER, PASSWORD)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
    }

    /// The root of the Flowable REST app, for example `http://localhost:32768/flowable-rest`.
    static String rootUrl(GenericContainer<?> container) {
        return "http://" + container.getHost() + ":" + container.getMappedPort(PORT) + CONTEXT_PATH;
    }

    String deploy(String key, String bpmn) {
        var parts = new MultipartBodyBuilder();
        parts.part("deploymentName", key);
        parts.part("file", bpmn.getBytes(StandardCharsets.UTF_8))
                .filename(key + ".bpmn")
                .contentType(MediaType.APPLICATION_XML);
        var deployment = rest.post()
                .uri("/repository/deployments")
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
        var typed = new ArrayList<Map<String, Object>>();
        variables.forEach((name, value) -> typed.add(Map.of("name", name, "type", type(value), "value", value)));
        var instance = rest.post()
                .uri("/runtime/process-instances")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("processDefinitionKey", key, "variables", typed))
                .retrieve()
                .body(MAP);
        return (String) instance.get("id");
    }

    boolean isRunning(String processId) {
        return !page("/runtime/process-instances?id={id}", processId).isEmpty();
    }

    List<Map<String, Object>> processInstances(String key) {
        return page("/runtime/process-instances?processDefinitionKey={key}", key);
    }

    List<Map<String, Object>> historicProcessInstances(String key) {
        return page("/history/historic-process-instances?processDefinitionKey={key}", key);
    }

    Map<String, Object> historicProcessInstance(String processId) {
        return rest.get()
                .uri("/history/historic-process-instances/{id}", processId)
                .retrieve()
                .body(MAP);
    }

    Map<String, Object> variables(String processId) {
        var variables = new HashMap<String, Object>();
        rest.get()
                .uri("/runtime/process-instances/{id}/variables", processId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .forEach(v -> variables.put((String) v.get("name"), v.get("value")));
        return variables;
    }

    Map<String, Object> historicVariables(String processId) {
        var variables = new HashMap<String, Object>();
        page("/history/historic-variable-instances?processInstanceId={id}", processId)
                .forEach(v -> {
                    var variable = (Map<?, ?>) v.get("variable");
                    variables.put((String) variable.get("name"), variable.get("value"));
                });
        return variables;
    }

    List<Map<String, Object>> tasks(String processId) {
        return page("/runtime/tasks?processInstanceId={id}", processId);
    }

    long taskCount(String taskId) {
        return page("/runtime/tasks?taskId={id}", taskId).size();
    }

    void completeTask(String taskId) {
        rest.post()
                .uri("/runtime/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("action", "complete"))
                .retrieve()
                .toBodilessEntity();
    }

    /// The deployment list has no id filter, so the deployment is read directly.
    boolean deploymentExists(String deploymentId) {
        try {
            rest.get()
                    .uri("/repository/deployments/{id}", deploymentId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound _) {
            return false;
        }
    }

    List<String> deploymentResourceNames(String deploymentId) {
        return rest
                .get()
                .uri("/repository/deployments/{id}/resources", deploymentId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .stream()
                .map(resource -> (String) resource.get("id"))
                .toList();
    }

    List<Map<String, Object>> processDefinitions(String key) {
        return page("/repository/process-definitions?key={key}", key);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> page(String uri, Object... variables) {
        var page = rest.get()
                .uri(uri + (uri.contains("?") ? "&" : "?") + PAGE, variables)
                .retrieve()
                .body(MAP);
        return (List<Map<String, Object>>) page.get("data");
    }

    private static String type(Object value) {
        return switch (value) {
            case Integer _ -> "integer";
            case Boolean _ -> "boolean";
            case Long _ -> "long";
            case Double _ -> "double";
            default -> "string";
        };
    }
}
