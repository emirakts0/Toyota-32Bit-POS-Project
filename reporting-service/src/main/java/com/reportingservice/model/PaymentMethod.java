package com.reportingservice.model;

public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card");

    private final String text;

    PaymentMethod(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
