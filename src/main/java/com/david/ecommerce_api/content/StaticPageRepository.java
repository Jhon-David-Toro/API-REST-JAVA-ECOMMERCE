package com.david.ecommerce_api.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaticPageRepository extends JpaRepository<StaticPage, Long> {

    List<StaticPage> findAllByOrderByTitleAsc();

    List<StaticPage> findByPublishedTrueOrderByTitleAsc();

    Optional<StaticPage> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
