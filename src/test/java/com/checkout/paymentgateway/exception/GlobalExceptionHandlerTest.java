package com.checkout.paymentgateway.exception;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.checkout.paymentgateway.constants.TestConstants;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void handlePaymentNotFound_returnsNotFoundStatus() {
    PaymentNotFoundException exception = new PaymentNotFoundException(TestConstants.FIXED_UUID);

    ResponseEntity<?> response = handler.handlePaymentNotFound(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void handleBankUnavailable_returnsBadGatewayStatus() {
    BankUnavailableException exception = new BankUnavailableException("Bank is down");

    ResponseEntity<?> response = handler.handleBankUnavailable(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void handleValidationFailure_returnsBadRequestStatus() {
    MethodArgumentNotValidException exception = createValidationException(java.util.Collections.emptyList());

    ResponseEntity<?> response = handler.handleValidationFailure(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void handleMalformedJson_returnsBadRequestStatus() {
    HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

    ResponseEntity<?> response = handler.handleMalformedJson(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void handleUnexpected_returnsInternalServerErrorStatus() {
    Exception exception = new RuntimeException("Unexpected error");

    ResponseEntity<?> response = handler.handleUnexpected(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
  }

  private MethodArgumentNotValidException createValidationException(List<ObjectError> errors) {
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    var bindingResult = mock(org.springframework.validation.BindingResult.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getAllErrors()).thenReturn(errors);
    return exception;
  }
}
