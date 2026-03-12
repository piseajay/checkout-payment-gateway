package com.checkout.paymentgateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.checkout.paymentgateway.client.BankClient;
import com.checkout.paymentgateway.exception.BankUnavailableException;
import com.checkout.paymentgateway.exception.PaymentNotFoundException;
import com.checkout.paymentgateway.model.entity.PaymentStatus;
import com.checkout.paymentgateway.model.request.PaymentRequest;
import com.checkout.paymentgateway.model.response.PaymentResponse;
import com.checkout.paymentgateway.repository.InMemoryPaymentRepository;
import java.time.Year;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private BankClient bankClient;

  private PaymentService paymentService;

  private static final int FUTURE_YEAR = Year.now().getValue() + 2;

  @BeforeEach
  void setUp() {
    paymentService = new PaymentService(bankClient, new InMemoryPaymentRepository());
  }

  @Test
  void processPayment_bankAuthorizes_returnsAuthorizedResponse() {
    when(bankClient.processPayment(any())).thenReturn(true);

    PaymentResponse response = paymentService.processPayment(validRequest());

    assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.id()).isNotNull();
    assertThat(response.lastFourCardDigits()).isEqualTo("8877");
    assertThat(response.currency()).isEqualTo("GBP");
    assertThat(response.amount()).isEqualTo(1050);
  }

  @Test
  void processPayment_bankDeclines_returnsDeclinedResponse() {
    when(bankClient.processPayment(any())).thenReturn(false);

    PaymentResponse response = paymentService.processPayment(validRequest());

    assertThat(response.status()).isEqualTo(PaymentStatus.DECLINED);
  }

  @Test
  void processPayment_bankUnavailable_throwsBankUnavailableException() {
    when(bankClient.processPayment(any())).thenThrow(new BankUnavailableException("503"));

    assertThatThrownBy(() -> paymentService.processPayment(validRequest()))
        .isInstanceOf(BankUnavailableException.class);
  }

  @Test
  void processPayment_storedPaymentCanBeRetrieved() {
    when(bankClient.processPayment(any())).thenReturn(true);
    PaymentResponse created = paymentService.processPayment(validRequest());

    PaymentResponse retrieved = paymentService.getPayment(created.id());

    assertThat(retrieved.id()).isEqualTo(created.id());
    assertThat(retrieved.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(retrieved.lastFourCardDigits()).isEqualTo("8877");
  }

  @Test
  void getPayment_unknownId_throwsPaymentNotFoundException() {
    assertThatThrownBy(() -> paymentService.getPayment(UUID.randomUUID()))
        .isInstanceOf(PaymentNotFoundException.class);
  }

  @Test
  void processPayment_masksCardNumber_doesNotStoreFullNumber() {
    when(bankClient.processPayment(any())).thenReturn(true);
    PaymentResponse response = paymentService.processPayment(validRequest());

    assertThat(response.lastFourCardDigits()).isEqualTo("8877");
    assertThat(response.lastFourCardDigits()).hasSize(4);
  }

  private PaymentRequest validRequest() {
    return new PaymentRequest("2222405343248877", 12, FUTURE_YEAR, "GBP", 1050, "123");
  }
}
