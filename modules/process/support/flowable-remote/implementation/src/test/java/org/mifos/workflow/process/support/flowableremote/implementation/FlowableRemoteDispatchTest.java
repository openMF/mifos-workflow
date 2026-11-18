///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.support.flowableremote.implementation.FlowableRemoteDispatchTestApplication.GreetUsecase;
import org.mifos.workflow.process.support.flowableremote.implementation.FlowableRemoteDispatchTestApplication.ShoutFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.databind.json.JsonMapper;

/// Runs BPMN external worker tasks on a real Flowable engine in a container and checks that
/// `FlowableRemoteAdapter` acquires them, passes the input variable to the dispatched usecase or flow and completes
/// the job with its output. Failures do not roll back as with the embedded engine; the job becomes a dead letter job.
@Slf4j
@SpringBootTest(
        classes = FlowableRemoteDispatchTestApplication.class,
        properties = {
            MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE + "=FLOWABLE_REMOTE",
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED + "=true",
            "mifos.workflow.process.support.flowable-remote.poll-interval=200ms"
        })
final class FlowableRemoteDispatchTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {};

    @Autowired
    private GenericContainer<?> flowable;

    @Autowired
    private JsonMapper jsonMapper;

    private RestClient rest;

    @BeforeEach
    void setUp() {
        rest = RestClient.builder()
                .baseUrl(FlowableRemoteDispatchTestApplication.rootUrl(flowable) + "/service")
                .defaultHeaders(headers -> headers.setBasicAuth(
                        FlowableRemoteDispatchTestApplication.USER, FlowableRemoteDispatchTestApplication.PASSWORD))
                .build();
    }

    @Test
    void dispatchesExternalWorkerJobToUsecaseByCanonicalName() {
        var key = deploy(serviceTask(GreetUsecase.Request.class.getCanonicalName()));

        var processId = start(key, Map.of("input", "{\"name\":\"Ada\"}"));

        var output = await().atMost(TIMEOUT).until(() -> historicVariable(processId, "output"), v -> v != null);
        assertThat(jsonMapper.readValue((String) output, GreetUsecase.Response.class))
                .isEqualTo(new GreetUsecase.Response("Hello Ada"));

        log.warn("Result: {}", output);
    }

    @Test
    void dispatchesExternalWorkerJobToFlowByBinaryName() {
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
    void movesJobToDeadLetterWhenInputIsMissing() {
        var type = GreetUsecase.Request.class.getCanonicalName();
        var key = deploy(serviceTask(type));

        var processId = start(key, Map.of());

        assertThat(awaitDeadLetterMessage(processId)).contains("'input'").contains(type);
        assertThat(isRunning(processId)).isTrue();
    }

    @Test
    void movesJobToDeadLetterForUnknownType() {
        var key = deploy(serviceTask("com.acme.Unknown"));

        var processId = start(key, Map.of("input", "{}"));

        assertThat(awaitDeadLetterMessage(processId)).contains("com.acme.Unknown");
        assertThat(isRunning(processId)).isTrue();
    }

    @Test
    void movesJobToDeadLetterWhenTypeIsMissing() {
        var key = deploy(serviceTask(null));

        var processId = start(key, Map.of("input", "{}"));

        assertThat(awaitDeadLetterMessage(processId))
                .contains("'" + FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE + "'");
    }

    private String awaitDeadLetterMessage(String processId) {
        var jobs = await().atMost(TIMEOUT)
                .until(() -> page("/management/deadletter-jobs?processInstanceId={id}", processId), l -> !l.isEmpty());
        return String.valueOf(jobs.getFirst().get("exceptionMessage"));
    }

    private String deploy(String bpmnTemplate) {
        var key = "dispatch-" + UUID.randomUUID();
        var parts = new MultipartBodyBuilder();
        parts.part("deploymentName", key);
        parts.part("file", bpmnTemplate.formatted(key).getBytes(StandardCharsets.UTF_8))
                .filename(key + ".bpmn")
                .contentType(MediaType.APPLICATION_XML);
        rest.post()
                .uri("/repository/deployments")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts.build())
                .retrieve()
                .toBodilessEntity();
        return key;
    }

    private String start(String key, Map<String, String> variables) {
        var typed = new ArrayList<Map<String, Object>>();
        variables.forEach((name, value) -> typed.add(Map.of("name", name, "type", "string", "value", value)));
        var instance = rest.post()
                .uri("/runtime/process-instances")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("processDefinitionKey", key, "variables", typed))
                .retrieve()
                .body(MAP);
        return (String) instance.get("id");
    }

    private Object historicVariable(String processId, String name) {
        var variables = page(
                "/history/historic-variable-instances?processInstanceId={id}&variableName={name}", processId, name);
        return variables.isEmpty() ? null : ((Map<?, ?>) variables.getFirst().get("variable")).get("value");
    }

    private Object variable(String processId, String name) {
        try {
            return rest.get()
                    .uri("/runtime/process-instances/{id}/variables/{name}", processId, name)
                    .retrieve()
                    .body(MAP)
                    .get("value");
        } catch (HttpClientErrorException.NotFound _) {
            return null;
        }
    }

    private boolean isRunning(String processId) {
        return !page("/runtime/process-instances?id={id}", processId).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> page(String uri, Object... variables) {
        return (List<Map<String, Object>>)
                rest.get().uri(uri, variables).retrieve().body(MAP).get("data");
    }

    /// start -> external worker task dispatching `type` -> end; `%s` is the process key.
    private static String serviceTask(String type) {
        return process("""
                <serviceTask id="dispatch" flowable:type="external-worker" flowable:topic="%s">%s</serviceTask>
                <sequenceFlow id="toEnd" sourceRef="dispatch" targetRef="end"/>
            """.formatted(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, mappings(type)));
    }

    /// start -> external worker task dispatching `type` -> user task (wait state) -> end; `%s` is the process key.
    private static String serviceTaskThenWait(String type) {
        return process("""
                <serviceTask id="dispatch" flowable:type="external-worker" flowable:topic="%s">%s</serviceTask>
                <sequenceFlow id="toWait" sourceRef="dispatch" targetRef="wait"/>
                <userTask id="wait"/>
                <sequenceFlow id="toEnd" sourceRef="wait" targetRef="end"/>
            """.formatted(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, mappings(type)));
    }

    /// Sets the type as a local variable of the service task, so the job carries it.
    private static String mappings(String type) {
        if (type == null) {
            return "";
        }
        return """
            <extensionElements><flowable:executionListener event="start" \
            expression="${execution.setVariableLocal('%s', '%s')}"/></extensionElements>\
            """.formatted(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE, type);
    }

    private static String process(String steps) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         xmlns:flowable="http://flowable.org/bpmn"
                         targetNamespace="http://mifos.org/workflow/test">
              <process id="%s" isExecutable="true">
                <startEvent id="start"/>
                <sequenceFlow id="toDispatch" sourceRef="start" targetRef="dispatch"/>
            """ + steps + """
                <endEvent id="end"/>
              </process>
            </definitions>
            """;
    }
}
