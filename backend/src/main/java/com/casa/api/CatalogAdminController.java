package com.casa.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.catalog.CatalogCategoryRequest;
import com.casa.api.dto.catalog.CatalogCategoryResponse;
import com.casa.api.dto.catalog.CatalogProductRequest;
import com.casa.api.dto.catalog.CatalogProductResponse;
import com.casa.api.dto.catalog.CatalogUnitRequest;
import com.casa.api.dto.catalog.CatalogUnitResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.CatalogAdminService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/admin/catalog")
public class CatalogAdminController {

    private final CatalogAdminService catalogAdminService;

    public CatalogAdminController(CatalogAdminService catalogAdminService) {
        this.catalogAdminService = catalogAdminService;
    }

    @GetMapping("/units")
    public List<CatalogUnitResponse> listUnits(Authentication authentication, @PathVariable UUID householdId) {
        return catalogAdminService.listUnits(currentUserId(authentication), householdId);
    }

    @PostMapping("/units")
    public CatalogUnitResponse createUnit(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody CatalogUnitRequest request
    ) {
        return catalogAdminService.createUnit(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/units/{unitId}")
    public CatalogUnitResponse updateUnit(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID unitId,
        @Valid @RequestBody CatalogUnitRequest request
    ) {
        return catalogAdminService.updateUnit(currentUserId(authentication), householdId, unitId, request);
    }

    @DeleteMapping("/units/{unitId}")
    public void deleteUnit(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID unitId
    ) {
        catalogAdminService.deleteUnit(currentUserId(authentication), householdId, unitId);
    }

    @GetMapping("/categories")
    public List<CatalogCategoryResponse> listCategories(Authentication authentication, @PathVariable UUID householdId) {
        return catalogAdminService.listCategories(currentUserId(authentication), householdId);
    }

    @PostMapping("/categories")
    public CatalogCategoryResponse createCategory(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody CatalogCategoryRequest request
    ) {
        return catalogAdminService.createCategory(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/categories/{categoryId}")
    public CatalogCategoryResponse updateCategory(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID categoryId,
        @Valid @RequestBody CatalogCategoryRequest request
    ) {
        return catalogAdminService.updateCategory(currentUserId(authentication), householdId, categoryId, request);
    }

    @DeleteMapping("/categories/{categoryId}")
    public void deleteCategory(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID categoryId
    ) {
        catalogAdminService.deleteCategory(currentUserId(authentication), householdId, categoryId);
    }

    @GetMapping("/products")
    public List<CatalogProductResponse> listProducts(Authentication authentication, @PathVariable UUID householdId) {
        return catalogAdminService.listProducts(currentUserId(authentication), householdId);
    }

    @PostMapping("/products")
    public CatalogProductResponse createProduct(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody CatalogProductRequest request
    ) {
        return catalogAdminService.createProduct(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/products/{productId}")
    public CatalogProductResponse updateProduct(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID productId,
        @Valid @RequestBody CatalogProductRequest request
    ) {
        return catalogAdminService.updateProduct(currentUserId(authentication), householdId, productId, request);
    }

    @DeleteMapping("/products/{productId}")
    public void deleteProduct(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID productId
    ) {
        catalogAdminService.deleteProduct(currentUserId(authentication), householdId, productId);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
