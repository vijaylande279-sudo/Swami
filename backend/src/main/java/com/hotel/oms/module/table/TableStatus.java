package com.hotel.oms.module.table;

public enum TableStatus {
    AVAILABLE("gray"),
    OCCUPIED("red"),
    RESERVED("red"),
    ORDER_READY("orange"),
    READY_FOR_BILL("green");

    private final String borderColor;

    TableStatus(String borderColor) {
        this.borderColor = borderColor;
    }

    public String borderColor() {
        return borderColor;
    }
}
