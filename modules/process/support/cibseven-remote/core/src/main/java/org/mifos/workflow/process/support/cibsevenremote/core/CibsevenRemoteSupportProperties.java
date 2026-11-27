///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Configuration properties for the remote CIB seven support module.
///
/// @param baseUrl the root of the engine REST API, including the `/engine-rest` path
/// @param username the user for HTTP basic authentication; no authentication when not set
/// @param password the password for HTTP basic authentication
/// @param topic the external task topic of the service tasks; `mifosAdapter` by default
/// @param workerId the name of this worker in the engine
/// @param maxTasks the maximum number of tasks to fetch at once
/// @param lockDuration how long a fetched task stays locked for this worker
/// @param asyncResponseTimeout how long a fetch waits for new tasks (long polling)
/// @param retries the retries of a task that fails for the first time
/// @param retryTimeout the wait before a failed task can be fetched again
/// @param inputVariable the process variable that holds the input
/// @param outputVariable the process variable that receives the output
/// @param failOnNullInput fail the task when the input variable is missing
/// @param serializeResult write the output as JSON text
@ConfigurationProperties(prefix = CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX)
public record CibsevenRemoteSupportProperties(
        @DefaultValue("http://localhost:8080/engine-rest") String baseUrl,
        @Nullable String username,
        @Nullable String password,
        @DefaultValue(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN) String topic,
        @DefaultValue("mifos-workflow") String workerId,
        @DefaultValue("10") int maxTasks,
        @DefaultValue("30s") Duration lockDuration,
        @DefaultValue("20s") Duration asyncResponseTimeout,
        @DefaultValue("3") int retries,
        @DefaultValue("10s") Duration retryTimeout,
        @DefaultValue("input") String inputVariable,
        @DefaultValue("output") String outputVariable,
        @DefaultValue("true") boolean failOnNullInput,
        @DefaultValue("true") boolean serializeResult) {}
