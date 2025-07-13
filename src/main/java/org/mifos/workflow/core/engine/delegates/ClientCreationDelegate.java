package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.address.AddressDTO;
import org.mifos.fineract.client.models.PostClientsResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Delegate for creating a new client in the Fineract system.
 * Creates an inactive client that will be activated later in the workflow.
 */
@Component
public class ClientCreationDelegate implements JavaDelegate, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ClientCreationDelegate.class);
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ClientCreationDelegate.applicationContext = applicationContext;
    }

    private FineractClientService getFineractClientService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not available");
        }
        return applicationContext.getBean(FineractClientService.class);
    }

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("ClientCreationDelegate.execute() called for process instance: {}", execution.getProcessInstanceId());
        try {
            String firstName = (String) execution.getVariable("firstName");
            String lastName = (String) execution.getVariable("lastName");
            String mobileNo = (String) execution.getVariable("mobileNo");
            Long officeId = (Long) execution.getVariable("officeId");
            Long legalFormId = (Long) execution.getVariable("legalFormId");
            String externalId = (String) execution.getVariable("externalId");
            Object dateOfBirthObj = execution.getVariable("dateOfBirth");
            String dateFormat = (String) execution.getVariable("dateFormat");
            String locale = (String) execution.getVariable("locale");
            Boolean active = (Boolean) execution.getVariable("active");
            String addressJson = (String) execution.getVariable("addressJson");
            LocalDate dateOfBirth = null;
            if (dateOfBirthObj != null) {
                if (dateOfBirthObj instanceof LocalDate) {
                    dateOfBirth = (LocalDate) dateOfBirthObj;
                } else if (dateOfBirthObj instanceof String) {
                    String dateOfBirthStr = (String) dateOfBirthObj;
                    if (!dateOfBirthStr.trim().isEmpty()) {
                        try {
                            dateOfBirth = LocalDate.parse(dateOfBirthStr);
                        } catch (Exception e) {
                            logger.warn("Could not parse dateOfBirth string: {}, using null", dateOfBirthStr);
                        }
                    }
                } else {
                    logger.warn("Unexpected dateOfBirth type: {}, using null", dateOfBirthObj.getClass().getSimpleName());
                }
            }
            if (firstName == null || firstName.trim().isEmpty()) {
                throw new IllegalArgumentException("firstName is required");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                throw new IllegalArgumentException("lastName is required");
            }
            if (officeId == null) {
                throw new IllegalArgumentException("officeId is required");
            }
            if (legalFormId == null) {
                throw new IllegalArgumentException("legalFormId is required");
            }
            logger.info("Creating client: {} {} in office: {}", firstName, lastName, officeId);
            List<AddressDTO> addresses = new ArrayList<>();
            if (addressJson != null && !addressJson.trim().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    addresses = mapper.readValue(addressJson, new TypeReference<List<AddressDTO>>() {
                    });
                } catch (Exception e) {
                    logger.warn("Could not parse address JSON: {}, using empty list", addressJson);
                }
            }
            ClientCreateRequestDTO clientRequest = ClientCreateRequestDTO.builder().firstName(firstName).lastName(lastName).mobileNo(mobileNo).officeId(officeId).legalFormId(legalFormId).externalId(externalId).dateOfBirth(dateOfBirth).active(active != null ? active : false).dateFormat(dateFormat != null ? dateFormat : "yyyy-MM-dd").locale(locale != null ? locale : "en").address(addresses).submissionDate(LocalDate.now()).build();
            FineractClientService fineractClientService = getFineractClientService();
            PostClientsResponse response = fineractClientService.createClient(clientRequest, clientRequest.getDateFormat(), clientRequest.getLocale(), 1L).blockingFirst();
            if (response != null && response.getClientId() != null) {
                Long clientId = response.getClientId();
                logger.info("Successfully created client with ID: {}", clientId);
                execution.setVariable("clientId", clientId);
                execution.setVariable("clientCreated", true);
                execution.setVariable("clientStatus", "PENDING");
                execution.setVariable("creationDate", LocalDate.now());
            } else {
                throw new RuntimeException("Failed to create client: No response received from Fineract");
            }
        } catch (FineractApiException e) {
            logger.error("Fineract API error during client creation: {}", e.getMessage());
            execution.setVariable("clientCreated", false);
            execution.setVariable("clientStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating client: {}", e.getMessage(), e);
            execution.setVariable("clientCreated", false);
            execution.setVariable("clientStatus", "ERROR");
            execution.setVariable("errorMessage", e.getMessage());
            throw new WorkflowException("Client creation failed", e, "client creation", "CLIENT_CREATION_FAILED");
        }
    }
} 