package org.mifos.workflow.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProcessVariableUtilTest {

    @Test
    void getLocalDate_NullValue_ReturnsNull() {
        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(null);

        // Then
        assertNull(result);
    }

    @Test
    void getLocalDate_LocalDateValue_ReturnsSameValue() {
        // Given
        LocalDate expectedDate = LocalDate.of(2024, 1, 15);

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(expectedDate);

        // Then
        assertEquals(expectedDate, result);
    }

    @Test
    void getLocalDate_ValidStringValue_ReturnsParsedDate() {
        // Given
        String dateString = "2024-01-15";

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(dateString);

        // Then
        assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    void getLocalDate_StringWithWhitespace_ReturnsParsedDate() {
        // Given
        String dateString = "  2024-01-15  ";

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(dateString);

        // Then
        assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    void getLocalDate_EmptyString_ReturnsNull() {
        // Given
        String dateString = "";

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(dateString);

        // Then
        assertNull(result);
    }

    @Test
    void getLocalDate_WhitespaceOnlyString_ReturnsNull() {
        // Given
        String dateString = "   ";

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(dateString);

        // Then
        assertNull(result);
    }

    @Test
    void getLocalDate_InvalidStringFormat_ReturnsNull() {
        // Given
        String dateString = "2024/01/15";

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(dateString);

        // Then
        assertNull(result);
    }

    @Test
    void getLocalDate_NonStringNonLocalDateValue_ReturnsNull() {
        // Given
        Integer intValue = 123;

        // When
        LocalDate result = ProcessVariableUtil.getLocalDate(intValue);

        // Then
        assertNull(result);
    }

    @Test
    void getLong_NullValue_ReturnsDefaultValue() {
        // Given
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(null, defaultValue);

        // Then
        assertEquals(defaultValue, result);
    }

    @Test
    void getLong_LongValue_ReturnsSameValue() {
        // Given
        Long longValue = 123L;
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(longValue, defaultValue);

        // Then
        assertEquals(longValue, result);
    }

    @Test
    void getLong_IntegerValue_ReturnsLongValue() {
        // Given
        Integer intValue = 123;
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(intValue, defaultValue);

        // Then
        assertEquals(123L, result);
    }

    @Test
    void getLong_ValidStringValue_ReturnsParsedLong() {
        // Given
        String stringValue = "123";
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(stringValue, defaultValue);

        // Then
        assertEquals(123L, result);
    }

    @Test
    void getLong_StringWithWhitespace_ReturnsParsedLong() {
        // Given
        String stringValue = "  123  ";
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(stringValue, defaultValue);

        // Then
        assertEquals(123L, result);
    }

    @Test
    void getLong_InvalidStringFormat_ReturnsDefaultValue() {
        // Given
        String stringValue = "abc";
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(stringValue, defaultValue);

        // Then
        assertEquals(defaultValue, result);
    }

    @Test
    void getLong_EmptyString_ReturnsDefaultValue() {
        // Given
        String stringValue = "";
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(stringValue, defaultValue);

        // Then
        assertEquals(defaultValue, result);
    }

    @Test
    void getLong_WhitespaceOnlyString_ReturnsDefaultValue() {
        // Given
        String stringValue = "   ";
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(stringValue, defaultValue);

        // Then
        assertEquals(defaultValue, result);
    }

    @Test
    void getLong_OtherNumberTypes_ReturnsLongValue() {
        // Given
        Long defaultValue = 100L;

        // When & Then
        assertEquals(123L, ProcessVariableUtil.getLong((short) 123, defaultValue));
        assertEquals(123L, ProcessVariableUtil.getLong((byte) 123, defaultValue));
        assertEquals(123L, ProcessVariableUtil.getLong(123.0f, defaultValue));
        assertEquals(123L, ProcessVariableUtil.getLong(123.0, defaultValue));
    }

    @Test
    void getLong_NonNumericValue_ReturnsDefaultValue() {
        // Given
        Object nonNumericValue = "not a number";
        Long defaultValue = 100L;

        // When
        Long result = ProcessVariableUtil.getLong(nonNumericValue, defaultValue);

        // Then
        assertEquals(defaultValue, result);
    }

    @Test
    void getLong_NullDefaultValue_ReturnsNull() {
        // Given
        Long longValue = 123L;

        // When
        Long result = ProcessVariableUtil.getLong(longValue, null);

        // Then
        assertEquals(longValue, result);
    }

    @Test
    void getLong_NullValueWithNullDefault_ReturnsNull() {
        // When
        Long result = ProcessVariableUtil.getLong(null, null);

        // Then
        assertNull(result);
    }

    @Test
    void getLong_InvalidStringWithNullDefault_ReturnsNull() {
        // Given
        String stringValue = "abc";

        // When
        Long result = ProcessVariableUtil.getLong(stringValue, null);

        // Then
        assertNull(result);
    }
}