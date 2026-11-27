///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import java.util.UUID;
import lombok.experimental.UtilityClass;

/// Camunda 8 identifies everything by `long` keys while the process usecases use UUIDs, so a key is carried in the
/// low bits of a UUID with zero high bits. The encoding is reversible, so ids handed out can be passed back in, and
/// string ids accept either the raw key or its UUID form.
@UtilityClass
class CamundaIds {
    static UUID toUuid(long key) {
        return new UUID(0L, key);
    }

    static String toId(long key) {
        return toUuid(key).toString();
    }

    static long toKey(UUID id) {
        if (id.getMostSignificantBits() != 0L) {
            throw new IllegalArgumentException("Not a Camunda key: " + id);
        }

        return id.getLeastSignificantBits();
    }

    static long toKey(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException _) {
            return toKey(UUID.fromString(id));
        }
    }
}
