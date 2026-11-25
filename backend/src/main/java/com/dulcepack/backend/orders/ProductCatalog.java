package com.dulcepack.backend.orders;

import java.math.BigDecimal;

interface ProductCatalog {

    ProductSnapshot findRequired(String productId);

    record ProductSnapshot(String id, String name, BigDecimal unitPrice) {
    }
}
