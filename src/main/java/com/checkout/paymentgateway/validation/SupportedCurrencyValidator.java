package com.checkout.paymentgateway.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class SupportedCurrencyValidator implements ConstraintValidator<SupportedCurrency, String> {

  private static final Set<String> CURRENCIES = Set.of("USD", "GBP", "EUR");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return CURRENCIES.contains(value.toUpperCase());
  }
}
