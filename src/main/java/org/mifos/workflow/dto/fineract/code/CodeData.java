package org.mifos.workflow.dto.fineract.code;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Represents a code data object in the Fineract system.
 * This class encapsulates the properties of a code, including its ID, name, and whether it is system-defined.
 */
@Data
public class CodeData {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("systemDefined")
    private Boolean systemDefined;
} 