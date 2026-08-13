/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.usecase.flowable.core;

import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FlowableFlowUsecaseConstants {
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_PACKAGE_BASE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PACKAGE_BASE + ".usecase.flowable";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_CORE_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_PACKAGE_BASE + ".core";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_MAPPING_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_PACKAGE_BASE + ".mapping";
    public static final String MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_IMPLEMENTATION_PACKAGE =
            MIFOS_WORKFLOW_INFRASTRUCTURE_USECASE_FLOWABLE_PACKAGE_BASE + ".implementation";
    public static final String FLOWABLE_WORKFLOW_PROPERTIES_PREFIX =
            MIFOS_WORKFLOW_INFRASTRUCTURE_PROPERTIES_PREFIX + ".flowable";
    public static final String FLOWABLE_WORKFLOW_PROPERTIES_ENABLED = FLOWABLE_WORKFLOW_PROPERTIES_PREFIX + ".enabled";
}
