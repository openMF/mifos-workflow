package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import org.mifos.workflow.dto.fineract.address.AddressDTO;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a request to create a new client in the Fineract system.
 * Contains necessary details such as name, office ID, date format, locale, and active status.
 */
@Data
@Builder
public class ClientCreateRequestDTO {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private Long officeId;
    @NotNull
    private String dateFormat;
    @NotNull
    private String locale;
    @NotNull
    private Boolean active;
    @NotNull
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

    // Address DTOs
    private List<AddressDTO> address;

    // Family members information
    private String familyMembers;

    // Submission date
    private LocalDate submissionDate;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("firstname", firstName);
        map.put("lastname", lastName);
        map.put("officeId", officeId);
        map.put("dateFormat", dateFormat);
        map.put("locale", locale);
        map.put("active", active);
        map.put("legalFormId", legalFormId);
        map.put("externalId", externalId);
        map.put("mobileNo", mobileNo);
        if (dateOfBirth != null) {
            String format = dateFormat != null ? dateFormat : "dd MMMM yyyy";
            map.put("dateOfBirth", dateOfBirth.format(DateTimeFormatter.ofPattern(format)));
        }
        if (submissionDate != null) {
            String format = dateFormat != null ? dateFormat : "dd MMMM yyyy";
            map.put("submittedOnDate", submissionDate.format(DateTimeFormatter.ofPattern(format)));
        }
        map.put("clientClassificationId", clientClassificationId);
        map.put("familyMembers", familyMembers);

        if (address != null && !address.isEmpty()) {
            map.put("address", address.stream()
                    .map(AddressDTO::toMap)
                    .collect(Collectors.toList()));
        } else if (addressLine1 != null || addressLine2 != null || city != null) {
            AddressDTO address = AddressDTO.builder()
                    .addressLine1(addressLine1)
                    .addressLine2(addressLine2)
                    .city(city)
                    .stateProvinceId(stateProvinceId != null ? Long.parseLong(stateProvinceId) : null)
                    .countryId(countryId != null ? Long.parseLong(countryId) : null)
                    .postalCode(postalCode)
                    .build();
            map.put("address", List.of(address.toMap()));
        }

        return map;
    }
}