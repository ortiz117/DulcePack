package com.dulcepack.backend.catalog.domain;

public enum StockStatus {
    AVAILABLE,
    LOW,
    OUT_OF_STOCK;

    public static StockStatus from(int available, int minimumStock) {
        if (available == 0) {
            return OUT_OF_STOCK;
        }
        return available <= minimumStock ? LOW : AVAILABLE;
    }
}
