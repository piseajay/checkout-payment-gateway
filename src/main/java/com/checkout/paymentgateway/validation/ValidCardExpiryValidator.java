package com.checkout.paymentgateway.validation;

import java.time.YearMonth;

import com.checkout.paymentgateway.model.request.PaymentRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCardExpiryValidator
        implements ConstraintValidator<ValidCardExpiry, PaymentRequest> {

    @Override
    public boolean isValid(PaymentRequest request, ConstraintValidatorContext context) {

        // Return true for invalid months/years to let field validators handle them
        if (request.expiryMonth() == null || request.expiryYear() == null) {
            return true;
        }

        if (request.expiryMonth() < 1 || request.expiryMonth() > 12) {
            return true;
        }

        YearMonth expiryDate = YearMonth.of(request.expiryYear(), request.expiryMonth());

        return !expiryDate.isBefore(YearMonth.now());
    }
}
