///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.implementation;

import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX;

import java.time.Duration;
import java.util.Locale;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;
import org.mifos.commons.boot.operation.flow.MifosFlow;
import org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

/// Spring Boot application for the remote Flowable dispatch tests: the Flowable REST app runs in a container, the
/// dispatcher is auto-configured by the dispatch starter, and `FlowableRemoteAdapter` is picked up by component
/// scanning and acquires the external worker jobs of the engine. A usecase and a flow give the dispatcher something
/// to route to.
@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@EnableConfigurationProperties(FlowableRemoteSupportProperties.class)
@ComponentScan
class FlowableRemoteDispatchTestApplication {
    /// The Flowable REST app image; override with `-Dflowable.image=...` to pin a version.
    static final String IMAGE = System.getProperty("flowable.image", "flowable/flowable-rest:latest");

    /// The default administrator of the Flowable REST app.
    static final String USER = "rest-admin";

    static final String PASSWORD = "test";

    static final int PORT = 8080;

    @Bean
    GenericContainer<?> flowable() {
        return new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withExposedPorts(PORT)
                .waitingFor(Wait.forHttp("/flowable-rest/service/management/engine")
                        .forPort(PORT)
                        .withBasicCredentials(USER, PASSWORD)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
    }

    /// The root of the Flowable REST app, for example `http://localhost:32768/flowable-rest`.
    static String rootUrl(GenericContainer<?> flowable) {
        return "http://" + flowable.getHost() + ":" + flowable.getMappedPort(PORT) + "/flowable-rest";
    }

    @Bean
    DynamicPropertyRegistrar flowableProperties(GenericContainer<?> flowable) {
        return registry -> {
            registry.add(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL, () -> rootUrl(flowable));
            registry.add(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".username", () -> USER);
            registry.add(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".password", () -> PASSWORD);
        };
    }

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }

    @Bean
    GreetUsecase greetUsecase() {
        return new GreetUsecase();
    }

    @Bean
    ShoutFlow shoutFlow() {
        return new ShoutFlow();
    }

    static final class GreetUsecase implements MifosUsecase<GreetUsecase.Request, GreetUsecase.Response> {
        @Override
        public Response execute(Request request) {
            return new Response("Hello " + request.name());
        }

        record Request(String name) implements MifosUsecase.MifosRequest {}

        record Response(String greeting) implements MifosUsecase.MifosResponse {}
    }

    static final class ShoutFlow implements MifosFlow<ShoutFlow.Ctx> {
        @Override
        public Ctx run(Ctx context) {
            return new Ctx(context.text(), context.text().toUpperCase(Locale.ROOT));
        }

        record Ctx(String text, String result) implements MifosFlow.MifosFlowContext {}
    }
}
