///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.implementation;

import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL;

import java.time.Duration;
import java.util.Locale;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;
import org.mifos.commons.boot.operation.flow.MifosFlow;
import org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportProperties;
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

/// Spring Boot application for the remote CIB seven dispatch tests: the engine runs in a container, the dispatcher
/// is auto-configured by the dispatch starter, and `CibsevenRemoteAdapter` is picked up by component scanning and
/// fetches the external tasks of the engine. A usecase and a flow give the dispatcher something to route to.
@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@EnableConfigurationProperties(CibsevenRemoteSupportProperties.class)
@ComponentScan
class CibsevenRemoteDispatchTestApplication {
    /// The CIB seven Run image; override with `-Dcibseven.image=...` to pin a version.
    static final String IMAGE = System.getProperty("cibseven.image", "cibseven/cibseven:latest");

    static final int PORT = 8080;

    @Bean
    GenericContainer<?> cibseven() {
        return new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withExposedPorts(PORT)
                .waitingFor(Wait.forHttp("/engine-rest/engine")
                        .forPort(PORT)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
    }

    @Bean
    DynamicPropertyRegistrar cibsevenProperties(GenericContainer<?> cibseven) {
        return registry -> registry.add(
                CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL,
                () -> "http://" + cibseven.getHost() + ":" + cibseven.getMappedPort(PORT) + "/engine-rest");
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
