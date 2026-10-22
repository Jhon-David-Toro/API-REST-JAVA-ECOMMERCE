package com.david.ecommerce_api.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

    @Column(name = "ship_full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "ship_phone", nullable = false, length = 60)
    private String phone;

    @Column(name = "ship_street", nullable = false, length = 250)
    private String street;

    @Column(name = "ship_city", nullable = false, length = 120)
    private String city;

    @Column(name = "ship_state", length = 120)
    private String state;

    @Column(name = "ship_postal_code", length = 30)
    private String postalCode;

    @Column(name = "ship_country", nullable = false, length = 120)
    private String country;
}
