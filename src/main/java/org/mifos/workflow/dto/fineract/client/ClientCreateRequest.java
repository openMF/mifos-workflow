package org.mifos.workflow.dto.fineract.client;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a request to create a new client in the Fineract system.
 * Contains necessary details such as name, office ID, date format, locale, and active status.
 */
@Data
@Builder
public class ClientCreateRequest {
    private String firstName;
    private String lastName;
    private Long officeId;
    private String dateFormat;
    private String locale;
    private boolean active;


}