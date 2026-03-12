# Checkout Payment Gateway

A REST API payment gateway built with Spring Boot 4 / Java 21 that allows merchants to process card payments and retrieve payment details via an acquiring bank simulator.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Bank Simulator](#bank-simulator)
- [Design Decisions](#design-decisions)
- [Improvements & Future Work](#improvements--future-work)
- [Testing Strategy](#testing-strategy)
- [Known Issues](#known-issues)

---

## Overview

The payment flow involves three actors:

```
Merchant  →  Payment Gateway (this service)  →  Acquiring Bank (simulated)
```

A merchant can:
1. **Process a payment** — submit card details; the gateway validates them, forwards to the bank, and returns `AUTHORIZED` or `DECLINED`.
2. **Retrieve a payment** — fetch a previously processed payment by its ID.
3. **Receive a rejection** — if the request is invalid, the gateway rejects it immediately without contacting the bank.

---

## Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Docker + Docker Compose | Any recent version |

### 1. Start the Bank Simulator

```bash
docker-compose up -d
```

This starts a [Mountebank](http://www.mbtest.org/) bank simulator on port `8080` with behaviour defined in `imposters/bank_simulator.ejs`.

### 2. Run the Gateway

```bash
./mvnw spring-boot:run
```

The gateway starts on **port 8081**.

Swagger UI is available at: `http://localhost:8081/swagger-ui.html`

### 3. Run Tests

```bash
./mvnw test
```

---

## API Reference

Base path: `/api/v1`

### POST `/payments` — Process a Payment

**Request body:**

```json
{
  "card_number": "2222405343248877",
  "expiry_month": 4,
  "expiry_year": 2027,
  "currency": "GBP",
  "amount": 1050,
  "cvv": "123"
}
```

| Field | Type | Validation |
|---|---|---|
| `card_number` | string | Required. 14–19 numeric digits |
| `expiry_month` | integer | Required. 1–12 |
| `expiry_year` | integer | Required. Combined month+year must be in the future |
| `currency` | string | Required. One of `USD`, `GBP`, `EUR` |
| `amount` | integer | Required. Positive integer (minor currency unit, e.g. 1050 = £10.50) |
| `cvv` | string | Required. 3–4 numeric digits |

**Responses:**

| Status | Meaning |
|---|---|
| `201 Created` | Payment processed (body contains `status: Authorized` or `status: Declined`) |
| `400 Bad Request` | Validation failure — `RejectedPaymentResponse` with error list, or malformed JSON (`ProblemDetail`) |
| `502 Bad Gateway` | Bank unavailable (5xx from bank or connection error) |
| `500 Internal Server Error` | Unexpected error |

**Success response (201):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "Authorized",
  "last_four_card_digits": "8877",
  "expiry_month": 4,
  "expiry_year": 2027,
  "currency": "GBP",
  "amount": 1050
}
```

**Rejection response (400):**

```json
{
  "status": "Rejected",
  "errors": [
    "card_number: must match \"\\d{14,19}\"",
    "expiry_month: must be less than or equal to 12"
  ]
}
```

---

### GET `/payments/{id}` — Retrieve a Payment

**Path parameter:** `id` — UUID of the payment

**Responses:**

| Status | Meaning |
|---|---|
| `200 OK` | Payment found (same body shape as the 201 response above) |
| `404 Not Found` | No payment exists for the given ID |
| `400 Bad Request` | `id` is not a valid UUID |

---

## Bank Simulator

The Mountebank imposter (`imposters/bank_simulator.ejs`) listens on `http://localhost:8080` and simulates bank decisions based on the **last digit of the card number**:

| Last digit | Bank response |
|---|---|
| Odd (1, 3, 5, 7, 9) | `200 {"authorized": true, "authorization_code": "<uuid>"}` |
| Even (2, 4, 6, 8) | `200 {"authorized": false, "authorization_code": ""}` |
| `0` | `503 Service Unavailable` (simulates bank down) |
| Missing required fields | `400 Bad Request` |

This allows deterministic testing of all payment outcomes without a real bank connection.

---

## Design Decisions

### 1. Card Data — Store Only Last 4 Digits

The full card number is never persisted. `PaymentService` extracts the last 4 characters before constructing the `Payment` entity. The CVV is similarly excluded from storage entirely. This aligns with PCI DSS principles and eliminates a whole class of data breach risk.

`PaymentRequest.toString()` and `BankPaymentRequest.toString()` are overridden to mask sensitive fields, preventing card numbers and CVVs from leaking into logs.

### 2. `AUTHORIZED` / `DECLINED` Both Return 201

Both outcomes represent a successfully *processed* payment (the bank was reached and gave a deterministic answer). Using `201 Created` for both is correct: a `Payment` record was created in either case. A `DECLINED` payment is not an error — the merchant needs the payment ID for reconciliation.

### 3. Bank 4xx → 500, Bank 5xx → 502

- **Bank 5xx** (server error) or a network failure means the bank is *unavailable*. A retry might succeed, so `BankUnavailableException` → `502 Bad Gateway` is appropriate.
- **Bank 4xx** (client error) means the bank *processed* our request but rejected it due to bad data we sent. This signals an internal problem in the gateway (we should not have sent something the bank rejects). It is not retryable and does not warrant a 502; a `500 Internal Server Error` communicates the unexpected state correctly.

### 4. Custom Validators Instead of Inline Logic

Card expiry (month+year combination in the future) and currency support require cross-field or non-trivial logic. These are extracted into reusable `@ValidCardExpiry` and `@SupportedCurrency` constraint annotations backed by dedicated `ConstraintValidator` implementations, keeping `PaymentRequest` declarative and the validation logic independently testable.

### 5. In-Memory Repository with `ConcurrentHashMap`

As specified in the requirements, no real database is needed. `InMemoryPaymentRepository` wraps a `ConcurrentHashMap` for thread-safety under concurrent requests without the overhead of synchronisation locks. Payments are stored by `UUID` and are immediately consistent.

### 6. Spring `RestClient` Over `RestTemplate`

`RestClient` (introduced in Spring 6.1) is the modern, fluent, synchronous HTTP client. It replaces the deprecated `RestTemplate` and provides a cleaner API. It is injected via a `RestClient.Builder` bean, making `BankClient` straightforward to test by providing a different builder in test context.

---
