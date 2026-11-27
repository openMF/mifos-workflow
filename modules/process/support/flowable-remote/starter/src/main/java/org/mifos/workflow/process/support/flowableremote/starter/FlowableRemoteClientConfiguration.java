///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.starter;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE;

import org.apache.commons.lang3.StringUtils;
import org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportProperties;
import org.mifos.workflow.process.support.flowableremote.core.client.FlowableRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/// Creates the REST client from the Boot `RestClient.Builder`, so the application's HTTP and JSON settings apply.
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = MIFOS_WORKFLOW_PROCESS_PROPERTIES_ENGINE, havingValue = "FLOWABLE_REMOTE")
class FlowableRemoteClientConfiguration {
    @Bean
    FlowableRemoteClient flowableRemoteClient(RestClient.Builder builder, FlowableRemoteSupportProperties properties) {
        var restClientBuilder = builder.clone().baseUrl(properties.baseUrl());

        if (StringUtils.isNotBlank(properties.username()) && StringUtils.isNotBlank(properties.password())) {
            restClientBuilder.defaultHeaders(
                    headers -> headers.setBasicAuth(properties.username(), properties.password()));
        }

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClientBuilder.build()))
                .build()
                .createClient(FlowableRemoteClient.class);
    }
}
