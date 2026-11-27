///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Configuration properties for the remote Flowable support module.
///
/// @param baseUrl the root of the Flowable REST app, for example `http://flowable:8080/flowable-rest`; the usecases
///     use the process REST API below it (`/service`), the adapter the external job REST API (`/external-job-api`)
/// @param username the user for HTTP basic authentication; no authentication when not set
/// @param password the password for HTTP basic authentication
/// @param topic the topic of the external worker tasks; `mifosAdapter` by default
/// @param workerId the name of this worker in the engine
/// @param maxJobs the maximum number of jobs to acquire at once
/// @param lockDuration how long an acquired job stays locked for this worker
/// @param pollInterval the wait before the next acquire when no job was available or the engine did not answer
/// @param retryTimeout the wait before a failed job can be acquired again
/// @param inputVariable the job variable that holds the input
/// @param outputVariable the process variable that receives the output
/// @param failOnNullInput fail the job when the input variable is missing
/// @param serializeResult write the output as JSON text
@ConfigurationProperties(prefix = FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX)
public record FlowableRemoteSupportProperties(
        @DefaultValue("http://localhost:8080/flowable-rest") String baseUrl,

        @Nullable String username,
        @Nullable String password,
        @DefaultValue(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN) String topic,
        @DefaultValue("mifos-workflow") String workerId,
        @DefaultValue("10") int maxJobs,
        @DefaultValue("30s") Duration lockDuration,
        @DefaultValue("1s") Duration pollInterval,
        @DefaultValue("10s") Duration retryTimeout,
        @DefaultValue("input") String inputVariable,
        @DefaultValue("output") String outputVariable,
        @DefaultValue("true") boolean failOnNullInput,
        @DefaultValue("true") boolean serializeResult) {}
