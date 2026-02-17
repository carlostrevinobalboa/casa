import { api } from "./api";
import type { CatalogCategory, CatalogProduct, CatalogUnit } from "../types";

export interface HouseholdCatalog {
  units: CatalogUnit[];
  categories: CatalogCategory[];
  products: CatalogProduct[];
}

export const fetchHouseholdCatalog = async (householdId: string): Promise<HouseholdCatalog> => {
  const [unitsResponse, categoriesResponse, productsResponse] = await Promise.all([
    api.get<CatalogUnit[]>(`/api/households/${householdId}/catalog/units`),
    api.get<CatalogCategory[]>(`/api/households/${householdId}/catalog/categories`),
    api.get<CatalogProduct[]>(`/api/households/${householdId}/catalog/products`)
  ]);

  return {
    units: unitsResponse.data,
    categories: categoriesResponse.data,
    products: productsResponse.data
  };
};

export const findProductInCatalog = (products: CatalogProduct[], productName: string): CatalogProduct | null => {
  const normalizedName = productName.trim().toLocaleLowerCase();
  if (!normalizedName) {
    return null;
  }

  return products.find((product) => product.name.trim().toLocaleLowerCase() === normalizedName) ?? null;
};
