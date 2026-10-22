package com.david.ecommerce_api.product;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminProductApiTest extends ApiTestSupport {

    private static final String BASE = "/api/admin/products";

    private String product(String name, String sku) {
        return product(name, sku, "\"S\",\"M\"");
    }

    private String product(String name, String sku, String sizeValues) {
        StringBuilder sizeJson = new StringBuilder("[");
        StringBuilder variantJson = new StringBuilder("[");
        String[] values = sizeValues.replace("\"", "").split(",");

        for (int index = 0; index < values.length; index++) {
            String separator = index == 0 ? "" : ",";
            sizeJson.append(separator).append("{\"name\":\"").append(values[index]).append("\",\"value\":\"").append(values[index]).append("\"}");
            variantJson.append(separator)
                    .append("{\"color\":\"Navy\",\"size\":\"").append(values[index])
                    .append("\",\"sku\":\"").append(sku).append("-").append(values[index])
                    .append("\",\"stock\":").append(5 + index).append("}");
        }

        sizeJson.append("]");
        variantJson.append("]");

        return """
                {
                  "name": "%s",
                  "shortDescription": "Soft cotton",
                  "price": 120.00,
                  "compareAtPrice": 150.00,
                  "dressStyle": "Casual",
                  "images": [{"imageUrl": "https://img/%s-1.jpg", "alt": "front"}, {"imageUrl": "https://img/%s-2.jpg", "main": true}],
                  "colors": [{"name": "Navy", "hex": "#1E3A8A"}],
                  "sizes": %s,
                  "variants": %s,
                  "detail": {"overview": "Nice", "origin": "Aruba"},
                  "features": ["Organic cotton", "Pre-shrunk"],
                  "faqs": [{"question": "Machine wash?", "answer": "Yes"}]
                }
                """.formatted(name, sku, sku, sizeJson, variantJson);
    }

    private String createdId(String json) {
        return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
    }

    private String create(String body, String token) throws Exception {
        return send(json(post(BASE), body), token).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void keepsProductAdministrationForAdministrators() throws Exception {
        getAs(BASE, null).andExpect(status().isUnauthorized());
        getAs(BASE, customerToken()).andExpect(status().isForbidden());
    }

    @Test
    void createsADraftWithImagesOptionsVariantsAndContent() throws Exception {
        send(json(post(BASE), product("One Life T-Shirt", "OLT")), adminToken())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("one-life-t-shirt"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.stock").value(11))
                .andExpect(jsonPath("$.thumbnailUrl").value("https://img/OLT-2.jpg"))
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.colors[0].name").value("Navy"))
                .andExpect(jsonPath("$.sizes.length()").value(2))
                .andExpect(jsonPath("$.variants[0].sku").value("OLT-S"))
                .andExpect(jsonPath("$.detail.origin").value("Aruba"))
                .andExpect(jsonPath("$.features[1]").value("Pre-shrunk"))
                .andExpect(jsonPath("$.faqs[0].answer").value("Yes"));
    }

    @Test
    void rejectsInvalidProductData() throws Exception {
        String body = "{\"name\":\"\",\"price\":0,\"colors\":[{\"name\":\"Red\",\"hex\":\"red\"}]}";

        send(json(post(BASE), body), adminToken())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields").isNotEmpty());

        send(json(post(BASE), product("Cheap", "CHP").replace("150.00", "100.00")), adminToken())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRODUCT_INVALID_COMPARE_AT_PRICE"));
    }

    @Test
    void rejectsARepeatedSlugAndAnUnknownCategory() throws Exception {
        String token = adminToken();
        create(product("Jeans", "JNS"), token);

        send(json(post(BASE), product("Jeans", "JN2")), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_SLUG_TAKEN"));

        send(json(post(BASE), product("Other", "OTH").replace("\"dressStyle\"", "\"categoryId\": 999, \"dressStyle\"")), token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void rejectsARepeatedSkuAcrossProducts() throws Exception {
        String token = adminToken();
        create(product("First", "DUP"), token);

        send(json(post(BASE), product("Second", "DUP")), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_CONFLICT"));
    }

    @Test
    void linksACategoryAndABrand() throws Exception {
        String token = adminToken();
        String category = send(json(post("/api/admin/categories"), "{\"name\":\"T-Shirts\"}"), token)
                .andReturn().getResponse().getContentAsString();
        String brand = send(json(post("/api/admin/brands"), "{\"name\":\"Zara\"}"), token)
                .andReturn().getResponse().getContentAsString();
        String linked = product("Linked", "LNK").replace(
                "\"dressStyle\"",
                "\"categoryId\": " + createdId(category) + ", \"brandId\": " + createdId(brand) + ", \"dressStyle\"");

        send(json(post(BASE), linked), token)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("T-Shirts"))
                .andExpect(jsonPath("$.brandName").value("Zara"))
                .andExpect(jsonPath("$.dressStyle").value("Casual"));
    }

    @Test
    void publishingNeedsAnImageAndAnActiveVariantAndShowsTheProductInTheStore() throws Exception {
        String token = adminToken();
        String empty = create("{\"name\":\"Bare\",\"price\":10}", token);

        send(post(BASE + "/" + createdId(empty) + "/publish"), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_PUBLISHABLE"));

        String id = createdId(create(product("Visible Shirt", "VIS"), token));

        mvc.perform(get("/api/public/products/visible-shirt")).andExpect(status().isNotFound());

        send(post(BASE + "/" + id + "/publish"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").isNumber());

        mvc.perform(get("/api/public/products/visible-shirt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Visible Shirt"))
                .andExpect(jsonPath("$.availableStock").value(11));

        send(post(BASE + "/" + id + "/unpublish"), token).andExpect(jsonPath("$.status").value("DRAFT"));
        mvc.perform(get("/api/public/products/visible-shirt")).andExpect(status().isNotFound());
    }

    @Test
    void updatingKeepsTheVariantsAndHidesTheRemovedOptions() throws Exception {
        String token = adminToken();
        String created = create(product("Hoodie", "HOO"), token);
        String id = createdId(created);

        send(json(put(BASE + "/" + id), product("Hoodie", "HOO", "\"S\"")), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes.length()").value(1))
                .andExpect(jsonPath("$.variants.length()").value(1))
                .andExpect(jsonPath("$.stock").value(5));

        send(json(put(BASE + "/" + id), product("Hoodie", "HOO", "\"S\",\"M\"")), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes.length()").value(2))
                .andExpect(jsonPath("$.variants.length()").value(2))
                .andExpect(jsonPath("$.stock").value(11))
                .andExpect(jsonPath("$.features.length()").value(2));
    }

    @Test
    void listsWithSearchAndStatusFilter() throws Exception {
        String token = adminToken();
        create(product("Red Dress", "RED"), token);
        String blue = createdId(create(product("Blue Dress", "BLU"), token));
        send(post(BASE + "/" + blue + "/publish"), token).andExpect(status().isOk());

        getAs(BASE + "?q=dress", token).andExpect(jsonPath("$.totalElements").value(2));
        getAs(BASE + "?q=red", token).andExpect(jsonPath("$.content[0].name").value("Red Dress"));
        getAs(BASE + "?status=PUBLISHED", token)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Blue Dress"));
    }

    @Test
    void deletesDraftsButNotPublishedProducts() throws Exception {
        String token = adminToken();
        String id = createdId(create(product("Temp", "TMP"), token));
        send(post(BASE + "/" + id + "/publish"), token).andExpect(status().isOk());

        send(delete(BASE + "/" + id), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_DELETABLE"));

        send(post(BASE + "/" + id + "/archive"), token).andExpect(jsonPath("$.status").value("ARCHIVED"));
        send(delete(BASE + "/" + id), token).andExpect(status().isNoContent());
        getAs(BASE + "/" + id, token).andExpect(status().isNotFound());
    }
}
