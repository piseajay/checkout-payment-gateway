package com.checkout.paymentgateway.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankPaymentResponse(
    boolean authorized, @JsonProperty("authorization_code") String authorizationCode) {}
