package com.david.ecommerce_api.brand;

import com.david.ecommerce_api.brand.BrandDtos.BrandRequest;
import com.david.ecommerce_api.brand.BrandDtos.BrandResponse;
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
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/public/brands")
    public List<BrandResponse> listVisible() {
        return brandService.listVisible();
    }

    @GetMapping("/admin/brands")
    public List<BrandResponse> listAll() {
        return brandService.listAll();
    }

    @GetMapping("/admin/brands/{id}")
    public BrandResponse get(@PathVariable Long id) {
        return brandService.get(id);
    }

    @PostMapping("/admin/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@Valid @RequestBody BrandRequest request) {
        return brandService.create(request);
    }

    @PutMapping("/admin/brands/{id}")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return brandService.update(id, request);
    }

    @DeleteMapping("/admin/brands/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        brandService.delete(id);
    }
}
