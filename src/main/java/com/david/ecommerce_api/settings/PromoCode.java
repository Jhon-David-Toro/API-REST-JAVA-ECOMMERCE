package com.david.ecommerce_api.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false)
    private int percent;
}
