package com.checkout.paymentgateway.constants;

import java.util.UUID;

public final class TestConstants {

  public static final String TEST_CARD_LAST_FOUR = "8877";

  public static final int TEST_EXPIRY_MONTH = 12;
  public static final int TEST_EXPIRY_YEAR = 2025;
  public static final String TEST_CURRENCY = "GBP";
  public static final String TEST_CURRENCY_USD = "USD";
  public static final String TEST_CURRENCY_EUR = "EUR";
  public static final int TEST_AMOUNT = 1050;

  public static final String REJECTED_STATUS = "Rejected";

  public static final String ERROR_CARD_INVALID = "Card number is invalid";
  public static final String ERROR_CVV_REQUIRED = "CVV is required";
  public static final String ERROR_AMOUNT_POSITIVE = "Amount must be positive";

  public static final UUID FIXED_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
  public static final UUID FIXED_UUID_2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
  public static final UUID FIXED_UUID_3 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

  private TestConstants() {
    throw new AssertionError("Cannot instantiate constants class");
  }
}
