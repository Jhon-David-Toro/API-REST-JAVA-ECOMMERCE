package com.david.ecommerce_api.product;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_faqs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductFaq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String question;

    @Column(nullable = false, length = 2000)
    private String answer;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private ProductFaq(String question, String answer, int displayOrder) {
        Guard.notBlank(question, ErrorCode.VALIDATION_ERROR, "faq_question");
        Guard.notBlank(answer, ErrorCode.VALIDATION_ERROR, "faq_answer");

        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
    }

    public static ProductFaq create(String question, String answer, int displayOrder) {
        return new ProductFaq(question, answer, displayOrder);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    void attachTo(Product product) {
        this.product = product;
    }
}