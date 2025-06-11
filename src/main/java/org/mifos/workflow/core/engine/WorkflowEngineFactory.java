package org.mifos.workflow.core.engine;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.mifos.workflow.config.WorkflowProperties;
import org.mifos.workflow.engine.flowable.FlowableWorkflowEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Factory class for creating instances of WorkflowEngine based on configuration.
 * This implements the factory pattern to support dynamic selection of workflow engines.
 */
@Component
public class WorkflowEngineFactory {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineFactory.class);

    private final WorkflowProperties properties;

    @Getter
    private WorkflowEngine workflowEngine;

    @Autowired
    public WorkflowEngineFactory(WorkflowProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    private void init() {
        String engineType = properties.getEngine().getType().toLowerCase();
        logger.info("Initializing workflow engine of type: {}", engineType);
        switch (engineType) {
            case "flowable":
                this.workflowEngine = new FlowableWorkflowEngine(properties);
                break;
            case "temporal":
                logger.warn("Temporal engine not yet implemented, falling back to Flowable");
                break;
            default:
                logger.error("Unsupported workflow engine type: {}", engineType);
                throw new IllegalArgumentException("Unsupported workflow engine type: " + engineType);
        }
    }

}