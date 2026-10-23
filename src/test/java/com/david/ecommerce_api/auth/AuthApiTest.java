package com.david.ecommerce_api.auth;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends ApiTestSupport {

    @Test
    void registersACustomerAndReturnsATokenWithTheUser() throws Exception {
        String body = "{\"name\":\"Ana\",\"email\":\"Ana@Shop.test\",\"password\":\"secret1\"}";

        mvc.perform(json(post("/api/auth/register"), body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("ana@shop.test"))
                .andExpect(jsonPath("$.user.role").value("customer"))
                .andExpect(jsonPath("$.user.blocked").value(false))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void rejectsADuplicateEmail() throws Exception {
        register("Ana", "ana@shop.test", "secret1");

        String body = "{\"name\":\"Other\",\"email\":\"ANA@shop.test\",\"password\":\"secret2\"}";

        mvc.perform(json(post("/api/auth/register"), body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_EMAIL_TAKEN"));
    }

    @Test
    void rejectsInvalidRegistrationData() throws Exception {
        String body = "{\"name\":\"\",\"email\":\"nope\",\"password\":\"123\"}";

        mvc.perform(json(post("/api/auth/register"), body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields").isNotEmpty());
    }

    @Test
    void logsInWithValidCredentialsAndRejectsWrongOnes() throws Exception {
        register("Ana", "ana@shop.test", "secret1");

        mvc.perform(json(post("/api/auth/login"), credentials("ana@shop.test", "secret1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(json(post("/api/auth/login"), credentials("ana@shop.test", "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void requiresATokenToReadTheCurrentUser() throws Exception {
        getAs("/api/auth/me", null).andExpect(status().isUnauthorized());
        getAs("/api/auth/me", "not-a-token").andExpect(status().isUnauthorized());

        getAs("/api/auth/me", customerToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@shop.test"));
    }

    @Test
    void updatesTheProfileAndProtectsTheEmail() throws Exception {
        String token = customerToken();
        register("Luis", "luis@shop.test", "secret1");

        send(json(put("/api/auth/profile"), "{\"name\":\"Ana Maria\",\"email\":\"ana2@shop.test\"}"), token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Maria"))
                .andExpect(jsonPath("$.email").value("ana2@shop.test"));

        send(json(put("/api/auth/profile"), "{\"name\":\"Ana\",\"email\":\"luis@shop.test\"}"), token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_EMAIL_TAKEN"));
    }

    @Test
    void changesThePasswordOnlyWithTheCurrentOne() throws Exception {
        String token = customerToken();

        send(json(put("/api/auth/password"), "{\"current\":\"wrong1\",\"next\":\"newsecret\"}"), token)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_WRONG_PASSWORD"));

        send(json(put("/api/auth/password"), "{\"current\":\"secret1\",\"next\":\"newsecret\"}"), token)
                .andExpect(status().isNoContent());

        mvc.perform(json(post("/api/auth/login"), credentials("ana@shop.test", "newsecret")))
                .andExpect(status().isOk());
    }
}
