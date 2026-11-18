///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.cibsevenremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

class CibsevenRemoteMappingTest {
    @Test
    void parsesRestDatesIntoLocalTime() {
        var parsed = CibsevenRemoteMapping.parse("2026-11-22T10:15:30.123+0200");

        assertThat(parsed)
                .isEqualTo(Instant.parse("2026-11-22T08:15:30.123Z")
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
    }

    @Test
    void parsesNullAsNull() {
        assertThat(CibsevenRemoteMapping.parse(null)).isNull();
    }

    @Test
    void formatsDatesSoTheyParseBack() {
        var date = Date.from(Instant.parse("2026-11-22T08:15:30.123Z"));

        assertThat(CibsevenRemoteMapping.parse(CibsevenRemoteMapping.format(date)))
                .isEqualTo(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }
}
