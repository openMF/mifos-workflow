package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.address.AddressDTO;
import org.mifos.fineract.client.models.PostClientsResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.util.ProcessVariableUtil;

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
public class ClientCreationDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(ClientCreationDelegate.class);
    private static final Long DEFAULT_ADDRESS_TYPE_ID = 1L;
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_LOCALE = "en";
    
    private final FineractClientService fineractClientService;

    @Autowired
    public ClientCreationDelegate(FineractClientService fineractClientService) {
        this.fineractClientService = fineractClientService;
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
            LocalDate dateOfBirth = ProcessVariableUtil.getLocalDate(dateOfBirthObj);
            String dateFormat = (String) execution.getVariable("dateFormat");
            String locale = (String) execution.getVariable("locale");
            Boolean active = (Boolean) execution.getVariable("active");
            String addressJson = (String) execution.getVariable("addressJson");
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
            ClientCreateRequestDTO clientRequest = ClientCreateRequestDTO.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .mobileNo(mobileNo)
                    .officeId(officeId)
                    .legalFormId(legalFormId)
                    .externalId(externalId)
                    .dateOfBirth(dateOfBirth)
                    .active(active != null ? active : false)
                    .dateFormat(dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT)
                    .locale(locale != null ? locale : DEFAULT_LOCALE)
                    .address(addresses)
                    .submissionDate(LocalDate.now())
                    .build();
            PostClientsResponse response = fineractClientService.createClient(clientRequest, clientRequest.getDateFormat(), clientRequest.getLocale(), DEFAULT_ADDRESS_TYPE_ID).blockingFirst();
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
            throw new WorkflowException("Client creation failed", e, "client creation", WorkflowException.ERROR_CLIENT_CREATION_FAILED);
        }
    }
} 