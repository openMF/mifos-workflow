/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.transport.rest.core;

import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_BASE;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_PREFIX;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MifosFlowInfrastructureTransportRestConstants {
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MESSAGE_BASE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_BASE + "/billing/messages";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MESSAGE_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_PREFIX + ".billing";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MESSAGE_ERROR_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MESSAGE_PREFIX + ".error";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_MIME_TYPE_1_0 =
            "application/vnd.mifos.workflow+json;charset=UTF-8;version=1.0";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE = "/workflows";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_COMPLETE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/complete";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_DELETE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/delete";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_DEPLOY =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/deploy";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_HISTORY =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/history";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_REPLAY =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/replay";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_SIGNAL =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/signal";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_START =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/start";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_TASK_PENDING =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/tasks/pending";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_TERMINATE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ROUTE_BASE + "/terminate";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_TAG = "workflows";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_PACKAGE_BASE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_PREFIX + ".transport.rest";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_CORE_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_PACKAGE_BASE + ".implementation";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX + ".transport.rest";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_PROPERTIES_ENABLED =
            MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_PROPERTIES_PREFIX + ".enabled";
    public static final int MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ERROR_CODE_START =
            MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START + 100;
    public static final int MIFOS_WORKFLOW_INFRASTRUCTURE_TRANSPORT_REST_ERROR_CODE_INCREMENT =
            MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT;
}
