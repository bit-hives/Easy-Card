package com.easycard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class CardServiceTest {

    @Test
    void testLuhnAlgorithm_ValidCardNumber() {
        assertTrue(isValidLuhn("4532015112830366"));
        assertTrue(isValidLuhn("5425233430109903"));
    }

    @Test
    void testLuhnAlgorithm_InvalidCardNumber() {
        assertFalse(isValidLuhn("4532015112830367"));
        assertFalse(isValidLuhn("1234567890123456"));
    }

    @Test
    void testLuhnAlgorithm_EmptyOrNull() {
        assertFalse(isValidLuhn(""));
        assertFalse(isValidLuhn(null));
    }

    @Test
    void testLuhnAlgorithm_NonNumeric() {
        assertFalse(isValidLuhn("453201511283036a"));
        assertFalse(isValidLuhn("abcd123456789012"));
    }

    private boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d+")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
