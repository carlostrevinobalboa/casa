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

export interface HouseholdMember {
  userId: string;
  displayName: string;
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

export interface NotificationRealtimeEvent {
  householdId: string;
  unreadCount: number;
  notification: UserNotification;
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
  sourceType: "MANUAL" | "RECIPE_SHORTAGE" | "PANTRY_MINIMUM_LOW" | "PET_FOOD_LOW";
  purchased: boolean;
  purchasedAt: string | null;
}

export interface ShoppingListItemRequest {
  productName: string;
  quantity: number;
  unit: string;
  category: string;
}

export interface ShoppingPurchase {
  id: string;
  shoppingListItemId: string;
  productName: string;
  quantity: number;
  unit: string;
  category: string;
  totalPrice: number;
  unitPrice: number | null;
  purchasedByUserId: string;
  purchasedAt: string;
}

export interface CatalogUnit {
  id: string;
  code: string;
  label: string;
  active: boolean;
}

export interface CatalogUnitRequest {
  code: string;
  label: string;
  active: boolean;
}

export interface CatalogCategory {
  id: string;
  name: string;
  active: boolean;
}

export interface CatalogCategoryRequest {
  name: string;
  active: boolean;
}

export interface CatalogProduct {
  id: string;
  name: string;
  defaultUnitCode: string | null;
  defaultCategoryName: string | null;
  active: boolean;
}

export interface CatalogProductRequest {
  name: string;
  defaultUnitCode: string | null;
  defaultCategoryName: string | null;
  active: boolean;
}

export type PetCareType = "VACCINATION" | "GROOMING" | "DEWORMING" | "VET_VISIT" | "OTHER";

export interface Pet {
  id: string;
  name: string;
  type: string;
  chipCode: string | null;
  veterinarian: string | null;
  photoUrl: string | null;
  currentWeightKg: number | null;
  foodName: string | null;
  foodStockQuantity: number | null;
  foodDailyConsumptionQuantity: number | null;
  foodUnit: string | null;
  foodDaysRemaining: number | null;
  foodLow: boolean;
  active: boolean;
}

export interface PetRequest {
  name: string;
  type: string;
  chipCode: string | null;
  veterinarian: string | null;
  photoUrl: string | null;
  currentWeightKg: number | null;
  foodName: string | null;
  foodStockQuantity: number | null;
  foodDailyConsumptionQuantity: number | null;
  foodUnit: string | null;
  active: boolean;
}

export interface PetFeeding {
  id: string;
  foodType: string;
  quantity: number;
  unit: string;
  fedAt: string;
  notes: string | null;
  addedByUserId: string;
}

export interface PetFeedingRequest {
  foodType: string;
  quantity: number;
  unit: string;
  fedAt: string | null;
  notes: string | null;
}

export interface PetWeight {
  id: string;
  weightKg: number;
  recordedAt: string;
  addedByUserId: string;
}

export interface PetWeightRequest {
  weightKg: number;
  recordedAt: string | null;
}

export interface PetCareTask {
  id: string;
  careType: PetCareType;
  description: string | null;
  frequencyDays: number;
  notifyDaysBefore: number;
  lastPerformedAt: string | null;
  nextDueAt: string;
  dueSoon: boolean;
  active: boolean;
}

export interface PetCareTaskRequest {
  careType: PetCareType;
  description: string | null;
  frequencyDays: number;
  notifyDaysBefore: number;
  lastPerformedAt: string | null;
  active: boolean;
}

export type ActivityType = "RUN" | "WALK" | "BIKE" | "PET_WALK";

export interface ActivityPoint {
  id: string;
  sequenceNumber: number;
  latitude: number;
  longitude: number;
  recordedAt: string;
}

export interface ActivityPointRequest {
  latitude: number;
  longitude: number;
  recordedAt: string | null;
}

export interface Activity {
  id: string;
  type: ActivityType;
  performedByUserId: string;
  petId: string | null;
  startedAt: string;
  endedAt: string;
  durationSeconds: number;
  distanceKm: number;
  title: string | null;
  notes: string | null;
  gpsTracked: boolean;
  points: ActivityPoint[];
}

export interface ActivityRequest {
  type: ActivityType;
  performedByUserId: string | null;
  petId: string | null;
  startedAt: string;
  endedAt: string;
  distanceKm: number | null;
  gpsTracked: boolean;
  title: string | null;
  notes: string | null;
  points: ActivityPointRequest[];
}

export interface ActivityDailyStat {
  day: string;
  activitiesCount: number;
  distanceKm: number;
  durationMinutes: number;
}

export interface ActivityWeeklyStats {
  weekStart: string;
  weekEnd: string;
  activitiesCount: number;
  distanceKm: number;
  durationMinutes: number;
  daily: ActivityDailyStat[];
}

export type CalendarEventType = "WORK" | "TRAINING" | "TASK" | "PERSONAL" | "OTHER";
export type CalendarRecurrenceFrequency = "NONE" | "DAILY" | "WEEKLY" | "MONTHLY";

export interface CalendarEvent {
  id: string;
  title: string;
  description: string | null;
  startAt: string;
  endAt: string;
  occurrenceStartAt: string;
  occurrenceEndAt: string;
  type: CalendarEventType;
  colorHex: string | null;
  allDay: boolean;
  recurrenceFrequency: CalendarRecurrenceFrequency;
  recurrenceInterval: number;
  recurrenceCount: number | null;
  recurrenceUntil: string | null;
  reminderMinutesBefore: number | null;
  createdByUserId: string;
  assignedToUserId: string | null;
}

export interface CalendarEventRequest {
  title: string;
  description: string | null;
  startAt: string;
  endAt: string;
  type: CalendarEventType;
  colorHex: string | null;
  allDay: boolean;
  recurrenceFrequency: CalendarRecurrenceFrequency;
  recurrenceInterval: number | null;
  recurrenceCount: number | null;
  recurrenceUntil: string | null;
  reminderMinutesBefore: number | null;
  assignedToUserId: string | null;
}

export interface GoogleCalendarStatus {
  linked: boolean;
  calendarId: string | null;
  calendarName: string;
}
