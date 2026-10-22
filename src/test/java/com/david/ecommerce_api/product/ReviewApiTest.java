package com.david.ecommerce_api.product;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewApiTest extends ApiTestSupport {

    private String idOf(String json) {
        return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
    }

    private String publishedProduct(String name, String adminToken) throws Exception {
        String body = """
                {
                  "name": "%s", "price": 50.00,
                  "images": [{"imageUrl": "https://img/%s.jpg", "main": true}],
                  "colors": [{"name": "Navy", "hex": "#1E3A8A"}],
                  "sizes": [{"name": "Small", "value": "Small"}],
                  "variants": [{"color": "Navy", "size": "Small", "sku": "%s-S", "stock": 10}]
                }
                """.formatted(name, name, name.toUpperCase());
        String id = idOf(send(json(post("/api/admin/products"), body), adminToken).andReturn().getResponse().getContentAsString());
        send(post("/api/admin/products/" + id + "/publish"), adminToken).andExpect(status().isOk());

        return id;
    }

    private String review(String productId, int rating, String text, String token) throws Exception {
        return idOf(send(json(post("/api/products/" + productId + "/reviews"), "{\"rating\":" + rating + ",\"text\":\"" + text + "\"}"), token)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private String productJson(String productId, String adminToken) throws Exception {
        return getAs("/api/admin/products/" + productId, adminToken).andReturn().getResponse().getContentAsString();
    }

    @Test
    void reviewsWaitForModerationAndAreNotPublicUntilApproved() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Review Tee", admin);
        String customer = customerToken();

        send(json(post("/api/products/" + product + "/reviews"), "{\"rating\":5,\"text\":\"Great\"}"), customer)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.author").value("Ana Customer"))
                .andExpect(jsonPath("$.productId").value(Integer.parseInt(product)))
                .andExpect(jsonPath("$.verified").value(false));

        mvc.perform(get("/api/public/catalog/" + product + "/reviews")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void needsASignedInUserAndValidData() throws Exception {
        String product = publishedProduct("Valid Tee", adminToken());
        String ana = customerToken();

        send(json(post("/api/products/" + product + "/reviews"), "{\"rating\":5,\"text\":\"x\"}"), null).andExpect(status().isUnauthorized());
        send(json(post("/api/products/" + product + "/reviews"), "{\"rating\":9,\"text\":\"\"}"), ana)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        send(json(post("/api/products/999/reviews"), "{\"rating\":4,\"text\":\"ok\"}"), ana).andExpect(status().isNotFound());
    }

    @Test
    void approvingUpdatesTheRatingOfTheProductAndRejectingTakesItBack() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Rated Tee", admin);
        String ana = customerToken();
        String luis = register("Luis", "luis@shop.test", "secret1");
        String first = review(product, 5, "Great", ana);
        String second = review(product, 4, "Good", luis);

        send(json(patch("/api/admin/reviews/" + first + "/status"), "{\"status\":\"approved\"}"), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));
        send(json(patch("/api/admin/reviews/" + second + "/status"), "{\"status\":\"approved\"}"), admin);

        String json = productJson(product, admin);
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"ratingAverage\":4.5,\"ratingCount\":2"), json);
        mvc.perform(get("/api/public/catalog/" + product + "/reviews")).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].text").value("Good"));

        send(json(patch("/api/admin/reviews/" + second + "/status"), "{\"status\":\"rejected\"}"), admin)
                .andExpect(jsonPath("$.status").value("rejected"));
        org.junit.jupiter.api.Assertions.assertTrue(productJson(product, admin).contains("\"ratingAverage\":5.0,\"ratingCount\":1"));
        mvc.perform(get("/api/public/catalog/" + product + "/reviews")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deletingAReviewRefreshesTheRating() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Gone Tee", admin);
        String id = review(product, 3, "Meh", customerToken());
        send(json(patch("/api/admin/reviews/" + id + "/status"), "{\"status\":\"approved\"}"), admin);
        org.junit.jupiter.api.Assertions.assertTrue(productJson(product, admin).contains("\"ratingCount\":1"));

        send(delete("/api/admin/reviews/" + id), admin).andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertTrue(productJson(product, admin).contains("\"ratingAverage\":0.0,\"ratingCount\":0"));
        send(delete("/api/admin/reviews/" + id), admin).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("REVIEW_NOT_FOUND"));
    }

    @Test
    void marksReviewsOfBuyersAsVerified() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Bought Tee", admin);
        String ana = customerToken();
        String order = """
                {"items":[{"productId":%s,"color":"Navy","size":"Small","quantity":1}],
                 "address":{"fullName":"Ana","phone":"1","street":"S","city":"C","country":"AW"},
                 "paymentMethod":"cash_on_delivery"}""".formatted(product);
        send(json(post("/api/orders"), order), ana).andExpect(status().isCreated());

        send(json(post("/api/products/" + product + "/reviews"), "{\"rating\":5,\"text\":\"Bought it\"}"), ana)
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void administratorsListAndFilterReviews() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Admin Tee", admin);
        String ana = customerToken();
        String one = review(product, 5, "One", ana);
        review(product, 2, "Two", ana);
        send(json(patch("/api/admin/reviews/" + one + "/status"), "{\"status\":\"approved\"}"), admin);

        getAs("/api/admin/reviews", admin).andExpect(jsonPath("$.length()").value(2));
        getAs("/api/admin/reviews?status=pending", admin).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].text").value("Two"));
        getAs("/api/admin/reviews?productId=" + product, admin).andExpect(jsonPath("$.length()").value(2));
        getAs("/api/admin/reviews?status=bogus", admin).andExpect(status().isBadRequest());
        getAs("/api/admin/reviews", ana).andExpect(status().isForbidden());
    }
}
