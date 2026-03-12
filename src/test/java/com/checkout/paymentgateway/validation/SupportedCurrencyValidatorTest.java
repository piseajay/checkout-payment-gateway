package com.checkout.paymentgateway.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SupportedCurrencyValidatorTest {

  private final SupportedCurrencyValidator validator = new SupportedCurrencyValidator();

  @ParameterizedTest
  @ValueSource(strings = {"USD", "GBP", "EUR"})
  void isValid_supportedCurrency_returnsTrue(String currency) {
    boolean result = validator.isValid(currency, null);

    assertThat(result).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "INR", "AUD", "NZD", "CAD"})
  void isValid_unsupportedCurrency_returnsFalse(String currency) {
    boolean result = validator.isValid(currency, null);

    assertThat(result).isFalse();
  }

  @Test
  void isValid_nullCurrency_returnsTrue() {
    boolean result = validator.isValid(null, null);

    assertThat(result).isTrue();
  }

  @Test
  void isValid_emptyCurrency_returnsFalse() {
    boolean result = validator.isValid("", null);

    assertThat(result).isFalse();
  }

  @Test
  void isValid_mixedCase_returnsTrue() {
    boolean result = validator.isValid("GbP", null);

    assertThat(result).isTrue();
  }
}
