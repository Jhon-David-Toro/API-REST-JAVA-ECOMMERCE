package com.david.ecommerce_api.category;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaxonomyApiTest extends ApiTestSupport {

    @Test
    void createsACategoryWithAGeneratedSlug() throws Exception {
        send(json(post("/api/admin/categories"), "{\"name\":\"T-Shirts & Tops\",\"description\":\"Light\"}"), adminToken())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("t-shirts-tops"))
                .andExpect(jsonPath("$.visible").value(true));
    }

    @Test
    void rejectsADuplicateSlug() throws Exception {
        String token = adminToken();
        send(json(post("/api/admin/categories"), "{\"name\":\"Jeans\"}"), token).andExpect(status().isCreated());

        send(json(post("/api/admin/categories"), "{\"name\":\"Other\",\"slug\":\"jeans\"}"), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TAXONOMY_SLUG_TAKEN"));
    }

    @Test
    void publicListHidesHiddenCategories() throws Exception {
        String token = adminToken();
        send(json(post("/api/admin/categories"), "{\"name\":\"Visible one\"}"), token);
        send(json(post("/api/admin/categories"), "{\"name\":\"Hidden one\",\"visible\":false}"), token);

        mvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Visible one"));

        getAs("/api/admin/categories", token).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updatesAndDeletesACategory() throws Exception {
        String token = adminToken();
        String created = send(json(post("/api/admin/categories"), "{\"name\":\"Hoodie\"}"), token)
                .andReturn().getResponse().getContentAsString();
        String id = created.replaceAll(".*\"id\":(\\d+).*", "$1");

        send(json(put("/api/admin/categories/" + id), "{\"name\":\"Hoodies\"}"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("hoodies"));

        send(delete("/api/admin/categories/" + id), token).andExpect(status().isNoContent());
        getAs("/api/admin/categories/" + id, token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void customersCannotWriteTaxonomy() throws Exception {
        String token = customerToken();

        send(json(post("/api/admin/categories"), "{\"name\":\"X\"}"), token).andExpect(status().isForbidden());
        send(json(post("/api/admin/brands"), "{\"name\":\"X\"}"), token).andExpect(status().isForbidden());
    }

    @Test
    void managesBrandsLikeCategories() throws Exception {
        String token = adminToken();

        send(json(post("/api/admin/brands"), "{\"name\":\"Calvin Klein\",\"logo\":\"https://x/logo.svg\"}"), token)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("calvin-klein"))
                .andExpect(jsonPath("$.logo").value("https://x/logo.svg"));

        send(json(post("/api/admin/brands"), "{\"name\":\"Hidden\",\"visible\":false}"), token);

        mvc.perform(get("/api/public/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
