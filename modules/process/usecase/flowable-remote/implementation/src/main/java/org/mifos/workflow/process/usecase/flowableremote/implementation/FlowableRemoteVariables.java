///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient.Variable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/// Converts Java values to the typed variables of the REST API.
///
/// Strings, booleans, numbers and dates keep their engine type, so the process sees the same variables as with the
/// embedded engine. Other values (maps, lists, objects) are sent as `json` variables.
@Component
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
class FlowableRemoteVariables {
    List<Variable> toTyped(@Nullable Map<String, ?> variables) {
        var typed = new ArrayList<Variable>();
        if (variables != null) {
            variables.forEach((name, value) -> typed.add(toTyped(name, value)));
        }
        return typed;
    }

    Variable toTyped(String name, @Nullable Object value) {
        return switch (value) {
            case null -> new Variable(name, null, null);
            case String s -> new Variable(name, "string", s);
            case Boolean b -> new Variable(name, "boolean", b);
            case Integer i -> new Variable(name, "integer", i);
            case Long l -> new Variable(name, "long", l);
            case Short s -> new Variable(name, "short", s);
            case Double d -> new Variable(name, "double", d);
            case Float f -> new Variable(name, "double", f.doubleValue());
            case Date d -> new Variable(name, "date", FlowableRemoteMapping.format(d));
            default -> new Variable(name, "json", value);
        };
    }
}
