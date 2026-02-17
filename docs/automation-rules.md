# Reglas de automatizacion

## Hogar compartido, recepcion individual
- Los datos de despensa/compra/mascotas/calendario viven en el hogar comun.
- Las alertas se asignan a usuario destinatario.
- Si un usuario ejecuta una receta, la notificacion de pasos temporizados se envia solo a ese usuario.

## Reglas iniciales
1. Receta ejecutada:
- Descontar ingredientes de despensa.
- Si item cae por debajo del minimo, crear `ShoppingListItem`.

2. Producto bajo minimo:
- Crear/actualizar item en compra inteligente.
- Notificar a usuarios responsables de compra.

3. Caducidad proxima:
- Notificar a responsables del hogar.

4. Comida de mascota baja:
- Crear item de compra.
- Notificar a responsables de mascota.

5. Cuidado de mascota proximo:
- Notificar al usuario asignado.

6. Evento deportivo con mascota:
- Crear `SportActivity` + `PetWalk` enlazados.

7. Tarea olvidada / recordatorio:
- Disparar notificacion push/websocket al usuario destinatario.
