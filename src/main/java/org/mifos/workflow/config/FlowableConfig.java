package org.mifos.workflow.config;

import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Flowable workflow engine integration with Spring.
 * This ensures that delegate classes can use Spring dependency injection.
 */
@Configuration
public class FlowableConfig {

    @Autowired
    private WorkflowConfig workflowConfig;


    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> engineConfigurationConfigurer() {
        return engineConfiguration -> {


            if (workflowConfig.getEngine().getFlowable().isDatabaseSchemaUpdate()) {
                engineConfiguration.setDatabaseSchemaUpdate("true");
            }


            if (workflowConfig.getEngine().getFlowable().isHistoryEnabled()) {
                engineConfiguration.setHistory("full");
            }
        };
    }
} 