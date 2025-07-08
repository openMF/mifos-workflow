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
    private Authentication authentication = new Authentication();
    private Process process = new Process();

    @Data
    public static class Engine {
        private String type = "FLOWABLE";
        private Flowable flowable = new Flowable();
    }

    @Data
    public static class Flowable {
        private boolean asyncExecutorEnabled = true;
        private boolean databaseSchemaUpdate = true;
        private boolean historyEnabled = true;
        private String databaseType = "mysql";
        private String databaseUrl = "jdbc:mysql://localhost:3306/flowable";
        private String databaseUsername = "flowable";
        private String databasePassword = "flowable";
    }

    @Data
    public static class Fineract {
        private String baseUrl = "http://localhost:8443/fineract-provider/api/v1/";
        private String username = "mifos";
        private String password = "password";
        private String tenantId = "default";
        private boolean testEnabled = false;
        private int connectionTimeout = 30000;
        private int readTimeout = 30000;
    }

    @Data
    public static class Authentication {
        private boolean enabled = true;
        private String authKeyHeader = "Authorization";
        private String authKeyPrefix = "Basic ";
        private int tokenRefreshInterval = 3600;
        private boolean autoRefresh = true;
    }

    @Data
    public static class Process {
        private String defaultAssignee = "system";
        private boolean autoDeploy = true;
        private String processLocation = "classpath:processes/";
        private boolean enableProcessHistory = true;
        private int maxProcessInstances = 1000;
        private int processTimeout = 86400;
    }
}