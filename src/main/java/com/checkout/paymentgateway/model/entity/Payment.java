package com.checkout.paymentgateway.model.entity;

import java.util.UUID;

public record Payment(
    UUID id,
    PaymentStatus status,
    String lastFourCardDigits,
    int expiryMonth,
    int expiryYear,
    String currency,
    int amount) {}
