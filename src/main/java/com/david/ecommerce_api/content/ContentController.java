package com.david.ecommerce_api.content;

import com.david.ecommerce_api.content.ContentDtos.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    /**
     * Answers the saved content, or an empty object so the storefront keeps its defaults.
     */
    @GetMapping("/public/content")
    public Object content() {
        return contentService.find().<Object>map(content -> content).orElseGet(Map::of);
    }

    @PutMapping("/admin/content")
    public Content save(@Valid @RequestBody Content content) {
        return contentService.save(content);
    }

    @DeleteMapping("/admin/content")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset() {
        contentService.reset();
    }
}
