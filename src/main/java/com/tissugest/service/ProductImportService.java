package com.tissugest.service;

import com.tissugest.entity.Category;
import com.tissugest.entity.Product;
import com.tissugest.entity.Shop;
import com.tissugest.entity.enums.ProductUnit;
import com.tissugest.entity.enums.StockReferenceType;
import com.tissugest.repository.CategoryRepository;
import com.tissugest.repository.ProductRepository;
import com.tissugest.security.SecurityHelper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockService stockService;
    private final SecurityHelper securityHelper;

    private static final Map<String, ProductUnit> UNIT_MAP = Map.of(
            "piece", ProductUnit.PIECE,
            "pièce", ProductUnit.PIECE,
            "pcs", ProductUnit.PIECE,
            "metre", ProductUnit.METER,
            "mètre", ProductUnit.METER,
            "m", ProductUnit.METER,
            "lot", ProductUnit.LOT,
            "rouleau", ProductUnit.ROLL,
            "rlx", ProductUnit.ROLL
    );

    @Data
    @Builder
    public static class ImportResult {
        private int totalLines;
        private int imported;
        private int errors;
        private List<LineError> lineErrors;
    }

    @Data
    @Builder
    public static class LineError {
        private int line;
        private String value;
        private String error;
    }

    @Transactional
    public ImportResult importCsv(MultipartFile file) {
        Shop shop = securityHelper.getCurrentShop();
        List<LineError> lineErrors = new ArrayList<>();
        int imported = 0;
        int lineNum = 0;

        // Cache categories by name (case-insensitive)
        Map<String, Category> categoryCache = new HashMap<>();
        categoryRepository.findByShopId(shop.getId())
                .forEach(c -> categoryCache.put(c.getName().toLowerCase().trim(), c));

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ImportResult.builder()
                        .totalLines(0).imported(0).errors(1)
                        .lineErrors(List.of(LineError.builder().line(1).value("").error("Fichier vide").build()))
                        .build();
            }

            // Skip BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            // Validate header
            String[] headers = parseCsvLine(headerLine);
            if (headers.length < 2) {
                return ImportResult.builder()
                        .totalLines(0).imported(0).errors(1)
                        .lineErrors(List.of(LineError.builder().line(1).value(headerLine)
                                .error("En-tête invalide. Attendu: nom,categorie,prix_achat,prix_vente,unite,quantite").build()))
                        .build();
            }

            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String[] cols = parseCsvLine(line);
                    String nom = cols.length > 0 ? cols[0].trim() : "";
                    String categorie = cols.length > 1 ? cols[1].trim() : "";
                    String prixAchatStr = cols.length > 2 ? cols[2].trim() : "0";
                    String prixVenteStr = cols.length > 3 ? cols[3].trim() : "0";
                    String uniteStr = cols.length > 4 ? cols[4].trim().toLowerCase() : "piece";
                    String quantiteStr = cols.length > 5 ? cols[5].trim() : "0";

                    // Validate name
                    if (nom.isEmpty()) {
                        lineErrors.add(LineError.builder().line(lineNum + 1).value(line).error("Nom obligatoire").build());
                        continue;
                    }

                    // Parse prices
                    long prixAchat = parseLong(prixAchatStr, 0L);
                    long prixVente = parseLong(prixVenteStr, 0L);

                    // Parse unit
                    ProductUnit unit = UNIT_MAP.getOrDefault(uniteStr, ProductUnit.PIECE);

                    // Parse quantity
                    BigDecimal quantite = parseBigDecimal(quantiteStr, BigDecimal.ZERO);

                    // Get or create category
                    Category category = null;
                    if (!categorie.isEmpty()) {
                        String catKey = categorie.toLowerCase();
                        category = categoryCache.get(catKey);
                        if (category == null) {
                            category = Category.builder()
                                    .shop(shop)
                                    .name(categorie)
                                    .build();
                            category = categoryRepository.save(category);
                            categoryCache.put(catKey, category);
                            log.debug("Catégorie créée: {}", categorie);
                        }
                    }

                    // Create product
                    Product product = Product.builder()
                            .shop(shop)
                            .name(nom)
                            .purchasePrice(prixAchat)
                            .sellingPrice(prixVente)
                            .unit(unit)
                            .category(category)
                            .build();
                    product = productRepository.save(product);

                    // Initialize stock
                    stockService.initializeStock(product);
                    if (quantite.compareTo(BigDecimal.ZERO) > 0) {
                        stockService.addStock(product, shop, quantite, StockReferenceType.MANUAL, null);
                    }

                    imported++;
                } catch (Exception e) {
                    lineErrors.add(LineError.builder()
                            .line(lineNum + 1).value(line).error(e.getMessage()).build());
                }
            }
        } catch (Exception e) {
            log.error("Erreur import CSV", e);
            lineErrors.add(LineError.builder().line(0).value("").error("Erreur lecture fichier: " + e.getMessage()).build());
        }

        log.info("Import CSV: {} importés, {} erreurs sur {} lignes", imported, lineErrors.size(), lineNum);
        return ImportResult.builder()
                .totalLines(lineNum)
                .imported(imported)
                .errors(lineErrors.size())
                .lineErrors(lineErrors)
                .build();
    }

    /**
     * Génère le contenu du template CSV.
     */
    public String generateTemplate() {
        return "nom,categorie,prix_achat,prix_vente,unite,quantite\n"
                + "Bazin Riche Allemand,Bazin,5000,8000,piece,50\n"
                + "Wax Hollandais,Wax,3000,5000,metre,100\n"
                + "Fil à coudre blanc,Accessoires,500,1000,lot,20\n";
    }

    private String[] parseCsvLine(String line) {
        // Handle basic CSV (comma or semicolon separated)
        String separator = line.contains(";") ? ";" : ",";
        return line.split(separator, -1);
    }

    private long parseLong(String s, long defaultVal) {
        try {
            return Long.parseLong(s.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private BigDecimal parseBigDecimal(String s, BigDecimal defaultVal) {
        try {
            return new BigDecimal(s.replaceAll("[^0-9.,\\-]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
