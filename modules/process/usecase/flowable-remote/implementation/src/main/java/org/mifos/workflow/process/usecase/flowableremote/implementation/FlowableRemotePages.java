///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.usecase.flowableremote.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.experimental.UtilityClass;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;

/// Reads all pages of a query, so the usecases return complete lists as with the embedded engine.
@UtilityClass
class FlowableRemotePages {
    static final int PAGE_SIZE = 500;

    static <T> List<T> all(BiFunction<Integer, Integer, FlowableRemoteClient.Page<T>> query) {
        var all = new ArrayList<T>();
        var start = 0;
        while (true) {
            var page = query.apply(start, PAGE_SIZE);
            all.addAll(page.data());
            start += page.data().size();
            if (page.data().isEmpty() || start >= page.total()) {
                return all;
            }
        }
    }
}
