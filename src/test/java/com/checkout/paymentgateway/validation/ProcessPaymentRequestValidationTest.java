package com.checkout.paymentgateway.validation;

import java.time.Year;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.checkout.paymentgateway.model.request.PaymentRequest;

import jakarta.validation.ConstraintViolation;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;

import jakarta.validation.Validator;

class ProcessPaymentRequestValidationTest {

    private static Validator validator;
    private static final int FUTURE_YEAR = Year.now().getValue() + 2;

    @BeforeAll
    static void setUp() {
        validator = buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_producesNoViolations() {
        PaymentRequest request = validRequest();
        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901234", "1234567890123456789"})
    void cardNumber_atBoundaries_isValid(String cardNumber) {
        PaymentRequest request = new PaymentRequest(cardNumber, 12, FUTURE_YEAR, "GBP", 100, "123");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void cardNumber_tooShort_isRejected() {
        PaymentRequest request =
                new PaymentRequest("1234567890123", 12, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolation(request, "Card number must be 14-19 numeric digits");
    }

    @Test
    void cardNumber_tooLong_isRejected() {
        PaymentRequest request =
                new PaymentRequest("12345678901234567890", 12, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolation(request, "Card number must be 14-19 numeric digits");
    }

    @Test
    void cardNumber_containsLetters_isRejected() {
        PaymentRequest request =
                new PaymentRequest("4111111111111A1B", 12, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolation(request, "Card number must be 14-19 numeric digits");
    }

    @Test
    void cardNumber_null_isRejected() {
        PaymentRequest request = new PaymentRequest(null, 12, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolationContaining(request, "Card number");
    }

    @Test
    void expiryMonth_zero_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 0, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolationContaining(request, "Expiry month");
    }

    @Test
    void expiryMonth_thirteen_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 13, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolationContaining(request, "Expiry month");
    }

    @Test
    void expiryMonth_null_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", null, FUTURE_YEAR, "GBP", 100, "123");
        assertHasViolationContaining(request, "Expiry month");
    }

    // --- Expiry Date (combined) ---

    @Test
    void expiry_inThePast_isRejected() {
        int pastYear = Year.now().getValue() - 1;
        PaymentRequest request = new PaymentRequest("2222405343248877", 1, pastYear, "GBP", 100, "123");
        assertHasViolation(request, "Card expiry date must be in the future");
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "GBP", "EUR"})
    void currency_supported_isValid(String currency) {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, currency, 100, "123");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void currency_unsupported_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "JPY", 100, "123");
        assertHasViolation(request, "Currency must be one of: USD, GBP, EUR");
    }

    @Test
    void currency_wrongLength_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "US", 100, "123");
        assertHasViolationContaining(request, "Currency must be one of: USD, GBP, EUR");
    }

    @Test
    void currency_null_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, null, 100, "123");
        assertHasViolationContaining(request, "Currency is required");
    }

    // --- Amount ---

    @Test
    void amount_zero_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 0, "123");
        assertHasViolation(request, "Amount must be a positive integer representing the minor currency unit (e.g. 1050 for $10.50)");
    }

    @Test
    void amount_negative_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", -1, "123");
        assertHasViolation(request, "Amount must be a positive integer representing the minor currency unit (e.g. 1050 for $10.50)");
    }

    @Test
    void amount_null_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", null, "123");
        assertHasViolationContaining(request, "Amount is required");
    }

    // --- CVV ---

    @ParameterizedTest
    @ValueSource(strings = {"123", "1234"})
    void cvv_threeOrFourDigits_isValid(String cvv) {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 100, cvv);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void cvv_twoDigits_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 100, "12");
        assertHasViolation(request, "CVV must be 3 or 4 numeric digits");
    }

    @Test
    void cvv_containsLetters_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 100, "12X");
        assertHasViolation(request, "CVV must be 3 or 4 numeric digits");
    }

    @Test
    void cvv_null_isRejected() {
        PaymentRequest request =
                new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 100, null);
        assertHasViolationContaining(request, "CVV is required");
    }

    @Test
    void toString_doesNotLeakCardNumberOrCvv() {
        PaymentRequest request = validRequest();
        String representation = request.toString();
        assertThat(representation).doesNotContain("2222405343248877");
        assertThat(representation).doesNotContain("123");
        assertThat(representation).contains("****8877");
    }

    private PaymentRequest validRequest() {
        return new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 100, "123");
    }

    private void assertHasViolation(PaymentRequest request, String expectedMessage) {
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations).extracting(ConstraintViolation::getMessage).contains(expectedMessage);
    }

    private void assertHasViolationContaining(PaymentRequest request, String fragment) {
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(msg -> msg.contains(fragment));
    }
}
