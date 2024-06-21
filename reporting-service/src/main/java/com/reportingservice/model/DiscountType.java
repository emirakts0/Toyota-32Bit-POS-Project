package com.reportingservice.model;

public enum DiscountType {
    PERCENTAGE("Percentage"),
    FIXED_AMOUNT("Fixed Amount");

    private final String text;

    DiscountType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
