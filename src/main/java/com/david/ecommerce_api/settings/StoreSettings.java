package com.david.ecommerce_api.settings;

import com.david.ecommerce_api.common.persistence.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings of the store. There is only one row, with a fixed id.
 */
@Entity
@Table(name = "store_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreSettings extends BaseEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "store_name", nullable = false, length = 120)
    private String storeName;

    @Column(name = "contact_email", nullable = false, length = 180)
    private String contactEmail;

    @Column(name = "contact_phone", length = 60)
    private String contactPhone;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "free_shipping_over", nullable = false, precision = 10, scale = 2)
    private BigDecimal freeShippingOver;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold;

    @ElementCollection
    @CollectionTable(name = "store_promo_codes", joinColumns = @JoinColumn(name = "settings_id"))
    private List<PromoCode> promoCodes = new ArrayList<>();

    public static StoreSettings defaults() {
        StoreSettings settings = new StoreSettings();
        settings.update(
                "SHOP.CO",
                "support@shop.test",
                "+1 555 0100",
                "USD",
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(200),
                10,
                List.of(new PromoCode("SHOP20", 20))
        );
        return settings;
    }

    public void update(
            String storeName,
            String contactEmail,
            String contactPhone,
            String currency,
            BigDecimal shippingFee,
            BigDecimal freeShippingOver,
            int lowStockThreshold,
            List<PromoCode> promoCodes
    ) {
        this.storeName = storeName.trim();
        this.contactEmail = contactEmail.trim();
        this.contactPhone = contactPhone == null ? "" : contactPhone.trim();
        this.currency = currency.trim().toUpperCase();
        this.shippingFee = shippingFee;
        this.freeShippingOver = freeShippingOver;
        this.lowStockThreshold = lowStockThreshold;
        this.promoCodes.clear();
        this.promoCodes.addAll(promoCodes);
    }

    public java.util.Optional<PromoCode> findPromo(String code) {
        return this.promoCodes.stream()
                .filter(promo -> promo.getCode().equalsIgnoreCase(code.trim()))
                .findFirst();
    }
}
