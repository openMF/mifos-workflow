package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for client command requests, including transfer, reject, and close operations.
 */
@Data
@Builder
public class ClientCommandRequest {
    // Common fields
    private String dateFormat;
    private String locale;
    
    // Transfer specific fields
    private LocalDate transferDate;
    private Long destinationOfficeId;
    
    // Reject specific fields
    private LocalDate rejectionDate;
    private Long rejectionReasonId;
    
    // Close specific fields
    private LocalDate closureDate;
    private Long closureReasonId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (transferDate != null) map.put("proposedTransferDate", transferDate.toString());
        if (destinationOfficeId != null) map.put("destinationOfficeId", destinationOfficeId);
        if (rejectionDate != null) map.put("rejectionDate", rejectionDate.toString());
        if (rejectionReasonId != null) map.put("rejectionReasonId", rejectionReasonId);
        if (closureDate != null) map.put("closureDate", closureDate.toString());
        if (closureReasonId != null) map.put("closureReasonId", closureReasonId);
        return map;
    }
} 