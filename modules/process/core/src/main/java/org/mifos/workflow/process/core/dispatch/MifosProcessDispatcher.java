///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.core.dispatch;

import org.jspecify.annotations.Nullable;

/// Engine-neutral entry point for the inbound direction: a workflow engine reached an automated step and wants
/// Mifos business logic to run.
///
/// Every engine has such a step (a service task expression in CIB seven, Operaton, Eximee and Flowable, a job
/// worker in Camunda 8, a task worker in Conductor, an activity in Temporal), and all of them reduce to the same
/// three things: a type name, an input and an output. Engine adapters only translate their engine's callback into
/// a call to this interface.
///
/// The type is the class name of the input of a `MifosFlow` or of the request of a `MifosUsecase`. Because one
/// input class belongs to exactly one flow or usecase, the type is enough to find what to run; only input types of
/// registered beans are accepted.
public interface MifosProcessDispatcher {
    /// Runs the flow or usecase whose input type is `type`.
    ///
    /// @param type fully qualified class name of the input, either canonical (`a.b.Outer.Request`) or binary
    ///     (`a.b.Outer$Request`)
    /// @param input the input as JSON text, as a map of its fields, or as the input object itself; `null` creates
    ///     an empty input
    /// @return the flow output or usecase response, possibly `null`
    @Nullable Object dispatch(String type, @Nullable Object input);

    /// Serializes a value to JSON, the one representation every engine can store as a process variable.
    ///
    /// @param value the value to serialize, usually a flow output or usecase response
    /// @return the JSON text
    String toJson(Object value);
}
