///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.camunda.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class CamundaIdsTest {
    @Test
    void roundTripsKeyThroughUuid() {
        var key = 2251799813685249L;

        assertThat(CamundaIds.toKey(CamundaIds.toUuid(key))).isEqualTo(key);
        assertThat(CamundaIds.toKey(CamundaIds.toId(key))).isEqualTo(key);
    }

    @Test
    void acceptsRawKeyString() {
        assertThat(CamundaIds.toKey("2251799813685249")).isEqualTo(2251799813685249L);
    }

    @Test
    void rejectsUuidThatIsNotKey() {
        var id = UUID.randomUUID();

        assertThatThrownBy(() -> CamundaIds.toKey(id)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsGarbage() {
        assertThatThrownBy(() -> CamundaIds.toKey("not-an-id")).isInstanceOf(IllegalArgumentException.class);
    }
}
