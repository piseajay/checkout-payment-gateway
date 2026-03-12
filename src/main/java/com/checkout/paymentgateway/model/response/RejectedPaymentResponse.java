package com.checkout.paymentgateway.model.response;

import java.util.List;

public record RejectedPaymentResponse(String status, List<String> errors) {}
