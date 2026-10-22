package com.david.ecommerce_api.settings;

import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.validation.Guard;
import com.david.ecommerce_api.settings.SettingsDtos.PromoCodeDto;
import com.david.ecommerce_api.settings.SettingsDtos.SettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    /**
     * Gives the settings of the store, creating them with the default values the first time.
     */
    @Transactional
    public StoreSettings get() {
        return settingsRepository.findById(StoreSettings.SINGLETON_ID)
                .orElseGet(() -> settingsRepository.save(StoreSettings.defaults()));
    }

    @Transactional
    public StoreSettings update(SettingsRequest request) {
        List<PromoCodeDto> promos = request.promoCodes() == null ? List.of() : request.promoCodes();
        Set<String> codes = new HashSet<>();

        for (PromoCodeDto promo : promos) {
            Guard.isTrue(codes.add(promo.code().toLowerCase()), ErrorCode.PROMO_DUPLICATED, promo.code());
        }

        StoreSettings settings = get();
        settings.update(
                request.storeName(),
                request.contactEmail(),
                request.contactPhone(),
                request.currency(),
                request.shippingFee(),
                request.freeShippingOver(),
                request.lowStockThreshold(),
                promos.stream().map(promo -> new PromoCode(promo.code().trim(), promo.percent())).toList()
        );

        return settingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public PromoCode findPromo(String code) {
        return settingsRepository.findById(StoreSettings.SINGLETON_ID)
                .orElseGet(StoreSettings::defaults)
                .findPromo(code)
                .orElseThrow(() -> new DomainException(ErrorCode.PROMO_NOT_FOUND));
    }
}
