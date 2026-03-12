package com.checkout.paymentgateway.repository;

import com.checkout.paymentgateway.model.entity.Payment;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for payment persistence operations. Abstracted as an interface so the implementation can be
 * swapped (e.g. in-memory for tests, database for production).
 */
public interface PaymentRepository {

  void save(Payment payment);

  Optional<Payment> findById(UUID id);
}
