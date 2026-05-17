package com.tissugest.service;

import com.tissugest.entity.enums.DocType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentNumberService {

    private final JdbcTemplate jdbcTemplate;

    private static final Map<DocType, String> PREFIXES = Map.of(
            DocType.RECEIPT, "REC",
            DocType.INVOICE, "FAC",
            DocType.QUOTE, "DEV",
            DocType.PROFORMA, "PRO"
    );

    /**
     * Generate next document number: PREFIX-YEAR-NNNN (e.g. FAC-2026-0001)
     */
    @Transactional
    public String nextNumber(Long shopId, DocType docType) {
        String prefix = PREFIXES.getOrDefault(docType, "DOC");
        int year = LocalDate.now().getYear();

        // Upsert and get next number atomically
        int nextNum = jdbcTemplate.queryForObject(
            "INSERT INTO document_sequences (shop_id, doc_type, prefix, current_number) " +
            "VALUES (?, ?::document_type, ?, 1) " +
            "ON CONFLICT (shop_id, doc_type) DO UPDATE SET current_number = document_sequences.current_number + 1 " +
            "RETURNING current_number",
            Integer.class,
            shopId, docType.name(), prefix
        );

        return String.format("%s-%d-%04d", prefix, year, nextNum);
    }
}
