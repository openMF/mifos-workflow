///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;
import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE;
import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportProperties;
import org.mifos.workflow.process.support.cibsevenremote.implementation.CibsevenRemoteDispatchTestApplication.GreetUsecase;
import org.mifos.workflow.process.support.cibsevenremote.implementation.CibsevenRemoteDispatchTestApplication.ShoutFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/// Runs BPMN external tasks on a real CIB seven engine in a container and checks that `CibsevenRemoteAdapter`
/// fetches them, passes the input variable to the dispatched usecase or flow and completes the task with its
/// output. Failures do not roll back as with the embedded engine; they raise an incident.
@Slf4j
@SpringBootTest(
        classes = CibsevenRemoteDispatchTestApplication.class,
        properties = {
            MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE + "=CIBSEVEN_REMOTE",
            CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED + "=true",
            // short polls, so the tests do not wait for the long-poll timeout when the context stops
            "mifos.workflow.process.support.cibseven-remote.async-response-timeout=2s"
        })
final class CibsevenRemoteDispatchTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {};

    @Autowired
    private CibsevenRemoteSupportProperties properties;

    @Autowired
    private JsonMapper jsonMapper;

    private RestClient rest;

    @BeforeEach
    void setUp() {
        rest = RestClient.create(properties.baseUrl());
    }

    @Test
    void dispatchesExternalTaskToUsecaseByCanonicalName() {
        var key = deploy(serviceTask(GreetUsecase.Request.class.getCanonicalName()));

        var processId = start(key, Map.of("input", "{\"name\":\"Ada\"}"));

        var output = await().atMost(TIMEOUT).until(() -> historicVariable(processId, "output"), v -> v != null);
        assertThat(jsonMapper.readValue((String) output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Ada"));

        log.warn("Result: {}", output);
    }

    @Test
    void dispatchesExternalTaskToFlowByBinaryName() {
        var key = deploy(serviceTask(ShoutFlow.Ctx.class.getName()));

        var processId = start(key, Map.of("input", "{\"text\":\"hi\"}"));

        var output = await().atMost(TIMEOUT).until(() -> historicVariable(processId, "output"), v -> v != null);
        assertThat(jsonMapper.readValue((String) output, ShoutFlow.Ctx.class)).isEqualTo(new ShoutFlow.Ctx("hi", "HI"));

        log.warn("Result: {}", output);
    }

    @Test
    void keepsOutputAcrossWaitState() {
        var key = deploy(serviceTaskThenWait(GreetUsecase.Request.class.getCanonicalName()));

        var processId = start(key, Map.of("input", "{\"name\":\"Grace\"}"));

        var output = await().atMost(TIMEOUT).until(() -> variable(processId, "output"), v -> v != null);
        assertThat(jsonMapper.readValue((String) output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Grace"));
        assertThat(isRunning(processId)).isTrue();

        log.warn("Result: {}", output);
    }

    @Test
    void raisesIncidentWhenInputIsMissing() {
        var type = GreetUsecase.Request.class.getCanonicalName();
        var key = deploy(serviceTask(type));

        var processId = start(key, Map.of());

        assertThat(awaitIncidentMessage(processId)).contains("'input'").contains(type);
        assertThat(isRunning(processId)).isTrue();
    }

    @Test
    void raisesIncidentForUnknownType() {
        var key = deploy(serviceTask("com.acme.Unknown"));

        var processId = start(key, Map.of("input", "{}"));

        assertThat(awaitIncidentMessage(processId)).contains("com.acme.Unknown");
        assertThat(isRunning(processId)).isTrue();
    }

    @Test
    void raisesIncidentWhenTypeIsMissing() {
        var key = deploy(serviceTask(null));

        var processId = start(key, Map.of("input", "{}"));

        assertThat(awaitIncidentMessage(processId))
                .contains("'" + CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE + "'");
    }

    private String awaitIncidentMessage(String processId) {
        var incidents = await().atMost(TIMEOUT)
                .until(() -> list("/incident?processInstanceId={id}", processId), list -> !list.isEmpty());
        return String.valueOf(incidents.getFirst().get("incidentMessage"));
    }

    private String deploy(String bpmnTemplate) {
        var key = "dispatch-" + UUID.randomUUID();
        var parts = new MultipartBodyBuilder();
        parts.part("deployment-name", key);
        parts.part("data", bpmnTemplate.formatted(key).getBytes(StandardCharsets.UTF_8))
                .filename(key + ".bpmn")
                .contentType(MediaType.APPLICATION_XML);
        rest.post()
                .uri("/deployment/create")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts.build())
                .retrieve()
                .toBodilessEntity();
        return key;
    }

    private String start(String key, Map<String, String> variables) {
        var typed = new HashMap<String, Object>();
        variables.forEach((name, value) -> typed.put(name, Map.of("value", value, "type", "String")));
        var instance = rest.post()
                .uri("/process-definition/key/{key}/start", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("variables", typed))
                .retrieve()
                .body(MAP);
        return (String) instance.get("id");
    }

    private Object historicVariable(String processId, String name) {
        var variables = list("/history/variable-instance?processInstanceId={id}&variableName={name}", processId, name);
        return variables.isEmpty() ? null : variables.getFirst().get("value");
    }

    private Object variable(String processId, String name) {
        var variables = list("/variable-instance?processInstanceIdIn={id}&variableName={name}", processId, name);
        return variables.isEmpty() ? null : variables.getFirst().get("value");
    }

    private boolean isRunning(String processId) {
        return !list("/process-instance?processInstanceIds={id}", processId).isEmpty();
    }

    private List<Map<String, Object>> list(String uri, Object... variables) {
        return rest.get().uri(uri, variables).retrieve().body(LIST);
    }

    /// start -> external task dispatching `type` -> end; `%s` is the process key.
    private static String serviceTask(String type) {
        return process("""
                <serviceTask id="dispatch" camunda:type="external" camunda:topic="%s">%s</serviceTask>
                <sequenceFlow id="toEnd" sourceRef="dispatch" targetRef="end"/>
            """.formatted(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, typeProperty(type)));
    }

    /// start -> external task dispatching `type` -> user task (wait state) -> end; `%s` is the process key.
    private static String serviceTaskThenWait(String type) {
        return process("""
                <serviceTask id="dispatch" camunda:type="external" camunda:topic="%s">%s</serviceTask>
                <sequenceFlow id="toWait" sourceRef="dispatch" targetRef="wait"/>
                <userTask id="wait"/>
                <sequenceFlow id="toEnd" sourceRef="wait" targetRef="end"/>
            """.formatted(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, typeProperty(type)));
    }

    private static String typeProperty(String type) {
        if (type == null) {
            return "";
        }
        return """
            <extensionElements><camunda:properties><camunda:property name="%s" value="%s"/></camunda:properties>\
            </extensionElements>\
            """.formatted(CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE, type);
    }

    private static String process(String steps) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                         targetNamespace="http://mifos.org/workflow/test">
              <process id="%s" isExecutable="true" camunda:historyTimeToLive="P1D">
                <startEvent id="start"/>
                <sequenceFlow id="toDispatch" sourceRef="start" targetRef="dispatch"/>
            """ + steps + """
                <endEvent id="end"/>
              </process>
            </definitions>
            """;
    }
}
