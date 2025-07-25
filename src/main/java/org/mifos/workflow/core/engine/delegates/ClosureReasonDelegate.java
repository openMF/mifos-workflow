package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Delegate for retrieving closure reasons in the Fineract system.
 */
@Component
public class ClosureReasonDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ClosureReasonDelegate.class);
    private final FineractClientService fineractClientService;

    @Autowired
    public ClosureReasonDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        try {
            logger.info("Retrieving available closure reasons");
            var reasons = fineractClientService.retrieveClientClosureReasons().blockingFirst();
            List<Map<String, Object>> closureReasonsList = new ArrayList<>();
            for (var reason : reasons) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", reason.getId());
                map.put("name", reason.getName());
                map.put("description", reason.getDescription());
                map.put("position", reason.getPosition());
                closureReasonsList.add(map);
            }
            execution.setVariable("closureReasons", closureReasonsList);
            execution.setVariable("closureReasonsFetched", true);
        } catch (org.mifos.workflow.exception.FineractApiException e) {
            logger.error("Fineract API error during closure reason retrieval: {}", e.getMessage());
            execution.setVariable("closureReasonsFetched", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during closure reason retrieval: {}", e.getMessage(), e);
            execution.setVariable("closureReasonsFetched", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Closure reason retrieval failed", e, "closure reason retrieval", "ERROR_CLOSURE_REASON_RETRIEVAL_FAILED");
        }
    }
} 