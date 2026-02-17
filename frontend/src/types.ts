export interface UserSummary {
  id: string;
  email: string;
  displayName: string;
}

export interface HouseholdSummary {
  id: string;
  name: string;
  inviteCode: string;
  role: "OWNER" | "ADMIN" | "MEMBER";
  colorHex: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: UserSummary;
  households: HouseholdSummary[];
  activeHouseholdId: string | null;
}

export interface MeResponse {
  user: UserSummary;
  households: HouseholdSummary[];
  activeHouseholdId: string | null;
}

export interface PantryItem {
  id: string;
  productName: string;
  currentQuantity: number;
  minimumQuantity: number;
  unit: string;
  expirationDate: string | null;
  category: string;
  belowMinimum: boolean;
  expiresSoon: boolean;
}

export interface PantryItemRequest {
  productName: string;
  currentQuantity: number;
  minimumQuantity: number;
  unit: string;
  expirationDate: string | null;
  category: string;
}

export interface UserNotification {
  id: string;
  type: string;
  title: string;
  body: string;
  scheduledFor: string;
  readAt: string | null;
}

export interface RecipeIngredient {
  id: string;
  productName: string;
  requiredQuantity: number;
  unit: string;
}

export interface Recipe {
  id: string;
  name: string;
  description: string | null;
  steps: string | null;
  ingredients: RecipeIngredient[];
}

export interface RecipeIngredientRequest {
  productName: string;
  requiredQuantity: number;
  unit: string;
}

export interface RecipeRequest {
  name: string;
  description: string;
  steps: string;
  ingredients: RecipeIngredientRequest[];
}

export interface RecipeExecutionShortage {
  productName: string;
  missingQuantity: number;
  unit: string;
}

export interface RecipeExecutionResult {
  executionId: string;
  recipeId: string;
  recipeName: string;
  shortagesCount: number;
  shortages: RecipeExecutionShortage[];
}

export interface ShoppingListItem {
  id: string;
  productName: string;
  quantity: number;
  unit: string;
  category: string;
  sourceType: "MANUAL" | "RECIPE_SHORTAGE" | "PANTRY_MINIMUM_LOW";
  purchased: boolean;
  purchasedAt: string | null;
}

export interface ShoppingListItemRequest {
  productName: string;
  quantity: number;
  unit: string;
  category: string;
}
