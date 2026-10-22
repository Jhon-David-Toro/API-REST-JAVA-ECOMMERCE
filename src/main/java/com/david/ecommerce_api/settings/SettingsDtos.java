package com.david.ecommerce_api.settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class SettingsDtos {

    private SettingsDtos() {
    }

    public record PromoCodeDto(
            @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "may only contain letters, digits, dash and underscore") String code,
            @Min(1) @Max(100) int percent
    ) {
    }

    public record SettingsRequest(
            @NotBlank @Size(max = 120) String storeName,
            @NotBlank @Email @Size(max = 180) String contactEmail,
            @Size(max = 60) String contactPhone,
            @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "must be a 3-letter currency code") String currency,
            @NotNull @DecimalMin("0") BigDecimal shippingFee,
            @NotNull @DecimalMin("0") BigDecimal freeShippingOver,
            @Min(0) int lowStockThreshold,
            @Valid List<PromoCodeDto> promoCodes
    ) {
    }

    public record SettingsResponse(
            String storeName,
            String contactEmail,
            String contactPhone,
            String currency,
            BigDecimal shippingFee,
            BigDecimal freeShippingOver,
            int lowStockThreshold,
            List<PromoCodeDto> promoCodes
    ) {
        public static SettingsResponse from(StoreSettings settings) {
            return new SettingsResponse(
                    settings.getStoreName(),
                    settings.getContactEmail(),
                    settings.getContactPhone(),
                    settings.getCurrency(),
                    settings.getShippingFee(),
                    settings.getFreeShippingOver(),
                    settings.getLowStockThreshold(),
                    settings.getPromoCodes().stream().map(promo -> new PromoCodeDto(promo.getCode(), promo.getPercent())).toList()
            );
        }
    }

    public record PublicSettings(
            String storeName,
            String contactEmail,
            String contactPhone,
            String currency,
            BigDecimal shippingFee,
            BigDecimal freeShippingOver
    ) {
        public static PublicSettings from(StoreSettings settings) {
            return new PublicSettings(
                    settings.getStoreName(),
                    settings.getContactEmail(),
                    settings.getContactPhone(),
                    settings.getCurrency(),
                    settings.getShippingFee(),
                    settings.getFreeShippingOver()
            );
        }
    }
}
