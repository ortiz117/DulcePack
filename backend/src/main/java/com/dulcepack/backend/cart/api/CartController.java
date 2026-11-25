package com.dulcepack.backend.cart.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dulcepack.backend.cart.CartCookieService;
import com.dulcepack.backend.cart.CartService;
import com.dulcepack.backend.cart.CartService.CartResult;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cart;
    private final CartCookieService cookies;

    public CartController(CartService cart, CartCookieService cookies) {
        this.cart = cart;
        this.cookies = cookies;
    }

    @GetMapping
    CartResponse get(@CookieValue(name = CartCookieService.CART_COOKIE, required = false) String token) {
        return cart.getCart(token);
    }

    @PostMapping("/items")
    ResponseEntity<CartResponse> add(@Valid @RequestBody AddCartItemRequest request,
            @CookieValue(name = CartCookieService.CART_COOKIE, required = false) String token,
            HttpServletResponse response) {
        return respond(cart.addItem(token, request.productSlug(), request.quantity()), response);
    }

    @PatchMapping("/items/{productSlug}")
    ResponseEntity<CartResponse> update(@PathVariable String productSlug,
            @Valid @RequestBody UpdateCartItemRequest request,
            @CookieValue(name = CartCookieService.CART_COOKIE, required = false) String token,
            HttpServletResponse response) {
        return respond(cart.updateQuantity(token, productSlug, request.quantity()), response, HttpStatus.OK);
    }

    @DeleteMapping("/items/{productSlug}")
    ResponseEntity<CartResponse> remove(@PathVariable String productSlug,
            @CookieValue(name = CartCookieService.CART_COOKIE, required = false) String token,
            HttpServletResponse response) {
        return respond(cart.removeItem(token, productSlug), response, HttpStatus.OK);
    }

    @PostMapping("/coupon")
    ResponseEntity<CartResponse> applyCoupon(@Valid @RequestBody CouponRequest request,
            @CookieValue(name = CartCookieService.CART_COOKIE, required = false) String token) {
        return ResponseEntity.ok(cart.applyCoupon(token, request.code()).cart());
    }

    @DeleteMapping("/coupon")
    ResponseEntity<CartResponse> clearCoupon(
            @CookieValue(name = CartCookieService.CART_COOKIE, required = false) String token) {
        return ResponseEntity.ok(cart.clearCoupon(token).cart());
    }

    private ResponseEntity<CartResponse> respond(CartResult result, HttpServletResponse response) {
        return respond(result, response, HttpStatus.CREATED);
    }

    private ResponseEntity<CartResponse> respond(CartResult result, HttpServletResponse response,
            HttpStatus status) {
        if (result.created()) {
            cookies.write(response, result.token());
        }
        return ResponseEntity.status(status).body(result.cart());
    }
}
