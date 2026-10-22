package com.david.ecommerce_api.settings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<StoreSettings, Long> {
}
