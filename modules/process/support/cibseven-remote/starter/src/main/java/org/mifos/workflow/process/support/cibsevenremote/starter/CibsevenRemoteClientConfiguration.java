///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.starter;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import java.util.Objects;
import org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportProperties;
import org.mifos.workflow.process.support.cibsevenremote.core.client.CibsevenRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/// Creates the REST client from the Boot `RestClient.Builder`, so the application's HTTP and JSON settings apply.
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "CIBSEVEN_REMOTE")
class CibsevenRemoteClientConfiguration {
    @Bean
    CibsevenRemoteClient cibsevenRemoteClient(RestClient.Builder builder, CibsevenRemoteSupportProperties properties) {
        var restClient = builder.clone().baseUrl(properties.baseUrl());
        var username = properties.username();
        if (username != null) {
            var password = Objects.requireNonNullElse(properties.password(), "");
            restClient.defaultHeaders(headers -> headers.setBasicAuth(username, password));
        }
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient.build()))
                .build()
                .createClient(CibsevenRemoteClient.class);
    }
}
