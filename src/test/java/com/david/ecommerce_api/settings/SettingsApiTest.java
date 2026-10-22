package com.david.ecommerce_api.settings;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettingsApiTest extends ApiTestSupport {

    private static final String VALID = """
            {
              "storeName": "My Shop", "contactEmail": "help@myshop.test", "contactPhone": "123",
              "currency": "awg", "shippingFee": 10, "freeShippingOver": 150, "lowStockThreshold": 5,
              "promoCodes": [{"code": "WELCOME10", "percent": 10}]
            }
            """;

    @Test
    void publicSettingsHideThePromoCodes() throws Exception {
        mvc.perform(get("/api/public/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("SHOP.CO"))
                .andExpect(jsonPath("$.shippingFee").value(15))
                .andExpect(jsonPath("$.freeShippingOver").value(200))
                .andExpect(jsonPath("$.promoCodes").doesNotExist())
                .andExpect(jsonPath("$.lowStockThreshold").doesNotExist());
    }

    @Test
    void administratorsReadAndChangeTheSettings() throws Exception {
        String admin = adminToken();

        getAs("/api/admin/settings", admin)
                .andExpect(jsonPath("$.lowStockThreshold").value(10))
                .andExpect(jsonPath("$.promoCodes[0].code").value("SHOP20"));

        send(json(put("/api/admin/settings"), VALID), admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("AWG"))
                .andExpect(jsonPath("$.promoCodes.length()").value(1));

        mvc.perform(get("/api/public/settings")).andExpect(jsonPath("$.storeName").value("My Shop"));
    }

    @Test
    void validatesTheSettings() throws Exception {
        String admin = adminToken();

        send(json(put("/api/admin/settings"), VALID.replace("help@myshop.test", "nope")), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        send(json(put("/api/admin/settings"), VALID.replace("\"percent\": 10", "\"percent\": 150")), admin)
                .andExpect(status().isBadRequest());
        send(json(put("/api/admin/settings"), VALID.replace("}]\n}", "},{\"code\": \"welcome10\", \"percent\": 5}]\n}")), admin)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROMO_DUPLICATED"));
    }

    @Test
    void checksAPromoCodeAndKeepsTheAdminAreaClosed() throws Exception {
        mvc.perform(get("/api/public/promo-codes/shop20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percent").value(20));
        mvc.perform(get("/api/public/promo-codes/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMO_NOT_FOUND"));

        getAs("/api/admin/settings", customerToken()).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/settings")).andExpect(status().isUnauthorized());
    }
}
