package com.david.ecommerce_api.brand;

import com.david.ecommerce_api.brand.BrandDtos.BrandRequest;
import com.david.ecommerce_api.brand.BrandDtos.BrandResponse;
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
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandResponse> listVisible() {
        return brandRepository.findByVisibleTrueOrderByNameAsc().stream().map(BrandResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> listAll() {
        return brandRepository.findAllByOrderByNameAsc().stream().map(BrandResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse get(Long id) {
        return BrandResponse.from(find(id));
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        String slug = Slugs.resolve(request.slug(), request.name());

        Guard.isFalse(brandRepository.existsBySlug(slug), ErrorCode.TAXONOMY_SLUG_TAKEN);

        Brand brand = brandRepository.save(new Brand(request.name(), slug, request.logo(), isVisible(request)));

        return BrandResponse.from(brand);
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = find(id);
        String slug = Slugs.resolve(request.slug(), request.name());

        Guard.isFalse(brandRepository.existsBySlugAndIdNot(slug, id), ErrorCode.TAXONOMY_SLUG_TAKEN);

        brand.update(request.name(), slug, request.logo(), isVisible(request));

        return BrandResponse.from(brand);
    }

    @Transactional
    public void delete(Long id) {
        brandRepository.delete(find(id));
    }

    private Brand find(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.BRAND_NOT_FOUND));
    }

    private boolean isVisible(BrandRequest request) {
        return request.visible() == null || request.visible();
    }
}
