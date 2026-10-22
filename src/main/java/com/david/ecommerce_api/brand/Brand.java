package com.david.ecommerce_api.brand;

import com.david.ecommerce_api.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "brands",
        uniqueConstraints = @UniqueConstraint(name = "uk_brands_slug", columnNames = "slug")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(length = 1000)
    private String logo;

    @Column(nullable = false)
    private boolean visible;

    public Brand(String name, String slug, String logo, boolean visible) {
        update(name, slug, logo, visible);
    }

    public final void update(String name, String slug, String logo, boolean visible) {
        this.name = name.trim();
        this.slug = slug;
        this.logo = logo == null ? "" : logo.trim();
        this.visible = visible;
    }
}
