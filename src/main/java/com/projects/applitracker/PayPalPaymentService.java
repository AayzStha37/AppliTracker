package com.projects.applitracker;

import org.springframework.stereotype.Component;

@Component
public class PayPalPaymentService implements PaymentService {
    @Override
    public void payForOrder() {
        System.out.println("Paypal Payment Service is running...");
    }
}
