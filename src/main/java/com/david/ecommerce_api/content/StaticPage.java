package com.david.ecommerce_api.content;

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

import java.time.Instant;

/**
 * Text page of the store, such as "About us", linked from the menus and the footer as /p/{slug}.
 */
@Entity
@Table(
        name = "static_pages",
        uniqueConstraints = @UniqueConstraint(name = "uk_static_pages_slug", columnNames = "slug")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaticPage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 200)
    private String slug;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "edited_at", nullable = false)
    private Instant editedAt;

    public StaticPage(String title, String slug, String body, boolean published) {
        update(title, slug, body, published);
    }

    public final void update(String title, String slug, String body, boolean published) {
        this.title = title.trim();
        this.slug = slug;
        this.body = body == null ? "" : body;
        this.published = published;
        this.editedAt = Instant.now();
    }
}
