package com.david.ecommerce_api.common.text;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugs {

    private Slugs() {
    }

    /**
     * Turns a text into a lowercase, dash-separated slug without accents or symbols.
     */
    public static String from(String text) {
        String ascii = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return ascii.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
    }

    /**
     * Uses the given slug when there is one, otherwise builds it from the name.
     */
    public static String resolve(String slug, String name) {
        return from(slug == null || slug.isBlank() ? name : slug);
    }
}
