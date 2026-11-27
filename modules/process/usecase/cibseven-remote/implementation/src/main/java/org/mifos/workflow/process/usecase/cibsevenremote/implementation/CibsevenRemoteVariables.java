///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient.VariableValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/// Converts Java values to the typed values of the REST API.
///
/// Strings, booleans, numbers and dates keep their engine type, so the process sees the same variables as with the
/// embedded engine. Other values (maps, lists, objects) are sent as `Json` variables.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
class CibsevenRemoteVariables {
    private final JsonMapper jsonMapper;

    Map<String, VariableValue> toTyped(@Nullable Map<String, ?> variables) {
        var typed = new LinkedHashMap<String, VariableValue>();
        if (variables != null) {
            variables.forEach((name, value) -> typed.put(name, toTyped(value)));
        }
        return typed;
    }

    VariableValue toTyped(@Nullable Object value) {
        return switch (value) {
            case null -> new VariableValue(null, "Null", null);
            case String s -> new VariableValue(s, "String", null);
            case Boolean b -> new VariableValue(b, "Boolean", null);
            case Integer i -> new VariableValue(i, "Integer", null);
            case Long l -> new VariableValue(l, "Long", null);
            case Short s -> new VariableValue(s, "Short", null);
            case Double d -> new VariableValue(d, "Double", null);
            case Float f -> new VariableValue(f.doubleValue(), "Double", null);
            case Date d -> new VariableValue(CibsevenRemoteMapping.format(d), "Date", null);
            default -> new VariableValue(jsonMapper.writeValueAsString(value), "Json", null);
        };
    }
}
