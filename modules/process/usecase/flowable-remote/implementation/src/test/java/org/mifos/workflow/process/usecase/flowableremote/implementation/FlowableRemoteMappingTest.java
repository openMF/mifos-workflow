///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;

class FlowableRemoteMappingTest {
    @Test
    void parsesRestDatesIntoLocalTime() {
        var parsed = FlowableRemoteMapping.parse("2026-11-22T10:15:30.123+0200");

        assertThat(parsed)
                .isEqualTo(Instant.parse("2026-11-22T08:15:30.123Z")
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
    }

    @Test
    void parsesNullAsNull() {
        assertThat(FlowableRemoteMapping.parse(null)).isNull();
    }

    @Test
    void formatsDatesSoTheyParseBack() {
        var date = Date.from(Instant.parse("2026-11-22T08:15:30.123Z"));

        assertThat(FlowableRemoteMapping.parse(FlowableRemoteMapping.format(date)))
                .isEqualTo(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Test
    void parsesIsoOffsetsWithColonAndZulu() {
        var expected = Instant.parse("2026-11-22T08:15:30Z")
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        assertThat(FlowableRemoteMapping.parse("2026-11-22T10:15:30+02:00")).isEqualTo(expected);
        assertThat(FlowableRemoteMapping.parse("2026-11-22T08:15:30.000Z")).isEqualTo(expected);
    }

    @Test
    void looksUpProcessDefinitionKeyWhenMissing() {
        var instance = new FlowableRemoteClient.HistoricProcessInstance(
                "p1", "8f0c7a6e-e62f-11f1-b3bf-de8e7ad14fcf", null, null, null, null, null, null);

        assertThat(FlowableRemoteMapping.processDefinitionKey(
                        instance, id -> id.equals(instance.processDefinitionId()) ? "loan-approval" : null))
                .isEqualTo("loan-approval");
    }

    @Test
    void keepsProcessDefinitionKeyFromResponse() {
        var instance = new FlowableRemoteClient.HistoricProcessInstance(
                "p1", "8f0c7a6e-e62f-11f1-b3bf-de8e7ad14fcf", "loan-approval", null, null, null, null, null);

        assertThat(FlowableRemoteMapping.processDefinitionKey(instance, id -> {
                    throw new AssertionError("no lookup expected");
                }))
                .isEqualTo("loan-approval");
    }
}
