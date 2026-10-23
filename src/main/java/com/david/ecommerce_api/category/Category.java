package com.david.ecommerce_api.category;

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
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String image;

    @Column(nullable = false)
    private boolean visible;

    public Category(String name, String slug, String description, String image, boolean visible) {
        update(name, slug, description, image, visible);
    }

    public final void update(String name, String slug, String description, String image, boolean visible) {
        this.name = name.trim();
        this.slug = slug;
        this.description = description == null ? "" : description.trim();
        this.image = image == null ? "" : image.trim();
        this.visible = visible;
    }
}
