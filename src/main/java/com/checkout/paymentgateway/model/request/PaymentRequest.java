package com.checkout.paymentgateway.model.request;

import com.checkout.paymentgateway.validation.ValidCardExpiry;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@ValidCardExpiry
public record PaymentRequest(

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{14,19}", message = "Card number must be 14-19 numeric digits")
        @JsonProperty("card_number")
        String cardNumber,

        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        @JsonProperty("expiry_month")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @JsonProperty("expiry_year")
        Integer expiryYear,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "USD|GBP|EUR", message = "Currency must be one of: USD, GBP, EUR")
        String currency,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be a positive integer representing the minor currency unit (e.g. 1050 for $10.50)")
        Integer amount,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 numeric digits")
        String cvv
) {
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "cardNumber='****" + lastFour() +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                '}';
    }

    public String lastFour() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
