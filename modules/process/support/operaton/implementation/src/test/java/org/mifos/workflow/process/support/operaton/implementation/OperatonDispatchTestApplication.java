///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.operaton.implementation;

import java.util.Locale;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;
import org.mifos.commons.boot.operation.flow.MifosFlow;
import org.mifos.workflow.process.support.operaton.core.OperatonSupportProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

/// Spring Boot application for the Operaton dispatch tests: the embedded engine is auto-configured by the Operaton
/// starter against a PostgreSQL container, the dispatcher by the dispatch starter, and `OperatonAdapter` is picked up
/// by component scanning. A usecase and a flow give the dispatcher something to route to.
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties(OperatonSupportProperties.class)
@ComponentScan
class OperatonDispatchTestApplication {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
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
