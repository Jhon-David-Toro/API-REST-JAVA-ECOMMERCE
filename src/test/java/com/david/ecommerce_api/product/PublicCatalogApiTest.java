package com.david.ecommerce_api.product;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicCatalogApiTest extends ApiTestSupport {

    private String payload(String name, String sku, String taxonomy) {
        return """
                {
                  "name": "%s",
                  "shortDescription": "Soft cotton",
                  "price": 120.00,
                  "compareAtPrice": 150.00,
                  "dressStyle": "Casual",
                  %s
                  "images": [{"imageUrl": "https://img/%s-main.jpg", "main": true}, {"imageUrl": "https://img/%s-extra.jpg"}],
                  "colors": [{"name": "Navy", "hex": "#1E3A8A"}],
                  "sizes": [{"name": "Small", "value": "Small"}, {"name": "Large", "value": "Large"}],
                  "variants": [
                    {"color": "Navy", "size": "Small", "sku": "%s-S", "stock": 4},
                    {"color": "Navy", "size": "Large", "sku": "%s-L", "stock": 6}
                  ],
                  "detail": {"overview": "Nice", "origin": "Aruba", "companyName": "Thread", "aboutCompany": "We sew", "careInstructions": "Cold wash"},
                  "features": ["Organic cotton"],
                  "faqs": [{"question": "Machine wash?", "answer": "Yes"}]
                }
                """.formatted(name, taxonomy, sku, sku, sku, sku);
    }

    private String idOf(String json) {
        return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
    }

    private String createPublished(String name, String sku, String taxonomy, String token) throws Exception {
        String created = send(json(post("/api/admin/products"), payload(name, sku, taxonomy)), token)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = idOf(created);

        send(post("/api/admin/products/" + id + "/publish"), token).andExpect(status().isOk());

        return id;
    }

    @Test
    void listsPublishedProductsInTheShapeTheStoreNeeds() throws Exception {
        String token = adminToken();
        String category = idOf(send(json(post("/api/admin/categories"), "{\"name\":\"T-Shirts\"}"), token).andReturn().getResponse().getContentAsString());
        String brand = idOf(send(json(post("/api/admin/brands"), "{\"name\":\"Zara\"}"), token).andReturn().getResponse().getContentAsString());

        createPublished("Visible Tee", "VIS", "\"categoryId\": " + category + ", \"brandId\": " + brand + ",", token);
        send(json(post("/api/admin/products"), payload("Draft Tee", "DRF", "")), token).andExpect(status().isCreated());

        mvc.perform(get("/api/public/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Visible Tee"))
                .andExpect(jsonPath("$[0].slug").value("visible-tee"))
                .andExpect(jsonPath("$[0].brand").value("Zara"))
                .andExpect(jsonPath("$[0].category").value("T-Shirts"))
                .andExpect(jsonPath("$[0].dressStyle").value("Casual"))
                .andExpect(jsonPath("$[0].price").value(150.00))
                .andExpect(jsonPath("$[0].discountPrice").value(120.00))
                .andExpect(jsonPath("$[0].discount").value(20))
                .andExpect(jsonPath("$[0].image").value("https://img/VIS-main.jpg"))
                .andExpect(jsonPath("$[0].images[0]").value("https://img/VIS-extra.jpg"))
                .andExpect(jsonPath("$[0].colors[0].hex").value("#1E3A8A"))
                .andExpect(jsonPath("$[0].sizes[1]").value("Large"))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[0].status").value("published"))
                .andExpect(jsonPath("$[0].description.company.name").value("Thread"))
                .andExpect(jsonPath("$[0].description.features[0]").value("Organic cotton"))
                .andExpect(jsonPath("$[0].faqs[0].question").value("Machine wash?"));
    }

    @Test
    void anUnsoldProductShowsNoDiscount() throws Exception {
        String token = adminToken();
        String body = payload("Plain Tee", "PLN", "").replace("\"compareAtPrice\": 150.00,", "");
        String id = idOf(send(json(post("/api/admin/products"), body), token).andReturn().getResponse().getContentAsString());
        send(post("/api/admin/products/" + id + "/publish"), token);

        mvc.perform(get("/api/public/catalog/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(120.00))
                .andExpect(jsonPath("$.discountPrice").doesNotExist())
                .andExpect(jsonPath("$.discount").value(0));
    }

    @Test
    void hidesDraftsAndUnknownProducts() throws Exception {
        String token = adminToken();
        String draft = idOf(send(json(post("/api/admin/products"), payload("Hidden", "HID", "")), token)
                .andReturn().getResponse().getContentAsString());

        mvc.perform(get("/api/public/catalog/" + draft))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
        mvc.perform(get("/api/public/catalog/9999")).andExpect(status().isNotFound());
    }
}
