package com.projects.applitracker;

import org.springframework.stereotype.Component;


public class StripePaymentService implements PaymentService {

    @Override
    public void payForOrder() {
        System.out.println("Stripe Payment Service is running...");
    }
}
