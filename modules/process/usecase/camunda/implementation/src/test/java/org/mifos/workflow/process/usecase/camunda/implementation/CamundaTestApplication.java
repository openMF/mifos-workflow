///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/// Minimal Spring Boot application for the Camunda usecase tests: the Camunda client is auto-configured by the
/// Camunda starter and pointed at a Testcontainers-managed Camunda runtime by Camunda Process Test; usecases are
/// picked up by component scanning.
///
/// Camunda 8 keeps its state in the broker, so there is no application database; JDBC is on the test classpath anyway,
/// so its `DataSource` auto-configuration is excluded rather than left to fail for want of a URL.
@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@ComponentScan
class CamundaTestApplication {}
