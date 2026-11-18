///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.camunda.implementation;

import java.util.Locale;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;
import org.mifos.commons.boot.operation.flow.MifosFlow;
import org.mifos.workflow.process.support.camunda.core.CamundaSupportProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tools.jackson.databind.json.JsonMapper;

/// Spring Boot application for the Camunda dispatch tests: the Camunda client is auto-configured by the Camunda
/// starter and pointed at a Testcontainers-managed Camunda runtime by Camunda Process Test, the dispatcher comes from
/// the dispatch starter, and `CamundaAdapter` is picked up by component scanning. A usecase and a flow give the
/// dispatcher something to route to.
///
/// Camunda 8 keeps its state in the broker, so there is no application database; JDBC is on the test classpath anyway,
/// so its `DataSource` auto-configuration is excluded rather than left to fail for want of a URL.
@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@EnableConfigurationProperties(CamundaSupportProperties.class)
@ComponentScan
class CamundaDispatchTestApplication {
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
