package com.david.ecommerce_api.content;

import com.david.ecommerce_api.content.ContentDtos.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final SiteContentRepository repository;
    private final JsonMapper jsonMapper;

    /**
     * Gives the saved content, or nothing when the store still uses the defaults of the storefront.
     */
    @Transactional(readOnly = true)
    public Optional<Content> find() {
        return repository.findById(SiteContentDocument.SINGLETON_ID)
                .map(document -> jsonMapper.readValue(document.getJson(), Content.class));
    }

    @Transactional
    public Content save(Content content) {
        String json = jsonMapper.writeValueAsString(content);

        repository.findById(SiteContentDocument.SINGLETON_ID).ifPresentOrElse(
                document -> document.replace(json),
                () -> repository.save(new SiteContentDocument(json))
        );

        return content;
    }

    @Transactional
    public void reset() {
        repository.deleteById(SiteContentDocument.SINGLETON_ID);
    }
}
