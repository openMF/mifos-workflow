///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PASSWORD;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_USERNAME;

import org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.databind.json.JsonMapper;

/// Minimal Spring Boot application for the remote Flowable usecase tests: the Flowable REST app runs in a
/// container, the usecases reach it over REST. No DataSource, because the application has no database of its own.
@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@EnableConfigurationProperties(FlowableRemoteSupportProperties.class)
@ComponentScan
class FlowableRemoteTestApplication {
    @Bean
    GenericContainer<?> flowable() {
        return FlowableRemoteTestEngine.container();
    }

    @Bean
    DynamicPropertyRegistrar flowableProperties(GenericContainer<?> flowable) {
        return registry -> {
            registry.add(
                    FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL,
                    () -> FlowableRemoteTestEngine.rootUrl(flowable));
            registry.add(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_USERNAME, () -> FlowableRemoteTestEngine.USER);
            registry.add(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PASSWORD, () -> FlowableRemoteTestEngine.PASSWORD);
        };
    }

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }

    @Bean
    FlowableRemoteTestEngine flowableRemoteTestEngine(
            RestClient.Builder builder, FlowableRemoteSupportProperties properties) {
        return new FlowableRemoteTestEngine(builder.clone()
                .baseUrl(properties.baseUrl() + "/service")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(FlowableRemoteTestEngine.USER, FlowableRemoteTestEngine.PASSWORD))
                .build());
    }
}
