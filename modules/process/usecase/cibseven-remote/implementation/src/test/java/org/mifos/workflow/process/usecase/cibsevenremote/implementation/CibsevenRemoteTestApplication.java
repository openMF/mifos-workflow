///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL;

import org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.databind.json.JsonMapper;

/// Minimal Spring Boot application for the remote CIB seven usecase tests: the engine runs in a container, the
/// usecases reach it over REST. No DataSource, because the application has no database of its own.
@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@EnableConfigurationProperties(CibsevenRemoteSupportProperties.class)
@ComponentScan
class CibsevenRemoteTestApplication {
    @Bean
    GenericContainer<?> cibseven() {
        return CibsevenRemoteTestEngine.container();
    }

    @Bean
    DynamicPropertyRegistrar cibsevenProperties(GenericContainer<?> cibseven) {
        return registry -> registry.add(
                CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL, () -> CibsevenRemoteTestEngine.baseUrl(cibseven));
    }

    @Bean
    JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }

    @Bean
    CibsevenRemoteTestEngine cibsevenRemoteTestEngine(
            RestClient.Builder builder, CibsevenRemoteSupportProperties properties) {
        return new CibsevenRemoteTestEngine(
                builder.clone().baseUrl(properties.baseUrl()).build());
    }
}
