package com.david.ecommerce_api.content;

import com.david.ecommerce_api.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Editable content of the storefront (banner, hero, menus, footer), kept as one JSON document.
 * There is only one row, with a fixed id.
 */
@Entity
@Table(name = "site_content")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteContentDocument extends BaseEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false, columnDefinition = "text")
    private String json;

    public SiteContentDocument(String json) {
        this.json = json;
    }

    public void replace(String json) {
        this.json = json;
    }
}
