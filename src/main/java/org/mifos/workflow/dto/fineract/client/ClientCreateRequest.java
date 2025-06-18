package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

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
    private Long legalFormId;

    // Additional fields for complete client information
    private String externalId;
    private String mobileNo;
    private LocalDate dateOfBirth;

    private Long clientClassificationId;

    // Address information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvinceId;
    private String countryId;
    private String postalCode;

    // Family members information
    private String familyMembers;
}