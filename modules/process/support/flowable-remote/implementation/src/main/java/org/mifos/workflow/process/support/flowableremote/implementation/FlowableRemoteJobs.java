///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.implementation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/// The external job REST API of Flowable (`/flowable-rest/external-job-api`): acquire, complete and fail jobs.
final class FlowableRemoteJobs {
    /// The external job REST API, below the root of the Flowable REST app.
    private static final String EXTERNAL_JOB_API = "/external-job-api";

    private static final ParameterizedTypeReference<List<Job>> JOBS = new ParameterizedTypeReference<>() {};

    private final RestClient rest;
    private final FlowableRemoteSupportProperties properties;

    FlowableRemoteJobs(RestClient.Builder builder, FlowableRemoteSupportProperties properties) {
        this.properties = properties;
        var client = builder.clone().baseUrl(properties.baseUrl());
        var username = properties.username();
        if (username != null) {
            var password = Objects.requireNonNullElse(properties.password(), "");
            client.defaultHeaders(headers -> headers.setBasicAuth(username, password));
        }
        this.rest = client.build();
    }

    List<Job> acquire() {
        var jobs = rest.post()
                .uri(EXTERNAL_JOB_API + "/acquire/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Acquire(
                        properties.topic(),
                        properties.lockDuration().toString(),
                        properties.maxJobs(),
                        properties.workerId()))
                .retrieve()
                .body(JOBS);
        return jobs == null ? List.of() : jobs;
    }

    void complete(Job job, List<Variable> variables) {
        rest.post()
                .uri(EXTERNAL_JOB_API + "/acquire/jobs/{id}/complete", job.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Complete(properties.workerId(), variables))
                .retrieve()
                .toBodilessEntity();
    }

    void fail(Job job, String message, String details, int retries, Duration retryTimeout) {
        rest.post()
                .uri(EXTERNAL_JOB_API + "/acquire/jobs/{id}/fail", job.id())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Fail(properties.workerId(), message, details, retries, retryTimeout.toString()))
                .retrieve()
                .toBodilessEntity();
    }

    /// A typed variable, for example `{"name": "output", "type": "string", "value": "{...}"}`.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Variable(
            String name, @Nullable String type, @Nullable Object value) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Job(
            String id,
            @Nullable String elementId,
            @Nullable String processInstanceId,
            @Nullable Integer retries,
            @Nullable List<Variable> variables) {}

    record Acquire(String topic, String lockDuration, int numberOfTasks, String workerId) {}

    record Complete(String workerId, List<Variable> variables) {}

    record Fail(String workerId, String errorMessage, String errorDetails, int retries, String retryTimeout) {}
}
