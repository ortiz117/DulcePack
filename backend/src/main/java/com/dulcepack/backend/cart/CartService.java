package com.dulcepack.backend.cart;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dulcepack.backend.cart.api.CartItemResponse;
import com.dulcepack.backend.cart.api.CartResponse;
import com.dulcepack.backend.cart.domain.CartEntity;
import com.dulcepack.backend.cart.domain.CartItemEntity;
import com.dulcepack.backend.cart.domain.PromotionEntity;
import com.dulcepack.backend.cart.repository.CartItemRepository;
import com.dulcepack.backend.cart.repository.CartRepository;
import com.dulcepack.backend.cart.repository.PromotionRepository;
import com.dulcepack.backend.catalog.repository.ProductRepository;
import com.dulcepack.backend.security.TokenService;
import com.dulcepack.backend.shared.error.ApiException;

@Service
public class CartService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final CartRepository carts;
    private final CartItemRepository items;
    private final PromotionRepository promotions;
    private final ProductRepository products;
    private final TokenService tokenService;
    private final CartProperties properties;
    private final Clock clock;

    public CartService(CartRepository carts, CartItemRepository items, PromotionRepository promotions,
            ProductRepository products, TokenService tokenService, CartProperties properties, Clock clock) {
        this.carts = carts;
        this.items = items;
        this.promotions = promotions;
        this.products = products;
        this.tokenService = tokenService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public CartResponse getCart(String rawToken) {
        return findCart(rawToken).map(this::toResponse).orElseGet(this::emptyCart);
    }

    @Transactional
    public CartResult addItem(String rawToken, String slug, int quantity) {
        ResolvedCart resolved = resolveOrCreate(rawToken);
        var product = products.findBySlugForUpdate(normalizeSlug(slug))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND",
                        "Product was not found"));
        Instant now = clock.instant();
        Optional<CartItemEntity> existing = items.findByCartIdAndProductSlugForUpdate(
                resolved.cart().getId(), product.getSlug());
        int current = existing.map(CartItemEntity::getQuantity).orElse(0);
        ensureCapacity(resolved.cart().getId(), product.getSlug(), product.getInventory().getAvailable(),
                current + quantity);
        if (existing.isPresent()) {
            existing.get().increase(quantity, now);
        } else {
            CartItemEntity item = new CartItemEntity(UUID.randomUUID(), resolved.cart(), product, quantity, now);
            resolved.cart().getItems().add(item);
        }
        resolved.cart().touch(now);
        return new CartResult(resolved.token(), resolved.created(), toResponse(resolved.cart()));
    }

    @Transactional
    public CartResult updateQuantity(String rawToken, String slug, int quantity) {
        CartEntity cart = requireCart(rawToken);
        var product = products.findBySlugForUpdate(normalizeSlug(slug))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND",
                        "Product was not found"));
        CartItemEntity item = items.findByCartIdAndProductSlugForUpdate(cart.getId(), product.getSlug())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND",
                        "Product is not in the cart"));
        ensureCapacity(cart.getId(), product.getSlug(), product.getInventory().getAvailable(), quantity);
        item.setQuantity(quantity, clock.instant());
        cart.touch(clock.instant());
        return new CartResult(rawToken, false, toResponse(cart));
    }

    @Transactional
    public CartResult removeItem(String rawToken, String slug) {
        CartEntity cart = requireCart(rawToken);
        CartItemEntity item = items.findByCartIdAndProductSlugForUpdate(cart.getId(), normalizeSlug(slug))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND",
                        "Product is not in the cart"));
        cart.getItems().remove(item);
        cart.touch(clock.instant());
        return new CartResult(rawToken, false, toResponse(cart));
    }

    @Transactional
    public CartResult applyCoupon(String rawToken, String code) {
        CartEntity cart = requireCart(rawToken);
        String normalized = PromotionEntity.normalize(code);
        PromotionEntity promotion = promotions.findByCodeAndActiveTrue(normalized)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COUPON",
                        "Coupon is invalid or inactive"));
        cart.applyCoupon(promotion.getCode());
        return new CartResult(rawToken, false, toResponse(cart));
    }

    @Transactional
    public CartResult clearCoupon(String rawToken) {
        CartEntity cart = requireCart(rawToken);
        cart.clearCoupon();
        return new CartResult(rawToken, false, toResponse(cart));
    }

    private void ensureCapacity(UUID cartId, String slug, int available, int finalQuantity) {
        Instant cutoff = clock.instant().minus(Duration.ofMinutes(properties.reservationMinutes()));
        int reservedByOtherCarts = items.reservedQuantityExcluding(slug, clock.instant(), cutoff, cartId);
        if (reservedByOtherCarts + finalQuantity > available) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK",
                    "Requested quantity exceeds available stock");
        }
    }

    private Optional<CartEntity> findCart(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return Optional.empty();
        }
        Optional<CartEntity> found = carts.findByTokenHash(tokenService.hash(rawToken));
        if (found.isPresent() && !found.get().getExpiresAt().isAfter(clock.instant())) {
            carts.delete(found.get());
            return Optional.empty();
        }
        return found;
    }

    private CartEntity requireCart(String rawToken) {
        return findCart(rawToken).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                "CART_NOT_FOUND", "Cart was not found"));
    }

    private ResolvedCart resolveOrCreate(String rawToken) {
        Optional<CartEntity> existing = findCart(rawToken);
        if (existing.isPresent()) {
            return new ResolvedCart(existing.get(), rawToken, false);
        }
        String token = tokenService.generate();
        Instant now = clock.instant();
        CartEntity cart = carts.save(new CartEntity(UUID.randomUUID(), null, tokenService.hash(token),
                properties.defaultCoupon(), now, now.plus(Duration.ofDays(7))));
        return new ResolvedCart(cart, token, true);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireStaleCarts() {
        carts.deleteByExpiresAtBefore(clock.instant());
    }

    private CartResponse emptyCart() {
        return new CartResponse(List.of(), 0, ZERO, ZERO, ZERO, ZERO, properties.defaultCoupon());
    }

    private CartResponse toResponse(CartEntity cart) {
        List<CartItemResponse> lines = items.findByCartId(cart.getId()).stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getSlug(),
                        item.getProduct().getName(),
                        item.getProduct().getImage() == null ? null
                                : "/api/catalog/products/" + item.getProduct().getSlug() + "/image",
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                        item.getProduct().getInventory().getAvailable()))
                .toList();
        int itemCount = lines.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal subtotal = lines.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal shipping = lines.isEmpty() ? ZERO : properties.shippingFee();
        BigDecimal discount = ZERO;
        if (!lines.isEmpty() && StringUtils.hasText(cart.getCouponCode())) {
            discount = promotions.findByCodeAndActiveTrue(PromotionEntity.normalize(cart.getCouponCode()))
                    .map(PromotionEntity::getDiscountAmount)
                    .orElse(ZERO);
        }
        BigDecimal total = subtotal.add(shipping).subtract(discount).max(ZERO);
        return new CartResponse(lines, itemCount, subtotal, shipping, discount, total,
                cart.getCouponCode() == null ? null : cart.getCouponCode().toUpperCase(Locale.ROOT));
    }

    private String normalizeSlug(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record CartResult(String token, boolean created, CartResponse cart) {
    }

    private record ResolvedCart(CartEntity cart, String token, boolean created) {
    }
}
