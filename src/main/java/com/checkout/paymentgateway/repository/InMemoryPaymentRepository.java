package com.checkout.paymentgateway.repository;

import com.checkout.paymentgateway.model.entity.Payment;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

  private final Map<UUID, Payment> store = new ConcurrentHashMap<>();

  @Override
  public void save(Payment payment) {
    store.put(payment.id(), payment);
  }

  @Override
  public Optional<Payment> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }
}
