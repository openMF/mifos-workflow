package org.mifos.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for workflow engine settings.
 * Binds properties from application.properties under the 'workflow' prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "workflow")
public class WorkflowProperties {

    private Engine engine = new Engine();

    @Data
    public static class Engine {
        private String type = "FLOWABLE";
        private Flowable flowable = new Flowable();
    }

    @Data
    public static class Flowable {
        private boolean asyncExecutorEnabled = true;
        private boolean databaseSchemaUpdate = true;
    }

}