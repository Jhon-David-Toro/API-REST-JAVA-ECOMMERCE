package com.david.ecommerce_api.content;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PageApiTest extends ApiTestSupport {

    private String idOf(String json) {
        return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
    }

    private String create(String body, String token) throws Exception {
        return send(json(post("/api/admin/pages"), body), token).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void createsAPageWithAGeneratedSlug() throws Exception {
        send(json(post("/api/admin/pages"), "{\"title\":\"Shipping and returns\",\"body\":\"Two days\",\"published\":true}"), adminToken())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("shipping-and-returns"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.updatedAt").isNumber());
    }

    @Test
    void onlyPublishedPagesArePublic() throws Exception {
        String admin = adminToken();
        create("{\"title\":\"About\",\"body\":\"Us\",\"published\":true}", admin);
        create("{\"title\":\"Secret\",\"body\":\"Draft\"}", admin);

        mvc.perform(get("/api/public/pages")).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].slug").value("about"));
        mvc.perform(get("/api/public/pages/about")).andExpect(status().isOk()).andExpect(jsonPath("$.body").value("Us"));
        mvc.perform(get("/api/public/pages/secret")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PAGE_NOT_FOUND"));
        getAs("/api/admin/pages", admin).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void rejectsARepeatedSlug() throws Exception {
        String admin = adminToken();
        create("{\"title\":\"About\"}", admin);

        send(json(post("/api/admin/pages"), "{\"title\":\"Other\",\"slug\":\"about\"}"), admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TAXONOMY_SLUG_TAKEN"));
    }

    @Test
    void updatesAndDeletesAPage() throws Exception {
        String admin = adminToken();
        String id = idOf(create("{\"title\":\"Terms\",\"body\":\"v1\"}", admin));

        send(json(put("/api/admin/pages/" + id), "{\"title\":\"Terms and conditions\",\"body\":\"v2\",\"published\":true}"), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("terms-and-conditions"))
                .andExpect(jsonPath("$.body").value("v2"));

        send(delete("/api/admin/pages/" + id), admin).andExpect(status().isNoContent());
        getAs("/api/admin/pages/" + id, admin).andExpect(status().isNotFound());
    }

    @Test
    void customersCannotManagePages() throws Exception {
        send(json(post("/api/admin/pages"), "{\"title\":\"X\"}"), customerToken()).andExpect(status().isForbidden());
    }
}
