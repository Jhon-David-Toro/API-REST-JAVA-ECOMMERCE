package com.david.ecommerce_api.content;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContentApiTest extends ApiTestSupport {

    private static final String CONTENT = """
            {
              "banner": {"enabled": true, "text": "20% off", "linkText": "Join", "linkHref": "/register"},
              "hero": {"title": "HELLO", "paragraph": "Welcome", "ctaText": "Shop", "ctaHref": "/products",
                       "desktopImage": "https://img/d.png", "mobileImage": "https://img/m.svg"},
              "stats": [{"endValue": 200, "label": "Brands"}],
              "navbar": [{"label": "Shop", "href": "/products"}],
              "sidebar": [{"label": "Shop", "href": "/products"}],
              "footerTagline": "Clothes you love",
              "footerColumns": [{"title": "Company", "links": [{"label": "About", "href": "/p/about"}]}]
            }
            """;

    @Test
    void answersAnEmptyObjectUntilContentIsSaved() throws Exception {
        mvc.perform(get("/api/public/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hero").doesNotExist());
    }

    @Test
    void administratorsSaveAndResetTheContent() throws Exception {
        String admin = adminToken();

        send(json(put("/api/admin/content"), CONTENT), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hero.title").value("HELLO"));

        mvc.perform(get("/api/public/content"))
                .andExpect(jsonPath("$.banner.text").value("20% off"))
                .andExpect(jsonPath("$.stats[0].endValue").value(200))
                .andExpect(jsonPath("$.footerColumns[0].links[0].href").value("/p/about"));

        send(json(put("/api/admin/content"), CONTENT.replace("HELLO", "BYE")), admin).andExpect(status().isOk());
        mvc.perform(get("/api/public/content")).andExpect(jsonPath("$.hero.title").value("BYE"));

        send(delete("/api/admin/content"), admin).andExpect(status().isNoContent());
        mvc.perform(get("/api/public/content")).andExpect(jsonPath("$.hero").doesNotExist());
    }

    @Test
    void validatesTheContentAndKeepsTheAdminAreaClosed() throws Exception {
        send(json(put("/api/admin/content"), CONTENT.replace("\"hero\"", "\"heroo\"")), adminToken())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        send(json(put("/api/admin/content"), CONTENT.replace("\"label\": \"Brands\"", "\"label\": \"\"")), adminToken())
                .andExpect(status().isBadRequest());

        send(json(put("/api/admin/content"), CONTENT), customerToken()).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/content")).andExpect(status().isUnauthorized());
    }
}
