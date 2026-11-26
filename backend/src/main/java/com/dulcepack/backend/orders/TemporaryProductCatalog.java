package com.dulcepack.backend.orders;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
class TemporaryProductCatalog implements ProductCatalog {

    private final Map<String, ProductSnapshot> products = Map.of(
        "fiesta-dulce", new ProductSnapshot("fiesta-dulce", "Pack Fiesta Dulce", new BigDecimal("189.00")),
        "box-picosita", new ProductSnapshot("box-picosita", "Box Picosita", new BigDecimal("159.00"))
    );

    @Override
    public ProductSnapshot findRequired(String productId) {
        var product = products.get(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown product: " + productId);
        }
        return product;
    }
}
