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
public class WorkflowConfig {

    private Engine engine = new Engine();
    private Fineract fineract = new Fineract();

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

    @Data
    public static class Fineract {
        private String baseUrl = "http://localhost:8443/fineract-provider/api/v1/";
        private String username = "mifos";
        private String password = "password";
        private String tenantId = "default";
    }

}