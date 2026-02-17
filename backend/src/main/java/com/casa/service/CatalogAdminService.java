package com.casa.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.catalog.CatalogCategoryRequest;
import com.casa.api.dto.catalog.CatalogCategoryResponse;
import com.casa.api.dto.catalog.CatalogProductRequest;
import com.casa.api.dto.catalog.CatalogProductResponse;
import com.casa.api.dto.catalog.CatalogUnitRequest;
import com.casa.api.dto.catalog.CatalogUnitResponse;
import com.casa.domain.catalog.CatalogCategory;
import com.casa.domain.catalog.CatalogProduct;
import com.casa.domain.catalog.CatalogUnit;
import com.casa.domain.household.Household;
import com.casa.repository.CatalogCategoryRepository;
import com.casa.repository.CatalogProductRepository;
import com.casa.repository.CatalogUnitRepository;
import com.casa.repository.HouseholdRepository;

@Service
public class CatalogAdminService {

    private final CatalogUnitRepository catalogUnitRepository;
    private final CatalogCategoryRepository catalogCategoryRepository;
    private final CatalogProductRepository catalogProductRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdService householdService;

    public CatalogAdminService(
        CatalogUnitRepository catalogUnitRepository,
        CatalogCategoryRepository catalogCategoryRepository,
        CatalogProductRepository catalogProductRepository,
        HouseholdRepository householdRepository,
        HouseholdService householdService
    ) {
        this.catalogUnitRepository = catalogUnitRepository;
        this.catalogCategoryRepository = catalogCategoryRepository;
        this.catalogProductRepository = catalogProductRepository;
        this.householdRepository = householdRepository;
        this.householdService = householdService;
    }

    @Transactional(readOnly = true)
    public List<CatalogUnitResponse> listUnits(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);
        return catalogUnitRepository.findByHouseholdIdOrderByLabelAsc(householdId)
            .stream()
            .map(this::toUnitResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogCategoryResponse> listCategories(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);
        return catalogCategoryRepository.findByHouseholdIdOrderByNameAsc(householdId)
            .stream()
            .map(this::toCategoryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogProductResponse> listProducts(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);
        return catalogProductRepository.findByHouseholdIdOrderByNameAsc(householdId)
            .stream()
            .map(this::toProductResponse)
            .toList();
    }

    @Transactional
    public CatalogUnitResponse createUnit(UUID userId, UUID householdId, CatalogUnitRequest request) {
        householdService.requireAdminOrOwner(userId, householdId);

        String code = normalizeCode(request.code());
        if (catalogUnitRepository.existsByHouseholdIdAndCodeIgnoreCase(householdId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad ya existe");
        }

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        CatalogUnit unit = new CatalogUnit();
        unit.setHousehold(household);
        unit.setCode(code);
        unit.setLabel(request.label().trim());
        unit.setActive(request.active());
        unit.setCreatedByUserId(userId);
        catalogUnitRepository.save(unit);

        return toUnitResponse(unit);
    }

    @Transactional
    public CatalogUnitResponse updateUnit(UUID userId, UUID householdId, UUID unitId, CatalogUnitRequest request) {
        householdService.requireAdminOrOwner(userId, householdId);

        CatalogUnit unit = catalogUnitRepository.findByIdAndHouseholdId(unitId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));

        String code = normalizeCode(request.code());
        catalogUnitRepository.findByHouseholdIdAndCodeIgnoreCase(householdId, code)
            .filter(existing -> !existing.getId().equals(unitId))
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad ya existe");
            });

        unit.setCode(code);
        unit.setLabel(request.label().trim());
        unit.setActive(request.active());
        catalogUnitRepository.save(unit);

        return toUnitResponse(unit);
    }

    @Transactional
    public void deleteUnit(UUID userId, UUID householdId, UUID unitId) {
        householdService.requireAdminOrOwner(userId, householdId);

        CatalogUnit unit = catalogUnitRepository.findByIdAndHouseholdId(unitId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));

        catalogUnitRepository.delete(unit);
    }

    @Transactional
    public CatalogCategoryResponse createCategory(UUID userId, UUID householdId, CatalogCategoryRequest request) {
        householdService.requireAdminOrOwner(userId, householdId);

        String name = normalizeName(request.name());
        if (catalogCategoryRepository.existsByHouseholdIdAndNameIgnoreCase(householdId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoria ya existe");
        }

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        CatalogCategory category = new CatalogCategory();
        category.setHousehold(household);
        category.setName(name);
        category.setActive(request.active());
        category.setCreatedByUserId(userId);
        catalogCategoryRepository.save(category);

        return toCategoryResponse(category);
    }

    @Transactional
    public CatalogCategoryResponse updateCategory(UUID userId, UUID householdId, UUID categoryId, CatalogCategoryRequest request) {
        householdService.requireAdminOrOwner(userId, householdId);

        CatalogCategory category = catalogCategoryRepository.findByIdAndHouseholdId(categoryId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));

        String name = normalizeName(request.name());
        catalogCategoryRepository.findByHouseholdIdAndNameIgnoreCase(householdId, name)
            .filter(existing -> !existing.getId().equals(categoryId))
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoria ya existe");
            });

        category.setName(name);
        category.setActive(request.active());
        catalogCategoryRepository.save(category);

        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID userId, UUID householdId, UUID categoryId) {
        householdService.requireAdminOrOwner(userId, householdId);

        CatalogCategory category = catalogCategoryRepository.findByIdAndHouseholdId(categoryId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));

        catalogCategoryRepository.delete(category);
    }

    @Transactional
    public CatalogProductResponse createProduct(UUID userId, UUID householdId, CatalogProductRequest request) {
        householdService.requireAdminOrOwner(userId, householdId);

        String name = normalizeProductName(request.name());
        if (catalogProductRepository.existsByHouseholdIdAndNameIgnoreCase(householdId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya existe");
        }

        validateReferences(householdId, request.defaultUnitCode(), request.defaultCategoryName());

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        CatalogProduct product = new CatalogProduct();
        product.setHousehold(household);
        product.setName(name);
        product.setDefaultUnitCode(normalizeNullableCode(request.defaultUnitCode()));
        product.setDefaultCategoryName(normalizeNullableName(request.defaultCategoryName()));
        product.setActive(request.active());
        product.setCreatedByUserId(userId);
        catalogProductRepository.save(product);

        return toProductResponse(product);
    }

    @Transactional
    public CatalogProductResponse updateProduct(
        UUID userId,
        UUID householdId,
        UUID productId,
        CatalogProductRequest request
    ) {
        householdService.requireAdminOrOwner(userId, householdId);

        CatalogProduct product = catalogProductRepository.findByIdAndHouseholdId(productId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        String name = normalizeProductName(request.name());
        catalogProductRepository.findByHouseholdIdAndNameIgnoreCase(householdId, name)
            .filter(existing -> !existing.getId().equals(productId))
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya existe");
            });

        validateReferences(householdId, request.defaultUnitCode(), request.defaultCategoryName());

        product.setName(name);
        product.setDefaultUnitCode(normalizeNullableCode(request.defaultUnitCode()));
        product.setDefaultCategoryName(normalizeNullableName(request.defaultCategoryName()));
        product.setActive(request.active());
        catalogProductRepository.save(product);

        return toProductResponse(product);
    }

    @Transactional
    public void deleteProduct(UUID userId, UUID householdId, UUID productId) {
        householdService.requireAdminOrOwner(userId, householdId);

        CatalogProduct product = catalogProductRepository.findByIdAndHouseholdId(productId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        catalogProductRepository.delete(product);
    }

    private CatalogUnitResponse toUnitResponse(CatalogUnit unit) {
        return new CatalogUnitResponse(unit.getId(), unit.getCode(), unit.getLabel(), unit.isActive());
    }

    private CatalogCategoryResponse toCategoryResponse(CatalogCategory category) {
        return new CatalogCategoryResponse(category.getId(), category.getName(), category.isActive());
    }

    private CatalogProductResponse toProductResponse(CatalogProduct product) {
        return new CatalogProductResponse(
            product.getId(),
            product.getName(),
            product.getDefaultUnitCode(),
            product.getDefaultCategoryName(),
            product.isActive()
        );
    }

    private void validateReferences(UUID householdId, String defaultUnitCode, String defaultCategoryName) {
        String unitCode = normalizeNullableCode(defaultUnitCode);
        if (unitCode != null && catalogUnitRepository.findByHouseholdIdAndCodeIgnoreCase(householdId, unitCode).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La unidad por defecto no existe en el maestro");
        }

        String categoryName = normalizeNullableName(defaultCategoryName);
        if (categoryName != null && catalogCategoryRepository.findByHouseholdIdAndNameIgnoreCase(householdId, categoryName).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoria por defecto no existe en el maestro");
        }
    }

    private String normalizeCode(String rawCode) {
        return rawCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String rawName) {
        return rawName.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeProductName(String rawName) {
        return rawName.trim();
    }

    private String normalizeNullableCode(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeNullableName(String rawName) {
        if (rawName == null) {
            return null;
        }
        String normalized = rawName.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
