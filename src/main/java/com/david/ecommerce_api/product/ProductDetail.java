package com.david.ecommerce_api.product;

import com.david.ecommerce_api.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 3000)
    private String overview;

    @Column(length = 1000)
    private String origin;

    @Column(name = "company_name", length = 180)
    private String companyName;

    @Column(name = "about_company", length = 3000)
    private String aboutCompany;

    @Column(name = "care_instructions", length = 2000)
    private String careInstructions;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    public static ProductDetail create(
            String overview,
            String origin,
            String companyName,
            String aboutCompany,
            String careInstructions
    ) {
        ProductDetail detail = new ProductDetail();
        detail.overview = overview;
        detail.origin = origin;
        detail.companyName = companyName;
        detail.aboutCompany = aboutCompany;
        detail.careInstructions = careInstructions;
        return detail;
    }

    void attachTo(Product product) {
        this.product = product;
    }
}