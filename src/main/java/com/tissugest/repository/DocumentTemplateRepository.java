package com.tissugest.repository;

import com.tissugest.entity.DocumentTemplate;
import com.tissugest.entity.enums.DocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    /** Shop-specific template first, fallback to global default */
    @Query("SELECT t FROM DocumentTemplate t WHERE (t.shop.id = :shopId OR t.shop IS NULL) AND t.docType = :docType ORDER BY t.shop.id DESC NULLS LAST")
    List<DocumentTemplate> findByShopAndType(Long shopId, DocType docType);

    List<DocumentTemplate> findByShopIdIsNull();

    List<DocumentTemplate> findByShopId(Long shopId);

    default Optional<DocumentTemplate> findBestTemplate(Long shopId, DocType docType) {
        List<DocumentTemplate> templates = findByShopAndType(shopId, docType);
        return templates.isEmpty() ? Optional.empty() : Optional.of(templates.get(0));
    }
}
