package com.david.ecommerce_api.order;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 30)
    private String number;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 180)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Embedded
    private ShippingAddress address;

    @Column(length = 1000)
    private String notes;

    @Column(name = "promo_code", length = 40)
    private String promoCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shipping;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order(
            Long userId,
            String email,
            PaymentMethod paymentMethod,
            ShippingAddress address,
            String notes,
            String promoCode,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal shipping
    ) {
        this.userId = userId;
        this.email = email;
        this.paymentMethod = paymentMethod;
        this.address = address;
        this.notes = notes == null ? "" : notes.trim();
        this.promoCode = promoCode;
        this.subtotal = subtotal;
        this.discount = discount;
        this.shipping = shipping;
        this.total = subtotal.subtract(discount).add(shipping);
    }

    public void addItem(OrderItem item) {
        item.attachTo(this);
        this.items.add(item);
    }

    public void assignNumber(String number) {
        this.number = number;
    }

    public boolean belongsTo(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public boolean canBeCancelled() {
        return this.status.canMoveTo(OrderStatus.CANCELLED);
    }

    public void moveTo(OrderStatus target) {
        Guard.isTrue(
                this.status.canMoveTo(target),
                ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                this.status.apiValue(),
                target.apiValue()
        );

        this.status = target;
    }
}
