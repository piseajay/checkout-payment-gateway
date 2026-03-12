package com.checkout.paymentgateway.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SupportedCurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedCurrency {

  String message() default "Currency must be one of: USD, GBP, EUR";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
