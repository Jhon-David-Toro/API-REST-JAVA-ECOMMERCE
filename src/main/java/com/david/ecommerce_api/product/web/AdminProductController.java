package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.common.dto.PageResponse;
import com.david.ecommerce_api.product.domain.ProductStatus;
import com.david.ecommerce_api.product.service.AdminProductService;
import com.david.ecommerce_api.product.web.AdminProductDtos.ProductRequest;
import com.david.ecommerce_api.product.web.AdminProductDtos.Response;
import com.david.ecommerce_api.product.web.AdminProductDtos.Summary;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Validated
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public PageResponse<Summary> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return adminProductService.list(q, status, page, size);
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        return adminProductService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody ProductRequest request) {
        return adminProductService.create(request);
    }

    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return adminProductService.update(id, request);
    }

    @PostMapping("/{id}/publish")
    public Response publish(@PathVariable Long id) {
        return adminProductService.publish(id);
    }

    @PostMapping("/{id}/unpublish")
    public Response unpublish(@PathVariable Long id) {
        return adminProductService.unpublish(id);
    }

    @PostMapping("/{id}/archive")
    public Response archive(@PathVariable Long id) {
        return adminProductService.archive(id);
    }

    @PostMapping("/{id}/restore")
    public Response restore(@PathVariable Long id) {
        return adminProductService.restore(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adminProductService.delete(id);
    }
}
