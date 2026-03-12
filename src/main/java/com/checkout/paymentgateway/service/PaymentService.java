package com.checkout.paymentgateway.service;

import com.checkout.paymentgateway.client.BankClient;
import com.checkout.paymentgateway.exception.PaymentNotFoundException;
import com.checkout.paymentgateway.model.entity.Payment;
import com.checkout.paymentgateway.model.entity.PaymentStatus;
import com.checkout.paymentgateway.model.request.PaymentRequest;
import com.checkout.paymentgateway.model.response.PaymentResponse;
import com.checkout.paymentgateway.repository.PaymentRepository;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final BankClient bankClient;
    private final PaymentRepository paymentRepository;

    public PaymentService(BankClient bankClient, PaymentRepository paymentRepository) {
        this.bankClient = bankClient;
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment — with details {} ", request);

        boolean authorized = bankClient.processPayment(request);
        PaymentStatus status = authorized ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;

        Payment payment =
                new Payment(
                        UUID.randomUUID(),
                        status,
                        request.lastFour(),
                        request.expiryMonth(),
                        request.expiryYear(),
                        request.currency(),
                        request.amount());

        paymentRepository.save(payment);

        log.info("Payment saved — id: {}, status: {}", payment.id(), payment.status());
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPayment(UUID id) {
        log.debug("Retrieving payment — id: {}", id);
        Payment payment =
                paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
        return PaymentResponse.from(payment);
    }
}
