///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.core;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE;
import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

/// Constants for the remote Flowable support module.
@UtilityClass
public final class FlowableRemoteSupportConstants {
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_PACKAGE_BASE =
            MIFOS_WORKFLOW_PROCESS_PACKAGE_BASE + ".support.flowableremote";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_CORE_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_FLOWABLE_REMOTE_PACKAGE_BASE + ".implementation";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_PROCESS_SUPPORT_PROPERTIES_PREFIX + ".flowable-remote";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".enabled";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_BASE_URL =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".base-url";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_USERNAME =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".username";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PASSWORD =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".password";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_TOPIC =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".topic";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_WORKER_ID =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".worker-id";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_MAX_JOBS =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".max-jobs";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_LOCK_DURATION =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".lock-duration";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_POLL_INTERVAL =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".poll-interval";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_RETRY_TIMEOUT =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".retry-timeout";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_INPUT_VARIABLE =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".input-variable";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_OUTPUT_VARIABLE =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".output-variable";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_FAIL_ON_NULL_INPUT =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".fail-on-null-input";
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_SERIALIZE_RESULT =
            FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_PREFIX + ".serialize-result";
    /// Name of the job variable that holds the flow input or usecase request type; an execution listener on the
    /// service task sets it as a local variable.
    public static final String FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE = "type";
}
