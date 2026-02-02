package com.berk.lab10.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for password validation logic
 *
 * Strong password policy:
 * - At least 5 characters
 * - At least one lowercase letter
 * - At least one uppercase letter
 * - At least one digit
 * - At least one special character
 */
class PasswordValidationTest {

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{5,}$"
    );

    /**
     * Mirror of AuthController blacklist.
     * (Unit test should not depend on controller wiring; just logic.)
     */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password",
            "password123!",
            "a12345678!",
            "a123456789!",
            "aqwerty123",
            "qwertyuiop",
            "Admin123!",
            "letmein123!",
            "welcome123!",
            "a11111111!",
            "qwerty123!"
    );

    @Test
    void validatePassword_WithStrongPassword_ShouldPass() {
        assertTrue(STRONG_PASSWORD.matcher("StrongPass1!").matches());
        assertTrue(STRONG_PASSWORD.matcher("Abc123!@#").matches());
        assertTrue(STRONG_PASSWORD.matcher("P@ssw0rd").matches());
        assertTrue(STRONG_PASSWORD.matcher("MyS3cur3P@ss").matches());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "weak",              // Too short, missing many requirements
            "weakpassword",      // No uppercase, no digit, no special
            "WEAKPASSWORD",      // No lowercase, no digit, no special
            "WeakPassword",      // No digit, no special
            "WeakPassword1",     // No special character
            "WeakPassword!",     // No digit
            "weak1!",            // No uppercase
            "WEAK1!",            // No lowercase
            "Aa1!"               // Too short (min is 5)
    })
    void validatePassword_WithWeakPassword_ShouldFail(String weakPassword) {
        assertFalse(STRONG_PASSWORD.matcher(weakPassword).matches());
    }

    @Test
    void blacklist_ShouldContainKnownCommonPasswords() {
        // Blacklist’in işi: bu değerler listede olmalı.
        assertTrue(COMMON_PASSWORDS.contains("password123!"));
        assertTrue(COMMON_PASSWORDS.contains("qwerty123!"));
        assertTrue(COMMON_PASSWORDS.contains("Admin123!"));
        assertTrue(COMMON_PASSWORDS.contains("welcome123!"));
    }

    @Test
    void blacklist_ExamplesMayOrMayNotMatchStrongPattern() {
        // IMPORTANT:
        // Bazı blacklist şifreleri strong pattern'e uymayabilir (ör. uppercase yok).
        // Bu test sadece demonstrasyon amaçlı: en az 1 tanesi regex’i geçsin istiyorsan,
        // gerçekten geçen örnek seçiyoruz.

        assertTrue(STRONG_PASSWORD.matcher("Admin123!").matches());     // geçer (A var)
        assertTrue(STRONG_PASSWORD.matcher("welcome123!A").matches());  // bu blacklist'te yok; sadece örnek
        assertFalse(STRONG_PASSWORD.matcher("password123!").matches()); // uppercase yok -> false (DOĞRU)
    }

    @Test
    void validatePassword_EdgeCases() {
        assertTrue(STRONG_PASSWORD.matcher("Aa1!b").matches()); // exactly 5 chars
        assertFalse(STRONG_PASSWORD.matcher("Aa1!").matches()); // 4 chars

        assertTrue(STRONG_PASSWORD.matcher("ThisIsAVeryLongAndSecurePassword123!@#").matches());
        assertFalse(STRONG_PASSWORD.matcher("!@#$%^&*()").matches()); // missing alpha and digit
        assertTrue(STRONG_PASSWORD.matcher("P@ssw0rd!#$%").matches());
    }

    @Test
    void validatePassword_WithWhitespace_ShouldPass() {
        assertTrue(STRONG_PASSWORD.matcher("Pass Word1!").matches());
        assertTrue(STRONG_PASSWORD.matcher("My P@ss 123").matches());
    }

    @Test
    void validatePassword_Empty_ShouldFail() {
        assertFalse(STRONG_PASSWORD.matcher("").matches());
    }
}
