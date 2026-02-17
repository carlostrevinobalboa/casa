# Modelo de dominio inicial

## Nucleo multiusuario
- UserAccount: usuario autenticado.
- Household: hogar compartido.
- HouseholdMember: relacion usuario-hogar con rol y color de identificacion.

Regla: todo dato de negocio pertenece a un `Household`; las notificaciones y filtros pueden ser por `UserAccount`.

## Despensa
- PantryItem: producto, unidad, cantidad actual, minimo, caducidad.
- ConsumptionLog: historico de consumo.

## Recetas
- Recipe: receta con pasos.
- RecipeIngredient: ingrediente y cantidad.
- RecipeExecution: historico de recetas realizadas por usuario.

## Compra
- ShoppingListItem: lista inteligente + manual.
- PurchaseTicket: ticket/foto y total.
- PurchaseHistory: precios reales por producto y fecha.

## Mascotas
- Pet: datos basicos y foto.
- PetWalk: paseo (manual/GPS, distancia, duracion, ruta).
- PetFoodLog: consumo comida.
- PetCareEvent: vacunas, pelo, desparasitacion, veterinario.
- PetWeightLog: evolucion de peso.

## Actividad deportiva
- SportActivity: correr/caminar/bici (GPS, distancia, duracion, ruta).

## Calendario
- CalendarEvent: eventos, recurrencia, categoria, recordatorios.

## Notificaciones
- Notification: alerta dirigida a un usuario concreto, opcionalmente ligada a entidad origen.
