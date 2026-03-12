package com.checkout.paymentgateway.model.response;

import com.checkout.paymentgateway.model.entity.Payment;
import com.checkout.paymentgateway.model.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PaymentStatus status,

        @JsonProperty("last_four_card_digits")
        String lastFourCardDigits,

        @JsonProperty("expiry_month")
        int expiryMonth,

        @JsonProperty("expiry_year")
        int expiryYear,

        String currency,
        int amount) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.id(),
                payment.status(),
                payment.lastFourCardDigits(),
                payment.expiryMonth(),
                payment.expiryYear(),
                payment.currency(),
                payment.amount());
    }
}
