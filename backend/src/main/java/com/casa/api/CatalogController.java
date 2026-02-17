package com.casa.api;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.catalog.CatalogCategoryResponse;
import com.casa.api.dto.catalog.CatalogProductResponse;
import com.casa.api.dto.catalog.CatalogUnitResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.CatalogAdminService;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/catalog")
public class CatalogController {

    private final CatalogAdminService catalogAdminService;

    public CatalogController(CatalogAdminService catalogAdminService) {
        this.catalogAdminService = catalogAdminService;
    }

    @GetMapping("/units")
    public List<CatalogUnitResponse> listUnits(Authentication authentication, @PathVariable UUID householdId) {
        return catalogAdminService.listUnits(currentUserId(authentication), householdId);
    }

    @GetMapping("/categories")
    public List<CatalogCategoryResponse> listCategories(Authentication authentication, @PathVariable UUID householdId) {
        return catalogAdminService.listCategories(currentUserId(authentication), householdId);
    }

    @GetMapping("/products")
    public List<CatalogProductResponse> listProducts(Authentication authentication, @PathVariable UUID householdId) {
        return catalogAdminService.listProducts(currentUserId(authentication), householdId);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
