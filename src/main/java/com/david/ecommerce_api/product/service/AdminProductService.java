package com.david.ecommerce_api.product.service;

import com.david.ecommerce_api.brand.Brand;
import com.david.ecommerce_api.brand.BrandRepository;
import com.david.ecommerce_api.category.Category;
import com.david.ecommerce_api.category.CategoryRepository;
import com.david.ecommerce_api.common.dto.PageResponse;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.text.Slugs;
import com.david.ecommerce_api.common.validation.Guard;
import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductColor;
import com.david.ecommerce_api.product.domain.ProductSize;
import com.david.ecommerce_api.product.domain.ProductStatus;
import com.david.ecommerce_api.product.domain.ProductVariant;
import com.david.ecommerce_api.product.repository.ProductRepository;
import com.david.ecommerce_api.product.web.AdminProductDtos.ColorInput;
import com.david.ecommerce_api.product.web.AdminProductDtos.DetailInput;
import com.david.ecommerce_api.product.web.AdminProductDtos.FaqInput;
import com.david.ecommerce_api.product.web.AdminProductDtos.ImageInput;
import com.david.ecommerce_api.product.web.AdminProductDtos.ProductRequest;
import com.david.ecommerce_api.product.web.AdminProductDtos.Response;
import com.david.ecommerce_api.product.web.AdminProductDtos.SizeInput;
import com.david.ecommerce_api.product.web.AdminProductDtos.Summary;
import com.david.ecommerce_api.product.web.AdminProductDtos.VariantInput;
import com.david.ecommerce_api.product.web.AdminProductMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AdminProductMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<Summary> list(String query, ProductStatus status, int page, int size) {
        Specification<Product> specification = (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";

                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("slug")), pattern)
                ));
            }

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return PageResponse.from(productRepository.findAll(specification, pageable).map(mapper::toSummary));
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public Response create(ProductRequest request) {
        String slug = Slugs.resolve(request.slug(), request.name());

        Guard.isFalse(productRepository.existsBySlug(slug), ErrorCode.PRODUCT_SLUG_TAKEN);

        Product product = Product.createDraft(
                request.name(),
                slug,
                request.shortDescription(),
                request.price(),
                request.compareAtPrice()
        );

        applyContent(product, request);

        return mapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public Response update(Long id, ProductRequest request) {
        Product product = find(id);
        String slug = Slugs.resolve(request.slug(), request.name());

        Guard.isFalse(productRepository.existsBySlugAndIdNot(slug, id), ErrorCode.PRODUCT_SLUG_TAKEN);

        product.rename(request.name());
        product.changeSlug(slug);
        product.updateShortDescription(request.shortDescription());
        product.changePricing(request.price(), request.compareAtPrice());

        applyContent(product, request);

        return mapper.toResponse(productRepository.saveAndFlush(product));
    }

    @Transactional
    public Response publish(Long id) {
        return change(id, Product::publish);
    }

    @Transactional
    public Response unpublish(Long id) {
        return change(id, Product::unpublish);
    }

    @Transactional
    public Response archive(Long id) {
        return change(id, Product::archive);
    }

    @Transactional
    public Response restore(Long id) {
        return change(id, Product::restoreToDraft);
    }

    @Transactional
    public void delete(Long id) {
        Product product = find(id);

        Guard.isFalse(product.isPublished(), ErrorCode.PRODUCT_NOT_DELETABLE);

        productRepository.delete(product);
    }

    private Response change(Long id, Consumer<Product> transition) {
        Product product = find(id);
        transition.accept(product);

        return mapper.toResponse(product);
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void applyContent(Product product, ProductRequest request) {
        product.classify(findCategory(request.categoryId()), findBrand(request.brandId()), request.dressStyle());

        replaceImages(product, orEmpty(request.images()));
        syncOptions(product, orEmpty(request.colors()), orEmpty(request.sizes()), orEmpty(request.variants()));
        replaceDetail(product, request.detail());
        replaceFeatures(product, orEmpty(request.features()));
        replaceFaqs(product, orEmpty(request.faqs()));
    }

    private void replaceImages(Product product, List<ImageInput> images) {
        product.clearImages();

        boolean hasMain = images.stream().anyMatch(image -> Boolean.TRUE.equals(image.main()));

        for (int index = 0; index < images.size(); index++) {
            ImageInput image = images.get(index);
            boolean main = hasMain ? Boolean.TRUE.equals(image.main()) : index == 0;
            String publicId = image.publicId() == null || image.publicId().isBlank() ? image.imageUrl() : image.publicId();

            product.addImage(image.imageUrl(), publicId, image.alt(), index, main, image.width(), image.height());
        }
    }

    private void syncOptions(
            Product product,
            List<ColorInput> colors,
            List<SizeInput> sizes,
            List<VariantInput> variants
    ) {
        for (int index = 0; index < colors.size(); index++) {
            product.upsertColor(colors.get(index).name(), colors.get(index).hex(), index);
        }

        for (int index = 0; index < sizes.size(); index++) {
            product.upsertSize(sizes.get(index).name(), sizes.get(index).value(), index);
        }

        List<ProductVariant> kept = new ArrayList<>();

        for (VariantInput variant : variants) {
            ProductColor color = product.findColor(variant.color())
                    .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_COLOR_NOT_FOUND));
            ProductSize size = product.findSize(variant.size())
                    .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_SIZE_NOT_FOUND));

            kept.add(product.upsertVariant(
                    color,
                    size,
                    variant.sku(),
                    variant.stock(),
                    variant.priceOverride(),
                    variant.active() == null || variant.active()
            ));
        }

        product.keepOnly(
                colors.stream().map(ColorInput::name).toList(),
                sizes.stream().map(SizeInput::value).toList(),
                kept
        );
    }

    private void replaceDetail(Product product, DetailInput detail) {
        if (detail == null) {
            return;
        }

        product.updateDetail(
                detail.overview(),
                detail.origin(),
                detail.companyName(),
                detail.aboutCompany(),
                detail.careInstructions()
        );
    }

    private void replaceFeatures(Product product, List<String> features) {
        product.clearFeatures();

        for (int index = 0; index < features.size(); index++) {
            product.addFeature(features.get(index), index);
        }
    }

    private void replaceFaqs(Product product, List<FaqInput> faqs) {
        product.clearFaqs();

        for (int index = 0; index < faqs.size(); index++) {
            product.addFaq(faqs.get(index).question(), faqs.get(index).answer(), index);
        }
    }

    private Category findCategory(Long id) {
        if (id == null) {
            return null;
        }

        return categoryRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Brand findBrand(Long id) {
        if (id == null) {
            return null;
        }

        return brandRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.BRAND_NOT_FOUND));
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
