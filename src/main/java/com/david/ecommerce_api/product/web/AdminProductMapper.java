package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductDetail;
import com.david.ecommerce_api.product.web.AdminProductDtos.ColorOutput;
import com.david.ecommerce_api.product.web.AdminProductDtos.DetailOutput;
import com.david.ecommerce_api.product.web.AdminProductDtos.FaqOutput;
import com.david.ecommerce_api.product.web.AdminProductDtos.ImageOutput;
import com.david.ecommerce_api.product.web.AdminProductDtos.Response;
import com.david.ecommerce_api.product.web.AdminProductDtos.SizeOutput;
import com.david.ecommerce_api.product.web.AdminProductDtos.Summary;
import com.david.ecommerce_api.product.web.AdminProductDtos.VariantOutput;
import org.springframework.stereotype.Component;

@Component
public class AdminProductMapper {

    public Summary toSummary(Product product) {
        return new Summary(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getThumbnailUrl(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getStatus(),
                product.getAvailableStock(),
                product.getCategory() == null ? null : product.getCategory().getId(),
                product.getCategory() == null ? null : product.getCategory().getName(),
                product.getBrand() == null ? null : product.getBrand().getId(),
                product.getBrand() == null ? null : product.getBrand().getName(),
                product.getCreatedAt().toEpochMilli()
        );
    }

    public Response toResponse(Product product) {
        return new Response(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getStatus(),
                product.getCategory() == null ? null : product.getCategory().getId(),
                product.getCategory() == null ? null : product.getCategory().getName(),
                product.getBrand() == null ? null : product.getBrand().getId(),
                product.getBrand() == null ? null : product.getBrand().getName(),
                product.getDressStyle(),
                product.getRatingAverage(),
                product.getRatingCount(),
                product.getTotalSold(),
                product.getAvailableStock(),
                product.getThumbnailUrl(),
                product.getImages().stream()
                        .map(image -> new ImageOutput(
                                image.getId(),
                                image.getImageUrl(),
                                image.getPublicId(),
                                image.getAlt(),
                                image.isMainImage(),
                                image.getDisplayOrder(),
                                image.getWidth(),
                                image.getHeight()
                        ))
                        .toList(),
                product.getColors().stream()
                        .filter(color -> color.isActive())
                        .map(color -> new ColorOutput(color.getId(), color.getName(), color.getHex()))
                        .toList(),
                product.getSizes().stream()
                        .filter(size -> size.isActive())
                        .map(size -> new SizeOutput(size.getId(), size.getName(), size.getValue()))
                        .toList(),
                product.getVariants().stream()
                        .filter(variant -> variant.getColor().isActive() && variant.getSize().isActive())
                        .map(variant -> new VariantOutput(
                                variant.getId(),
                                variant.getColor().getName(),
                                variant.getSize().getValue(),
                                variant.getSku(),
                                variant.getStock(),
                                variant.getPriceOverride(),
                                variant.isActive()
                        ))
                        .toList(),
                toDetail(product.getDetail()),
                product.getFeatures().stream().map(feature -> feature.getText()).toList(),
                product.getFaqs().stream()
                        .map(faq -> new FaqOutput(faq.getQuestion(), faq.getAnswer()))
                        .toList(),
                product.getCreatedAt().toEpochMilli(),
                product.getPublishedAt() == null ? null : product.getPublishedAt().toEpochMilli()
        );
    }

    private DetailOutput toDetail(ProductDetail detail) {
        if (detail == null) {
            return null;
        }

        return new DetailOutput(
                detail.getOverview(),
                detail.getOrigin(),
                detail.getCompanyName(),
                detail.getAboutCompany(),
                detail.getCareInstructions()
        );
    }
}
