package org.mifos.workflow.dto.fineract.office;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Represents an office data transfer object in the Fineract system.
 * This class encapsulates the properties of an office, including its ID, name, external ID, opening date, and hierarchy.
 */
@Data
public class OfficeDTO {
    private Long id;
    private String name;
    @SerializedName("nameDecorated")
    private String nameDecorated;
    @SerializedName("externalId")
    private String externalId;
    @SerializedName("openingDate")
    private int[] openingDate;
    private String hierarchy;
} 