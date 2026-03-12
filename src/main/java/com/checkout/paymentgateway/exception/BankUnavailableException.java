package com.checkout.paymentgateway.exception;

public class BankUnavailableException extends RuntimeException {

  public BankUnavailableException(String message) {
    super(message);
  }
}
