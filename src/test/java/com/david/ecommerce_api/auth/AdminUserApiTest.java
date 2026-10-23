package com.david.ecommerce_api.auth;

import com.david.ecommerce_api.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUserApiTest extends ApiTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    void keepsTheAdminAreaForAdministrators() throws Exception {
        getAs("/api/admin/users", null).andExpect(status().isUnauthorized());
        getAs("/api/admin/users", customerToken()).andExpect(status().isForbidden());
        getAs("/api/admin/users", adminToken()).andExpect(status().isOk());
    }

    @Test
    void listsUsersWithoutPasswords() throws Exception {
        customerToken();

        getAs("/api/admin/users", adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void blockedCustomersCannotLogInAndTheirTokenStopsWorking() throws Exception {
        String customer = customerToken();
        Long id = userRepository.findByEmail("ana@shop.test").orElseThrow().getId();

        send(json(patch("/api/admin/users/" + id + "/blocked"), "{\"blocked\":true}"), adminToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(true));

        getAs("/api/auth/me", customer).andExpect(status().isUnauthorized());

        mvc.perform(json(post("/api/auth/login"), credentials("ana@shop.test", "secret1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_BLOCKED"));
    }

    @Test
    void administratorsCannotBeBlocked() throws Exception {
        Long id = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow().getId();

        send(json(patch("/api/admin/users/" + id + "/blocked"), "{\"blocked\":true}"), adminToken())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_ADMIN_CANNOT_BE_BLOCKED"));
    }
}
