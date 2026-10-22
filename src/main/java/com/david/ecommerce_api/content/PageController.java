package com.david.ecommerce_api.content;

import com.david.ecommerce_api.content.PageDtos.PageRequest;
import com.david.ecommerce_api.content.PageDtos.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping("/public/pages")
    public List<PageResponse> published() {
        return pageService.listPublished();
    }

    @GetMapping("/public/pages/{slug}")
    public PageResponse bySlug(@PathVariable String slug) {
        return pageService.getPublished(slug);
    }

    @GetMapping("/admin/pages")
    public List<PageResponse> all() {
        return pageService.listAll();
    }

    @GetMapping("/admin/pages/{id}")
    public PageResponse get(@PathVariable Long id) {
        return pageService.get(id);
    }

    @PostMapping("/admin/pages")
    @ResponseStatus(HttpStatus.CREATED)
    public PageResponse create(@Valid @RequestBody PageRequest request) {
        return pageService.create(request);
    }

    @PutMapping("/admin/pages/{id}")
    public PageResponse update(@PathVariable Long id, @Valid @RequestBody PageRequest request) {
        return pageService.update(id, request);
    }

    @DeleteMapping("/admin/pages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pageService.delete(id);
    }
}
