package com.checkout.paymentgateway.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankPaymentRequest(
        @JsonProperty("card_number")
        String cardNumber,

        @JsonProperty("expiry_date")
        String expiryDate,

        String currency,
        int amount,
        String cvv) {

    @Override
    public String toString() {
        return "BankPaymentRequest{" +
                "cardNumber='****" + lastFour() +
                ", expiryDate='" + expiryDate + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                '}';
    }

    public String lastFour() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
