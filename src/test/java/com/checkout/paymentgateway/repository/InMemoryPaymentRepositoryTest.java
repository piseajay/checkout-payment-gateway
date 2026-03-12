package com.checkout.paymentgateway.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.paymentgateway.constants.TestConstants;
import com.checkout.paymentgateway.model.entity.Payment;
import com.checkout.paymentgateway.model.entity.PaymentStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryPaymentRepositoryTest {

  private InMemoryPaymentRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryPaymentRepository();
  }

  @Test
  void save_storesPayment() {
    Payment payment = createPayment(TestConstants.FIXED_UUID);

    repository.save(payment);

    Optional<Payment> retrieved = repository.findById(payment.id());
    assertThat(retrieved).isPresent().contains(payment);
  }

  @Test
  void save_multiplePayments_storesAll() {
    Payment payment1 = createPayment(TestConstants.FIXED_UUID);
    Payment payment2 = createPayment(TestConstants.FIXED_UUID_2);

    repository.save(payment1);
    repository.save(payment2);

    assertThat(repository.findById(TestConstants.FIXED_UUID)).isPresent().contains(payment1);
    assertThat(repository.findById(TestConstants.FIXED_UUID_2)).isPresent().contains(payment2);
  }

  @Test
  void save_samePolicyTwice_overwritesPrevious() {
    Payment payment1 = createPayment(TestConstants.FIXED_UUID, PaymentStatus.AUTHORIZED);
    Payment payment2 = createPayment(TestConstants.FIXED_UUID, PaymentStatus.DECLINED);

    repository.save(payment1);
    repository.save(payment2);

    Optional<Payment> retrieved = repository.findById(TestConstants.FIXED_UUID);
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().status()).isEqualTo(PaymentStatus.DECLINED);
  }

  @Test
  void findById_nonExistentId_returnsEmpty() {
    Optional<Payment> retrieved = repository.findById(UUID.randomUUID());

    assertThat(retrieved).isEmpty();
  }

  @Test
  void findById_afterMultipleSaves_returnsCorrectPayment() {
    repository.save(createPayment(TestConstants.FIXED_UUID));
    repository.save(createPayment(TestConstants.FIXED_UUID_2));
    repository.save(createPayment(TestConstants.FIXED_UUID_3));

    assertThat(repository.findById(TestConstants.FIXED_UUID_2)).isPresent();
  }

  private Payment createPayment(UUID id) {
    return createPayment(id, PaymentStatus.AUTHORIZED);
  }

  private Payment createPayment(UUID id, PaymentStatus status) {
    return new Payment(
        id,
        status,
        TestConstants.TEST_CARD_LAST_FOUR,
        TestConstants.TEST_EXPIRY_MONTH,
        TestConstants.TEST_EXPIRY_YEAR,
        TestConstants.TEST_CURRENCY,
        TestConstants.TEST_AMOUNT);
  }
}
