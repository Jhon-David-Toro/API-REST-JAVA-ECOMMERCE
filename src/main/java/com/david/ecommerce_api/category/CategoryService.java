package com.david.ecommerce_api.category;

import com.david.ecommerce_api.category.CategoryDtos.CategoryRequest;
import com.david.ecommerce_api.category.CategoryDtos.CategoryResponse;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.text.Slugs;
import com.david.ecommerce_api.common.validation.Guard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listVisible() {
        return categoryRepository.findByVisibleTrueOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return CategoryResponse.from(find(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = Slugs.resolve(request.slug(), request.name());

        Guard.isFalse(categoryRepository.existsBySlug(slug), ErrorCode.TAXONOMY_SLUG_TAKEN);

        Category category = categoryRepository.save(new Category(
                request.name(),
                slug,
                request.description(),
                request.image(),
                isVisible(request)
        ));

        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = find(id);
        String slug = Slugs.resolve(request.slug(), request.name());

        Guard.isFalse(categoryRepository.existsBySlugAndIdNot(slug, id), ErrorCode.TAXONOMY_SLUG_TAKEN);

        category.update(request.name(), slug, request.description(), request.image(), isVisible(request));

        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(find(id));
    }

    private Category find(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private boolean isVisible(CategoryRequest request) {
        return request.visible() == null || request.visible();
    }
}
