package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductDetail;
import com.david.ecommerce_api.product.domain.ProductImage;
import com.david.ecommerce_api.product.web.StoreProductDtos.Color;
import com.david.ecommerce_api.product.web.StoreProductDtos.Company;
import com.david.ecommerce_api.product.web.StoreProductDtos.Description;
import com.david.ecommerce_api.product.web.StoreProductDtos.Faq;
import com.david.ecommerce_api.product.web.StoreProductDtos.StoreProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class StoreProductMapper {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public StoreProduct toStoreProduct(Product product) {
        boolean onSale = product.isOnSale();
        BigDecimal regularPrice = onSale ? product.getCompareAtPrice() : product.getPrice();

        return new StoreProduct(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getBrand() == null ? "" : product.getBrand().getName(),
                product.getCategory() == null ? "" : product.getCategory().getName(),
                product.getDressStyle() == null ? "" : product.getDressStyle(),
                regularPrice,
                onSale ? product.getPrice() : null,
                onSale ? discountPercent(regularPrice, product.getPrice()) : 0,
                product.getRatingAverage(),
                product.getRatingCount(),
                product.getThumbnailUrl() == null ? "" : product.getThumbnailUrl(),
                extraImages(product),
                product.getColors().stream()
                        .filter(color -> color.isActive())
                        .map(color -> new Color(color.getName(), color.getHex()))
                        .toList(),
                product.getSizes().stream()
                        .filter(size -> size.isActive())
                        .map(size -> size.getName())
                        .toList(),
                product.getAvailableStock(),
                product.getStatus().name().toLowerCase(),
                product.getCreatedAt().toEpochMilli(),
                toDescription(product),
                faqs(product)
        );
    }

    private int discountPercent(BigDecimal regular, BigDecimal current) {
        return regular.subtract(current)
                .multiply(ONE_HUNDRED)
                .divide(regular, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private List<String> extraImages(Product product) {
        return product.getImages().stream()
                .filter(image -> !image.getImageUrl().equals(product.getThumbnailUrl()))
                .map(ProductImage::getImageUrl)
                .toList();
    }

    private Description toDescription(Product product) {
        ProductDetail detail = product.getDetail();
        List<String> features = product.getFeatures().stream().map(feature -> feature.getText()).toList();

        if (detail == null) {
            return new Description("", features, "", new Company("", ""), "");
        }

        return new Description(
                orEmpty(detail.getOverview()),
                features,
                orEmpty(detail.getOrigin()),
                new Company(orEmpty(detail.getCompanyName()), orEmpty(detail.getAboutCompany())),
                orEmpty(detail.getCareInstructions())
        );
    }

    private List<Faq> faqs(Product product) {
        List<Faq> faqs = new java.util.ArrayList<>();
        int index = 0;

        for (var faq : product.getFaqs()) {
            if (faq.isActive()) {
                faqs.add(new Faq("faq-" + product.getId() + "-" + index++, faq.getQuestion(), faq.getAnswer()));
            }
        }

        return faqs;
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
