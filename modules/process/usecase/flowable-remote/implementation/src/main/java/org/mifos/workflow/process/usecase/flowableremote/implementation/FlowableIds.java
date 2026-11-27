///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.experimental.UtilityClass;

/// The Flowable REST app can use non-UUID identifiers, so ids that are not already UUIDs are converted
/// deterministically rather than rejected. Same rules as the CIB seven modules.
@UtilityClass
class FlowableIds {
    static UUID toUuid(String id) {
        if (id == null) {
            return null;
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException _) {
            return UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
        }
    }
}
