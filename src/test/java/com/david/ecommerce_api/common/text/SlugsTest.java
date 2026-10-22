package com.david.ecommerce_api.common.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugsTest {

    @Test
    void buildsLowercaseDashSeparatedSlugsWithoutAccentsOrSymbols() {
        assertEquals("camisetas-de-algodon", Slugs.from("  Camisetas de Algodón!  "));
        assertEquals("t-shirts-tops", Slugs.from("T-Shirts & Tops"));
    }

    @Test
    void prefersTheGivenSlugOverTheName() {
        assertEquals("custom-slug", Slugs.resolve("Custom Slug", "Name"));
        assertEquals("the-name", Slugs.resolve("  ", "The Name"));
        assertEquals("the-name", Slugs.resolve(null, "The Name"));
    }
}
