package com.checkout.paymentgateway.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.checkout.paymentgateway.exception.BankUnavailableException;
import com.checkout.paymentgateway.model.request.PaymentRequest;

@Component
public class BankClient {

    private static final Logger log = LoggerFactory.getLogger(BankClient.class);

    private final RestClient restClient;

    public BankClient(
            RestClient.Builder builder, @Value("${bank.simulator.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public boolean processPayment(PaymentRequest request) {

        log.debug("Forwarding payment to bank with details: {}", request);

        BankPaymentRequest bankRequest = toBankRequest(request);

        try {
            BankPaymentResponse response =
                    restClient
                            .post()
                            .uri("/payments")
                            .body(bankRequest)
                            .retrieve()
                            .body(BankPaymentResponse.class);

            if (response == null) {
                throw new BankUnavailableException("Bank returned an empty response");
            }

            log.info("Bank responded — authorized: {}", response.authorized());
            return response.authorized();

        } catch (HttpServerErrorException ex) {
            log.warn("Bank simulator unavailable — status: {}", ex.getStatusCode());
            throw new BankUnavailableException("Acquiring Bank is currently unavailable");
        } catch (BankUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("An unexpected error occurred: {} ", ex.getMessage());
            throw ex;
        }
    }

    private BankPaymentRequest toBankRequest(PaymentRequest request) {
        String expiryDate = String.format("%02d/%d", request.expiryMonth(), request.expiryYear());
        return new BankPaymentRequest(
                request.cardNumber(), expiryDate, request.currency(), request.amount(), request.cvv());
    }
}
