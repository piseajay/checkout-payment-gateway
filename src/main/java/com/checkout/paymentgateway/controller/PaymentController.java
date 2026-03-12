package com.checkout.paymentgateway.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkout.paymentgateway.model.request.PaymentRequest;
import com.checkout.paymentgateway.model.response.PaymentResponse;
import com.checkout.paymentgateway.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping
  public ResponseEntity<PaymentResponse> payment(
      @Valid @RequestBody PaymentRequest paymentRequest) {

    PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
  }

  @GetMapping("/{paymentId}")
  public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
    return ResponseEntity.status(HttpStatus.OK).body(paymentService.getPayment(paymentId));
  }
}
