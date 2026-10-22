package com.david.ecommerce_api.order;

import com.david.ecommerce_api.auth.AuthenticatedUser;
import com.david.ecommerce_api.auth.Role;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.order.OrderDtos.CreateOrderRequest;
import com.david.ecommerce_api.order.OrderDtos.LineRequest;
import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductVariant;
import com.david.ecommerce_api.product.repository.ProductRepository;
import com.david.ecommerce_api.product.repository.ProductVariantRepository;
import com.david.ecommerce_api.settings.PromoCode;
import com.david.ecommerce_api.settings.SettingsService;
import com.david.ecommerce_api.settings.StoreSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String NUMBER_PREFIX = "SH-";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final SettingsService settingsService;

    /**
     * Places an order: prices, totals, discount and shipping are worked out here from the catalog and the
     * settings, never taken from the request, and the sold units leave the stock of their variants.
     */
    @Transactional
    public Order create(AuthenticatedUser user, CreateOrderRequest request) {
        StoreSettings settings = settingsService.get();
        List<OrderItem> items = request.items().stream().map(this::reserve).toList();

        BigDecimal subtotal = items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        PromoCode promo = findPromo(settings, request.promoCode());
        BigDecimal discount = promo == null ? BigDecimal.ZERO : percentOf(subtotal, promo.getPercent());
        BigDecimal shipping = subtotal.compareTo(settings.getFreeShippingOver()) >= 0 ? BigDecimal.ZERO : settings.getShippingFee();

        Order order = new Order(
                user.id(),
                request.email() == null || request.email().isBlank() ? user.email() : request.email().trim().toLowerCase(Locale.ROOT),
                PaymentMethod.fromApi(request.paymentMethod()),
                request.address().toEntity(),
                request.notes(),
                promo == null ? null : promo.getCode(),
                subtotal,
                discount,
                shipping
        );
        items.forEach(order::addItem);

        Order saved = orderRepository.saveAndFlush(order);
        saved.assignNumber(NUMBER_PREFIX + String.format("%04d", saved.getId()));

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> listForUser(Long userId) {
        return orderRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public Order getVisibleTo(AuthenticatedUser user, Long id) {
        Order order = find(id);

        if (user.role() != Role.ADMIN && !order.belongsTo(user.id())) {
            throw new DomainException(ErrorCode.ORDER_NOT_FOUND);
        }

        return order;
    }

    @Transactional
    public Order cancelOwn(AuthenticatedUser user, Long id) {
        Order order = find(id);

        if (!order.belongsTo(user.id())) {
            throw new DomainException(ErrorCode.ORDER_NOT_FOUND);
        }

        return changeStatus(order, OrderStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<Order> listAll(OrderStatus status, String query) {
        List<Order> orders = status == null
                ? orderRepository.findAllByOrderByIdDesc()
                : orderRepository.findByStatusOrderByIdDesc(status);

        if (query == null || query.isBlank()) {
            return orders;
        }

        String text = query.trim().toLowerCase(Locale.ROOT);

        return orders.stream()
                .filter(order -> order.getNumber().toLowerCase(Locale.ROOT).contains(text)
                        || order.getEmail().toLowerCase(Locale.ROOT).contains(text)
                        || order.getAddress().getFullName().toLowerCase(Locale.ROOT).contains(text))
                .toList();
    }

    @Transactional(readOnly = true)
    public Order get(Long id) {
        return find(id);
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus target) {
        return changeStatus(find(id), target);
    }

    private Order changeStatus(Order order, OrderStatus target) {
        order.moveTo(target);

        if (target == OrderStatus.CANCELLED) {
            restock(order);
        }

        return order;
    }

    private void restock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getVariantId() != null) {
                variantRepository.findById(item.getVariantId()).ifPresent(variant -> variant.increaseStock(item.getQuantity()));
            }

            productRepository.findById(item.getProductId()).ifPresent(product -> product.decreaseSold(item.getQuantity()));
        }
    }

    private OrderItem reserve(LineRequest line) {
        Product product = productRepository.findById(line.productId())
                .filter(Product::isPublished)
                .orElseThrow(() -> new DomainException(ErrorCode.ORDER_PRODUCT_UNAVAILABLE, line.productId()));

        ProductVariant found = resolveVariant(product, line);
        ProductVariant variant = variantRepository.findByIdAndActiveTrue(found.getId())
                .orElseThrow(() -> new DomainException(ErrorCode.ORDER_PRODUCT_UNAVAILABLE, product.getName()));

        variant.decreaseStock(line.quantity());
        product.increaseSold(line.quantity());

        return new OrderItem(
                product.getId(),
                variant.getId(),
                product.getName(),
                product.getThumbnailUrl(),
                variant.getSku(),
                isBlank(line.color()) ? "" : variant.getColor().getName(),
                isBlank(line.size()) ? "" : variant.getSize().getName(),
                variant.getEffectivePrice(),
                line.quantity()
        );
    }

    /**
     * Finds the variant of a line by color and size; a product with a single active variant is chosen when
     * the line names no color and size.
     */
    private ProductVariant resolveVariant(Product product, LineRequest line) {
        List<ProductVariant> active = product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> variant.getColor().isActive() && variant.getSize().isActive())
                .toList();

        boolean unspecified = isBlank(line.color()) && isBlank(line.size());

        if (unspecified && active.size() == 1) {
            return active.get(0);
        }

        return active.stream()
                .filter(variant -> variant.getColor().getName().equalsIgnoreCase(orEmpty(line.color()).trim())
                        && variant.getSize().getName().equalsIgnoreCase(orEmpty(line.size()).trim()))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.ORDER_PRODUCT_UNAVAILABLE, product.getName()));
    }

    private PromoCode findPromo(StoreSettings settings, String code) {
        if (isBlank(code)) {
            return null;
        }

        return settings.findPromo(code).orElseThrow(() -> new DomainException(ErrorCode.PROMO_NOT_FOUND));
    }

    private BigDecimal percentOf(BigDecimal amount, int percent) {
        return amount.multiply(BigDecimal.valueOf(percent)).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private Order find(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.ORDER_NOT_FOUND));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
