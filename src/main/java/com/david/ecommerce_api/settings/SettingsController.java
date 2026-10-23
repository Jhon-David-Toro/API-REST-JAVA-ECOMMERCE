package com.david.ecommerce_api.settings;

import com.david.ecommerce_api.settings.SettingsDtos.PromoCodeDto;
import com.david.ecommerce_api.settings.SettingsDtos.PublicSettings;
import com.david.ecommerce_api.settings.SettingsDtos.SettingsRequest;
import com.david.ecommerce_api.settings.SettingsDtos.SettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/public/settings")
    public PublicSettings publicSettings() {
        return PublicSettings.from(settingsService.get());
    }

    @GetMapping("/public/promo-codes/{code}")
    public PromoCodeDto promoCode(@PathVariable String code) {
        PromoCode promo = settingsService.findPromo(code);

        return new PromoCodeDto(promo.getCode(), promo.getPercent());
    }

    @GetMapping("/admin/settings")
    public SettingsResponse settings() {
        return SettingsResponse.from(settingsService.get());
    }

    @PutMapping("/admin/settings")
    public SettingsResponse update(@Valid @RequestBody SettingsRequest request) {
        return SettingsResponse.from(settingsService.update(request));
    }
}
