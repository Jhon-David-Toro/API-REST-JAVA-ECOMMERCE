package com.david.ecommerce_api.content;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ContentDtos {

    private ContentDtos() {
    }

    public record Banner(
            boolean enabled,
            @NotNull @Size(max = 300) String text,
            @NotNull @Size(max = 80) String linkText,
            @NotNull @Size(max = 300) String linkHref
    ) {
    }

    public record Hero(
            @NotNull @Size(max = 200) String title,
            @NotNull @Size(max = 1000) String paragraph,
            @NotNull @Size(max = 80) String ctaText,
            @NotNull @Size(max = 300) String ctaHref,
            @NotNull @Size(max = 1000) String desktopImage,
            @NotNull @Size(max = 1000) String mobileImage
    ) {
    }

    public record Stat(@Min(0) long endValue, @NotBlank @Size(max = 80) String label) {
    }

    public record MenuItem(@NotBlank @Size(max = 80) String label, @NotBlank @Size(max = 300) String href) {
    }

    public record FooterLink(
            @NotBlank @Size(max = 80) String label,
            @NotBlank @Size(max = 300) String href,
            @Size(max = 1000) String src
    ) {
    }

    public record FooterColumn(
            @NotBlank @Size(max = 80) String title,
            @NotNull @Valid @Size(max = 20) List<FooterLink> links
    ) {
    }

    public record Content(
            @NotNull @Valid Banner banner,
            @NotNull @Valid Hero hero,
            @NotNull @Valid @Size(max = 6) List<Stat> stats,
            @NotNull @Valid @Size(max = 12) List<MenuItem> navbar,
            @NotNull @Valid @Size(max = 12) List<MenuItem> sidebar,
            @NotNull @Size(max = 500) String footerTagline,
            @NotNull @Valid @Size(max = 8) List<FooterColumn> footerColumns
    ) {
    }
}
