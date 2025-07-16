package org.mifos.workflow.util;

import java.time.LocalDate;

public class ProcessVariableUtil {
    private ProcessVariableUtil() {}

    public static LocalDate getLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (!str.isEmpty()) {
                try {
                    return LocalDate.parse(str);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static Long getLong(Object value, Long defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong(((String) value).trim());
            } catch (NumberFormatException ignored) {}
        }
        if (value instanceof Number) return ((Number) value).longValue();
        return defaultValue;
    }
} 