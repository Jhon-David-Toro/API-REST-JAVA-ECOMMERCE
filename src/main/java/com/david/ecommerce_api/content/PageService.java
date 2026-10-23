package com.david.ecommerce_api.content;

import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.text.Slugs;
import com.david.ecommerce_api.common.validation.Guard;
import com.david.ecommerce_api.content.PageDtos.PageRequest;
import com.david.ecommerce_api.content.PageDtos.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageService {

    private final StaticPageRepository repository;

    @Transactional(readOnly = true)
    public List<PageResponse> listPublished() {
        return repository.findByPublishedTrueOrderByTitleAsc().stream().map(PageResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse getPublished(String slug) {
        return repository.findBySlugAndPublishedTrue(slug)
                .map(PageResponse::from)
                .orElseThrow(() -> new DomainException(ErrorCode.PAGE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PageResponse> listAll() {
        return repository.findAllByOrderByTitleAsc().stream().map(PageResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse get(Long id) {
        return PageResponse.from(find(id));
    }

    @Transactional
    public PageResponse create(PageRequest request) {
        String slug = Slugs.resolve(request.slug(), request.title());

        Guard.isFalse(repository.existsBySlug(slug), ErrorCode.TAXONOMY_SLUG_TAKEN);

        return PageResponse.from(repository.save(new StaticPage(request.title(), slug, request.body(), isPublished(request))));
    }

    @Transactional
    public PageResponse update(Long id, PageRequest request) {
        StaticPage page = find(id);
        String slug = Slugs.resolve(request.slug(), request.title());

        Guard.isFalse(repository.existsBySlugAndIdNot(slug, id), ErrorCode.TAXONOMY_SLUG_TAKEN);

        page.update(request.title(), slug, request.body(), isPublished(request));

        return PageResponse.from(page);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private StaticPage find(Long id) {
        return repository.findById(id).orElseThrow(() -> new DomainException(ErrorCode.PAGE_NOT_FOUND));
    }

    private boolean isPublished(PageRequest request) {
        return request.published() != null && request.published();
    }
}
