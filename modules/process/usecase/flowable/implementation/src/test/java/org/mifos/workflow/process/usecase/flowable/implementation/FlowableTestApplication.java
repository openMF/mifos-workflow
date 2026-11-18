///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowable.implementation;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.testcontainers.postgresql.PostgreSQLContainer;

/// Minimal Spring Boot application for the Flowable usecase tests: the embedded engine is auto-configured by the
/// Flowable starter and backed by a PostgreSQL container; usecases and mappers are picked up by component scanning.
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
class FlowableTestApplication {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }
}
