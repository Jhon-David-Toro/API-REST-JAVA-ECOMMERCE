package com.david.ecommerce_api.order;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderApiTest extends ApiTestSupport {

    private static final String ADDRESS = """
            {"fullName":"Ana Ruiz","phone":"5551234","street":"Main 1","city":"Oranjestad","country":"Aruba"}""";

    private String idOf(String json) {
        return json.replaceAll("^\\{\"id\":(\\d+).*", "$1");
    }

    private String productPayload(String name, String sku) {
        return """
                {
                  "name": "%s",
                  "price": 120.00,
                  "images": [{"imageUrl": "https://img/%s.jpg", "main": true}],
                  "colors": [{"name": "Navy", "hex": "#1E3A8A"}],
                  "sizes": [{"name": "Small", "value": "Small"}, {"name": "Large", "value": "Large"}],
                  "variants": [
                    {"color": "Navy", "size": "Small", "sku": "%s-S", "stock": 10},
                    {"color": "Navy", "size": "Large", "sku": "%s-L", "stock": 3, "priceOverride": 130.00}
                  ]
                }
                """.formatted(name, sku, sku, sku);
    }

    private String publishedProduct(String name, String sku, String adminToken) throws Exception {
        String id = idOf(send(json(post("/api/admin/products"), productPayload(name, sku)), adminToken)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

        send(post("/api/admin/products/" + id + "/publish"), adminToken).andExpect(status().isOk());

        return id;
    }

    private String order(String productId, String size, int quantity, String extra) {
        return """
                {
                  "items": [{"productId": %s, "color": "Navy", "size": "%s", "quantity": %d}],
                  "address": %s,
                  "paymentMethod": "cash_on_delivery"%s
                }
                """.formatted(productId, size, quantity, ADDRESS, extra);
    }

    private String stockOf(String productId, String adminToken) throws Exception {
        return getAs("/api/admin/products/" + productId, adminToken).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"totalSold\":\\d+,\"stock\":(\\d+).*", "$1");
    }

    @Test
    void requiresASignedInUserToOrder() throws Exception {
        send(json(post("/api/orders"), order("1", "Small", 1, "")), null).andExpect(status().isUnauthorized());
    }

    @Test
    void worksOutPricesAndTotalsOnTheServerAndTakesTheStock() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Order Tee", "ORD", admin);
        String customer = customerToken();

        send(json(post("/api/orders"), order(product, "Small", 2, "")), customer)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value(org.hamcrest.Matchers.startsWith("SH-")))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.paymentMethod").value("cash_on_delivery"))
                .andExpect(jsonPath("$.email").value("ana@shop.test"))
                .andExpect(jsonPath("$.items[0].name").value("Order Tee"))
                .andExpect(jsonPath("$.items[0].price").value(120.00))
                .andExpect(jsonPath("$.items[0].color").value("Navy"))
                .andExpect(jsonPath("$.subtotal").value(240.00))
                .andExpect(jsonPath("$.shipping").value(0))
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.total").value(240.00))
                .andExpect(jsonPath("$.address.city").value("Oranjestad"));

        org.junit.jupiter.api.Assertions.assertEquals("11", stockOf(product, admin));
    }

    @Test
    void chargesShippingBelowTheFreeShippingThresholdAndUsesTheVariantPrice() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Ship Tee", "SHP", admin);
        String customer = customerToken();

        send(json(post("/api/orders"), order(product, "Small", 1, "")), customer)
                .andExpect(jsonPath("$.subtotal").value(120.00))
                .andExpect(jsonPath("$.shipping").value(15.00))
                .andExpect(jsonPath("$.total").value(135.00));

        send(json(post("/api/orders"), order(product, "Large", 1, "")), customer)
                .andExpect(jsonPath("$.items[0].price").value(130.00));
    }

    @Test
    void appliesAPromoCodeAndRejectsAnUnknownOne() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Promo Tee", "PRM", admin);
        String customer = customerToken();

        send(json(post("/api/orders"), order(product, "Small", 1, ",\"promoCode\":\"shop20\"")), customer)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discount").value(24.00))
                .andExpect(jsonPath("$.total").value(111.00));

        send(json(post("/api/orders"), order(product, "Small", 1, ",\"promoCode\":\"NOPE\"")), customer)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMO_NOT_FOUND"));
    }

    @Test
    void refusesWhatCannotBeBought() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Limited Tee", "LTD", admin);
        String draft = idOf(send(json(post("/api/admin/products"), productPayload("Draft Tee", "DRT")), admin)
                .andReturn().getResponse().getContentAsString());
        String customer = customerToken();

        send(json(post("/api/orders"), order(product, "Large", 4, "")), customer)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOCK_INSUFFICIENT"));
        send(json(post("/api/orders"), order(draft, "Small", 1, "")), customer)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_PRODUCT_UNAVAILABLE"));
        send(json(post("/api/orders"), order(product, "XL", 1, "")), customer)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_PRODUCT_UNAVAILABLE"));
        send(json(post("/api/orders"), "{\"items\":[],\"address\":" + ADDRESS + ",\"paymentMethod\":\"cash_on_delivery\"}"), customer)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        org.junit.jupiter.api.Assertions.assertEquals("13", stockOf(product, admin));
    }

    @Test
    void customersOnlySeeTheirOwnOrders() throws Exception {
        String product = publishedProduct("Mine Tee", "MIN", adminToken());
        String ana = customerToken();
        String luis = register("Luis", "luis@shop.test", "secret1");
        String id = idOf(send(json(post("/api/orders"), order(product, "Small", 1, "")), ana).andReturn().getResponse().getContentAsString());

        getAs("/api/orders", ana).andExpect(jsonPath("$.length()").value(1));
        getAs("/api/orders", luis).andExpect(jsonPath("$.length()").value(0));
        getAs("/api/orders/" + id, ana).andExpect(status().isOk());
        getAs("/api/orders/" + id, luis).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        getAs("/api/orders/" + id, adminToken()).andExpect(status().isOk());
    }

    @Test
    void cancellingPutsTheStockBackAndCannotBeRepeated() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Cancel Tee", "CNL", admin);
        String customer = customerToken();
        String id = idOf(send(json(post("/api/orders"), order(product, "Small", 3, "")), customer).andReturn().getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals("10", stockOf(product, admin));

        send(post("/api/orders/" + id + "/cancel"), customer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"));
        org.junit.jupiter.api.Assertions.assertEquals("13", stockOf(product, admin));

        send(post("/api/orders/" + id + "/cancel"), customer)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_INVALID_STATUS_TRANSITION"));
    }

    @Test
    void administratorsMoveOrdersAlongTheFlow() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("Flow Tee", "FLW", admin);
        String id = idOf(send(json(post("/api/orders"), order(product, "Small", 1, "")), customerToken()).andReturn().getResponse().getContentAsString());
        String url = "/api/admin/orders/" + id + "/status";

        send(json(patch(url), "{\"status\":\"shipped\"}"), admin)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_INVALID_STATUS_TRANSITION"));

        for (String next : new String[]{"paid", "shipped", "delivered"}) {
            send(json(patch(url), "{\"status\":\"" + next + "\"}"), admin)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(next));
        }

        send(json(patch(url), "{\"status\":\"cancelled\"}"), admin).andExpect(status().isConflict());
        send(json(patch(url), "{\"status\":\"bogus\"}"), admin).andExpect(status().isBadRequest());
    }

    @Test
    void administratorsListAndFilterEveryOrder() throws Exception {
        String admin = adminToken();
        String product = publishedProduct("List Tee", "LST", admin);
        String customer = customerToken();
        send(json(post("/api/orders"), order(product, "Small", 1, "")), customer);
        String second = idOf(send(json(post("/api/orders"), order(product, "Small", 1, "")), customer).andReturn().getResponse().getContentAsString());
        send(json(patch("/api/admin/orders/" + second + "/status"), "{\"status\":\"paid\"}"), admin);

        getAs("/api/admin/orders", admin).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(Integer.parseInt(second)));
        getAs("/api/admin/orders?status=paid", admin).andExpect(jsonPath("$.length()").value(1));
        getAs("/api/admin/orders?q=ana", admin).andExpect(jsonPath("$.length()").value(2));
        getAs("/api/admin/orders?q=zzz", admin).andExpect(jsonPath("$.length()").value(0));
        getAs("/api/admin/orders?status=bogus", admin).andExpect(status().isBadRequest());
        getAs("/api/admin/orders", customer).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/orders")).andExpect(status().isUnauthorized());
    }
}
