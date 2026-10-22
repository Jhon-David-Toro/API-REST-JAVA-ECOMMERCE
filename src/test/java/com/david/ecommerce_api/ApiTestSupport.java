package com.david.ecommerce_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTestSupport {

    protected static final String ADMIN_EMAIL = "admin@shop.test";
    protected static final String ADMIN_PASSWORD = "admin123";

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    protected MockMvc mvc;

    protected ResultActions send(MockHttpServletRequestBuilder request, String token) throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return mvc.perform(request);
    }

    protected MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected String login(String email, String password) throws Exception {
        return tokenFrom(mvc.perform(json(post("/api/auth/login"), credentials(email, password))));
    }

    protected String register(String name, String email, String password) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        return tokenFrom(mvc.perform(json(post("/api/auth/register"), body)));
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected String customerToken() throws Exception {
        return register("Ana Customer", "ana@shop.test", "secret1");
    }

    protected String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    protected ResultActions getAs(String url, String token) throws Exception {
        return send(get(url), token);
    }

    private String tokenFrom(ResultActions actions) throws UnsupportedEncodingException {
        String content = actions.andReturn().getResponse().getContentAsString();
        Matcher matcher = TOKEN.matcher(content);

        if (!matcher.find()) {
            throw new IllegalStateException("No token in response: " + content);
        }

        return matcher.group(1);
    }
}
